package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.BadRequestException;
import com.app.usochicamochabackend.substation.application.dto.ElementoMarcadoRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionRequest;
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ConjuntoElementoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ConjuntoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ElementoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EstacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ActividadRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ConjuntoElementoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ConjuntoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.DisciplinaRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ElementoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EstacionRepository;
import jakarta.persistence.EntityManager;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Descarga a Excel de Estaciones: hojas, filtros y columnas elegidas. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ExportacionEstacionesIntegrationTest {

    @MockitoBean
    private com.app.usochicamochabackend.substation.application.port.SubstationEventosPort eventos;

    @Autowired private ExportacionEstacionesService exportacion;
    @Autowired private com.app.usochicamochabackend.common.excel.reporte.MotorReportes motor;
    @Autowired private SubstationEjecucionUseCase ejecucionUseCase;
    @Autowired private UserRepositoryJpa userRepositoryJpa;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private EstacionRepository estacionRepository;
    @Autowired private ActividadRepository actividadRepository;
    @Autowired private ConjuntoRepository conjuntoRepository;
    @Autowired private ElementoRepository elementoRepository;
    @Autowired private ConjuntoElementoRepository conjuntoElementoRepository;
    @Autowired private EntityManager entityManager;

    private UserPrincipal usuario;
    private EstacionEntity ayalas;
    private EstacionEntity surba;
    private ActividadEntity inspeccion;
    private ActividadEntity pintura;
    private ConjuntoEntity transformador;
    private ElementoEntity estadoFisico;

    @BeforeEach
    void setUp() {
        UserEntity u = userRepositoryJpa.save(UserEntity.builder().username("exporta").fullName("Exporta")
                .email("exporta@example.com").role("ADMIN").password("x").status(true).build());
        usuario = new UserPrincipal(u.getId(), u.getUsername());
        DisciplinaEntity electrico = disciplinaRepository.save(DisciplinaEntity.builder().codigo("ELECTRICO")
                .nombre("Eléctrico").orden(2).conjuntos("OBLIGATORIO").usaElementos(true).build());
        DisciplinaEntity civil = disciplinaRepository.save(DisciplinaEntity.builder().codigo("CIVIL").nombre("Civil").orden(1).build());
        ayalas = estacionRepository.save(EstacionEntity.builder().nombre("Ayalas").tipo("BOMBEO").frecuenciaBase("MENSUAL").status(true).build());
        surba = estacionRepository.save(EstacionEntity.builder().nombre("Surba").tipo("BOMBEO").frecuenciaBase("MENSUAL").status(true).build());
        inspeccion = actividadRepository.save(ActividadEntity.builder().nombre("Inspección eléctrica")
                .disciplina(electrico).capturaMovilHabilitada(true).status(true).build());
        pintura = actividadRepository.save(ActividadEntity.builder().nombre("Pintura muros")
                .disciplina(civil).capturaMovilHabilitada(true).status(true).build());
        transformador = conjuntoRepository.save(ConjuntoEntity.builder().nombre("Transformador").disciplina(electrico).build());
        estadoFisico = elementoRepository.save(ElementoEntity.builder().nombre("Estado físico").disciplina(electrico).build());
        conjuntoElementoRepository.save(ConjuntoElementoEntity.builder().conjunto(transformador).elemento(estadoFisico).pideDetalle(true).build());

        visita(ayalas, "ELECTRICO", inspeccion, LocalDate.of(2026, 9, 7), "CON_HALLAZGOS", true);
        visita(surba, "CIVIL", pintura, LocalDate.of(2026, 10, 14), "CONFORME", false);
        entityManager.flush();
    }

    private void visita(EstacionEntity estacion, String disciplina, ActividadEntity actividad, LocalDate fecha,
            String resultado, boolean conElemento) {
        ejecucionUseCase.registrarEjecucion(new EjecucionRequest(fecha, fecha.getMonthValue(), 2, estacion.getId(),
                disciplina, "PREVENTIVO", "INSPECCION", actividad.getId(), null, null, resultado, "Revisión",
                null, UUID.randomUUID(), null,
                conElemento ? List.of(transformador.getId()) : null, null, null, null,
                conElemento ? List.of(new ElementoMarcadoRequest(transformador.getId(), estadoFisico.getId(), "Con óxido")) : null,
                null), usuario);
    }

    /** Filas de una hoja; en las de datos, desde el encabezado (fila 4: arriba van título y periodo). */
    private static List<List<String>> tabla(byte[] xlsx, int hoja) throws Exception {
        try (XSSFWorkbook libro = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            Sheet s = libro.getSheetAt(hoja);
            List<List<String>> filas = new ArrayList<>();
            for (Row r : s) {
                if (hoja > 0 && r.getRowNum() < 3) {
                    continue;
                }
                List<String> celdas = new ArrayList<>();
                r.forEach(c -> celdas.add(switch (c.getCellType()) {
                    case NUMERIC -> org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(c)
                            ? c.getLocalDateTimeCellValue().toLocalDate().toString()
                            : String.valueOf((long) c.getNumericCellValue());
                    default -> c.toString();
                }));
                filas.add(celdas);
            }
            return filas;
        }
    }

    @Test
    void lasHojasTraenSusColumnasPorDefecto() {
        assertEquals(List.of("estaciones-ejecuciones", "estaciones-hallazgos", "estaciones-ordenes", "estaciones-avance",
                        "estaciones-actividades"),
                ExportacionEstacionesService.HOJAS.stream().map(h -> ReportesEstaciones.clave(h.getKey())).toList());
        var ej = exportacion.columnasDe("EJECUCIONES");
        assertTrue(ej.stream().anyMatch(c -> c.clave().equals("elementos") && c.porDefecto()));
        assertTrue(ej.stream().noneMatch(c -> c.clave().toLowerCase().contains("uuid")), "Nunca salen IDs internos");
    }

    @Test
    void ejecucionesConFiltroDeDisciplinaYColumnasElegidas() throws Exception {
        var archivo = exportar("ejecuciones", "ELECTRICO", 2026, null, null, List.of("estacion", "fecha", "elementos", "disciplina"), "exporta");

        assertEquals("estaciones_ejecuciones_electrico_2026-01-01_a_2026-12-31.xlsx", archivo.nombre());
        // Muestra para revisar a ojo (solo si se pide con -Dexcel.muestra=<carpeta>).
        String muestra = System.getProperty("excel.muestra");
        if (muestra != null) {
            java.nio.file.Files.write(java.nio.file.Path.of(muestra, archivo.nombre()), archivo.contenido());
        }
        List<List<String>> filas = tabla(archivo.contenido(), 1);
        // En el orden de la hoja, no en el que se pidieron.
        assertEquals(List.of("Fecha", "Estación", "Disciplina", "Elementos"), filas.get(0));
        assertEquals(2, filas.size(), "Solo la visita de Eléctrico");
        assertEquals(List.of("2026-09-07", "Ayalas", "Eléctrico", "Transformador › Estado físico (Con óxido)"), filas.get(1));
    }

    @Test
    void laHojaResumenDiceQueSeDescargoYCuantosRegistros() throws Exception {
        var archivo = exportar("EJECUCIONES", null, 2026, 9, ayalas.getId(), null, "exporta");

        List<List<String>> resumen = tabla(archivo.contenido(), 0);
        List<String> textos = resumen.stream().flatMap(List::stream).filter(t -> !t.isBlank()).toList();
        assertTrue(textos.contains("Estaciones de Bombeo · Ejecuciones"), textos.toString());
        assertTrue(resumen.stream().anyMatch(f -> f.size() > 1 && f.get(0).equals("Periodo") && f.get(1).equals("1 sep 2026 – 30 sep 2026")
                || f.size() > 1 && f.get(0).equals("Periodo") && f.get(1).startsWith("1 sep")), resumen.toString());
        assertTrue(resumen.stream().anyMatch(f -> f.size() > 1 && f.get(0).equals("Disciplina") && f.get(1).equals("Todas las disciplinas")));
        assertTrue(resumen.stream().anyMatch(f -> f.size() > 1 && f.get(0).equals("Estación") && f.get(1).equals("Ayalas")));
        assertTrue(resumen.stream().anyMatch(f -> f.size() > 1 && f.get(0).equals("Registros") && f.get(1).equals("1")));
        assertTrue(textos.stream().anyMatch(t -> t.endsWith("por exporta")), "Quién lo descargó");
        // Índice con un enlace a la hoja de datos y cuántos registros trae.
        assertTrue(resumen.stream().anyMatch(f -> f.size() > 2 && f.get(0).equals("Ejecuciones") && f.get(2).equals("1")), resumen.toString());
        assertEquals("estaciones_ejecuciones_2026-09-01_a_2026-09-30.xlsx", archivo.nombre());
    }

    @Test
    void hallazgosSoloTraeLosQueNoSonConformes() throws Exception {
        var archivo = exportar("HALLAZGOS", null, 2026, null, null, List.of("estacion", "resultado", "seguimiento"), "exporta");

        List<List<String>> filas = tabla(archivo.contenido(), 1);
        assertEquals(2, filas.size());
        assertEquals(List.of("Ayalas", "Con hallazgos", "Abierto"), filas.get(1));
    }

    @Test
    void avancePorEstacionConCriticidad() throws Exception {
        var archivo = exportar("AVANCE", null, 2026, null, null, List.of("estacion", "total", "criticidad"), "exporta");

        List<List<String>> filas = tabla(archivo.contenido(), 1);
        assertEquals(List.of("Estación", "Ejecutado total", "Criticidad"), filas.get(0));
        // Cada estación tiene la mitad de las intervenciones: ≥ 12 % es Alto.
        assertTrue(filas.contains(List.of("Ayalas", "1", "Alto")), filas.toString());
    }

    @Test
    void cortesDeCriticidad() {
        assertEquals("Alto", ExportacionEstacionesService.nivelCriticidad(12.0));
        assertEquals("Medio", ExportacionEstacionesService.nivelCriticidad(6.0));
        assertEquals("Bajo", ExportacionEstacionesService.nivelCriticidad(5.9));
        assertEquals("Bajo", ExportacionEstacionesService.nivelCriticidad(null));
    }

    @Test
    void unaHojaDesconocidaEsUn404YSinColumnasOMesMaloUn400() {
        assertThrows(com.app.usochicamochabackend.exception.ResourceNotFoundException.class,
                () -> exportar("NOMINA", null, 2026, null, null, null, "x"));
        assertThrows(BadRequestException.class, () -> exportar("EJECUCIONES", null, 2026, null, null, List.of("noExiste"), "x"));
        assertThrows(BadRequestException.class, () -> exportar("EJECUCIONES", null, 2026, 13, null, null, "x"));
    }

    @Test
    void lasHojasEstanEnElMotorConSusFiltrosYColumnas() {
        var info = motor.listar(List.of("ROLE_SUPERVISOR_OPERATIVO")).stream()
                .filter(r -> r.clave().equals("estaciones-avance")).findFirst().orElseThrow();
        assertEquals("Estaciones de Bombeo", info.modulo());
        assertEquals(List.of("disciplina", "anio", "estacionId"), info.filtros().stream().map(f -> f.clave()).toList());
        assertTrue(info.columnas().stream().anyMatch(c -> c.clave().equals("criticidad")));
        assertTrue(motor.listar(List.of("ROLE_OPERARIO")).stream().noneMatch(r -> r.clave().startsWith("estaciones-")),
                "El operario no descarga Estaciones");
    }

    /** Por el motor único, como la web. */
    private com.app.usochicamochabackend.common.excel.reporte.MotorReportes.Archivo exportar(String hoja, String disciplina,
            Integer anio, Integer mes, Long estacionId, List<String> columnas, String usuario) throws java.io.IOException {
        java.util.Map<String, String> f = new java.util.HashMap<>();
        f.put("disciplina", disciplina);
        f.put("anio", anio == null ? null : anio.toString());
        f.put("mes", mes == null ? null : mes.toString());
        f.put("estacionId", estacionId == null ? null : estacionId.toString());
        return motor.generar(ReportesEstaciones.clave(hoja),
                new com.app.usochicamochabackend.common.excel.reporte.PeticionReporte(null, f, columnas, usuario),
                List.of("ROLE_ADMIN"));
    }
}
