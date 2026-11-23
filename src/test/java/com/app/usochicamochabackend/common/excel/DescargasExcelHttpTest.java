package com.app.usochicamochabackend.common.excel;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Todas las descargas a Excel del sistema responden con el estilo común: un .xlsx que abre, con la
 * portada "Resumen" de primera hoja y el nombre del archivo con el periodo cuando se pide.
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
            "/api/v1/oil-changes/consolidated/excel",
            "/api/v1/vehicle/monitoring/consolidated/export",
            "/api/v1/moto/monitoring/consolidated/export",
            "/api/v1/curriculum/export?desde=2026-01-01&hasta=2026-12-31",
            "/api/v1/curriculum/export/vehicles?desde=2026-01-01&hasta=2026-12-31",
            "/api/v1/curriculum/export/motos",
            "/api/v1/inspection/export?desde=2026-01-01&hasta=2026-12-31",
            "/api/v1/vehicle-inspection/export?desde=2026-01-01",
            "/api/v1/moto/inspections/export?hasta=2026-12-31",
            "/api/v1/order/export?desde=2026-01-01&hasta=2026-12-31",
            "/api/v1/order/vehicle/export?desde=2026-01-01&hasta=2026-12-31",
            "/api/v1/order/vehicle/export?soloMotos=true",
            "/api/v1/fuel/refueling/reporte/export?tipo=VEHICULO&fechaInicio=2026-01-01&fechaFin=2026-01-31",
            "/api/v1/fuel/rendimiento/export-mensual?fechaInicio=2026-01-01&fechaFin=2026-02-28",
            "/api/v1/substation/exportar?hoja=EJECUCIONES&anio=2026",
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
}
