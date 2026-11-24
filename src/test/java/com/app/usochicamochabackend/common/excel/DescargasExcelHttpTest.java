package com.app.usochicamochabackend.common.excel;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Todas las descargas a Excel salen del motor único (/api/v1/reportes) con el estilo común: un .xlsx
 * que abre, con la portada "Resumen" de primera hoja y el nombre del archivo con el periodo cuando se
 * pide.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DescargasExcelHttpTest {

    @MockitoBean
    private com.app.usochicamochabackend.substation.application.port.SubstationEventosPort eventos;

    @Autowired
    private MockMvc mockMvc;

    private static final UsernamePasswordAuthenticationToken ADMIN = new UsernamePasswordAuthenticationToken(
            new UserPrincipal(1L, "admin.excel"), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/reportes/consolidado-aceites-maquinaria",
            "/api/v1/reportes/consolidado-vehiculos",
            "/api/v1/reportes/consolidado-motos",
            "/api/v1/reportes/hojas-de-vida-maquinaria",
            "/api/v1/reportes/hojas-de-vida-vehiculos",
            "/api/v1/reportes/hojas-de-vida-motos",
            "/api/v1/reportes/inspecciones-maquinaria?desde=2026-01-01&hasta=2026-12-31",
            "/api/v1/reportes/inspecciones-vehiculos?desde=2026-01-01",
            "/api/v1/reportes/inspecciones-motos?hasta=2026-12-31",
            "/api/v1/reportes/ordenes-maquinaria?desde=2026-01-01&hasta=2026-12-31",
            "/api/v1/reportes/ordenes-vehiculos?desde=2026-01-01&hasta=2026-12-31",
            "/api/v1/reportes/ordenes-motos",
            "/api/v1/reportes/tanqueos?tipo=VEHICULO&desde=2026-01-01&hasta=2026-01-31",
            "/api/v1/reportes/tanqueos?tipo=MAQUINARIA_MOTO",
            "/api/v1/reportes/rendimiento-mensual?desde=2026-01-01&hasta=2026-02-28",
            "/api/v1/reportes/estaciones-ejecuciones?anio=2026&mes=9",
            "/api/v1/reportes/estaciones-avance?anio=2026&columnas=estacion,criticidad",
    })
    void cadaDescargaTraeLaPortadaYSeAbre(String url) throws Exception {
        MvcResult r = mockMvc.perform(get(url).with(authentication(ADMIN)))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(LibroExcel.TIPO_CONTENIDO, r.getResponse().getContentType());
        String disposicion = r.getResponse().getHeader("Content-Disposition");
        assertTrue(disposicion != null && disposicion.contains(".xlsx"), disposicion);
        if (url.contains("desde=2026-01-01&hasta=2026-12-31")) {
            assertTrue(disposicion.contains("2026-01-01_a_2026-12-31"), "El nombre dice el periodo: " + disposicion);
        }
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(r.getResponse().getContentAsByteArray()))) {
            assertEquals("Resumen", wb.getSheetName(0));
            assertTrue(wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue().startsWith("USOCHICAMOCHA"));
        }
    }

    @Autowired
    private com.app.usochicamochabackend.common.excel.reporte.MotorReportes motor;

    @Test
    void todoReporteDelCatalogoSeDescargaPorElMotor() throws Exception {
        var reportes = motor.listar(List.of("ROLE_ADMIN"));
        assertTrue(reportes.size() >= 20, "Flota, combustible y estaciones: " + reportes.size());
        for (var info : reportes) {
            if (info.filtros().stream().anyMatch(f -> f.obligatorio() && !f.clave().equals("tipo"))) {
                continue; // El historial de un activo necesita un activo con tanqueos.
            }
            String url = "/api/v1/reportes/" + info.clave() + (info.filtros().stream().anyMatch(f -> f.clave().equals("tipo"))
                    ? "?tipo=VEHICULO" : "");
            MvcResult r = mockMvc.perform(get(url).with(authentication(ADMIN))).andExpect(status().isOk()).andReturn();
            String disposicion = r.getResponse().getHeader("Content-Disposition");
            if (!info.usaPeriodo() && !info.clave().startsWith("estaciones-")) {
                // Sin periodo (consolidados, hojas de vida): el nombre lleva la fecha de corte.
                assertTrue(disposicion.contains(info.clave().replace('-', '_') + "_" + LocalDate.now()), disposicion);
            }
            try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(r.getResponse().getContentAsByteArray()))) {
                assertEquals("Resumen", wb.getSheetName(0), info.clave());
            }
        }
    }

    @Test
    void elCatalogoDependeDelRolYElMotorNiegaLoQueNoLeToca() throws Exception {
        UsernamePasswordAuthenticationToken operario = new UsernamePasswordAuthenticationToken(
                new UserPrincipal(2L, "operario.excel"), null, List.of(new SimpleGrantedAuthority("ROLE_OPERARIO")));
        mockMvc.perform(get("/api/v1/reportes").with(authentication(operario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.clave == 'ordenes-maquinaria')]").exists())
                .andExpect(jsonPath("$[?(@.clave == 'estaciones-ejecuciones')]").doesNotExist())
                .andExpect(jsonPath("$[?(@.clave == 'tanqueos')]").doesNotExist());
        mockMvc.perform(get("/api/v1/reportes/estaciones-ejecuciones").with(authentication(operario)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/reportes/no-existe").with(authentication(ADMIN)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/reportes/tanqueos").with(authentication(ADMIN)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void elCatalogoDiceComoPedirCadaReporte() throws Exception {
        mockMvc.perform(get("/api/v1/reportes").with(authentication(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.clave == 'hojas-de-vida-maquinaria')].usaPeriodo").value(false))
                .andExpect(jsonPath("$[?(@.clave == 'ordenes-maquinaria')].usaPeriodo").value(true))
                .andExpect(jsonPath("$[?(@.clave == 'tanqueos')].filtros[0].tipo").value("CONTEXTO"))
                .andExpect(jsonPath("$[?(@.clave == 'estaciones-ejecuciones')].filtros[*].tipo")
                        .value(org.hamcrest.Matchers.contains("DISCIPLINA", "ANIO", "MES", "ESTACION")));
    }
}
