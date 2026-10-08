package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.BadRequestException;
import com.app.usochicamochabackend.substation.application.dto.CambioCampo;
import com.app.usochicamochabackend.substation.application.dto.ActividadRequest;
import com.app.usochicamochabackend.substation.application.dto.DescarteResultado;
import com.app.usochicamochabackend.substation.application.dto.CriticidadResponse;
import com.app.usochicamochabackend.substation.application.dto.PublicacionResponse;
import com.app.usochicamochabackend.substation.application.dto.ResumenBorradorResponse;
import com.app.usochicamochabackend.substation.application.dto.PublicacionResultado;
import com.app.usochicamochabackend.substation.application.dto.CopiarAnioRequest;
import com.app.usochicamochabackend.substation.application.dto.AsignarCitasRequest;
import com.app.usochicamochabackend.substation.application.dto.AsignacionResultado;
import com.app.usochicamochabackend.substation.application.dto.CronogramaResponse;
import com.app.usochicamochabackend.substation.application.dto.CumplimientoResponse;
import com.app.usochicamochabackend.substation.application.dto.EjecucionEditRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionResponse;
import com.app.usochicamochabackend.substation.application.dto.EstacionResponse;
import com.app.usochicamochabackend.substation.application.dto.IndicadorEstacionResponse;
import com.app.usochicamochabackend.substation.application.dto.ProgramacionResponse;
import com.app.usochicamochabackend.substation.application.dto.ResolverHallazgoRequest;
import com.app.usochicamochabackend.substation.application.dto.ResumenActividadResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationCatalogAdminUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationCatalogUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationCronogramaUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationHallazgoUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationIndicadoresUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EstacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ProgramacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.PublicacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ActividadRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.DisciplinaRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EstacionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.HallazgoSeguimientoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ProgramacionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.PublicacionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración real contra el esquema H2 (modo PostgreSQL). El perfil `test`
 * real (application-test.properties) corre con Flyway deshabilitado y
 * spring.jpa.hibernate.ddl-auto=create-drop: el esquema lo genera Hibernate a partir de
 * las entidades JPA, no las migraciones V29/V30/V34-V38.
 *
 * <p>Este test es deliberadamente INDEPENDIENTE del contenido del seeder de producción
 * (V30__mantenimiento_subestaciones_catalogos_seed.sql): ese seeder todavía no está
 * terminado/definitivo — la siembra real de datos de producción se hará de forma manual
 * más adelante — así que la lógica de negocio que se prueba aquí no debe depender de
 * cuántas estaciones/actividades reales existan hoy ni de sus nombres. El
 * {@code @BeforeEach} siembra, vía los repositorios JPA, un fixture SINTÉTICO mínimo y
 * arbitrario (estaciones y actividades con nombres inventados tipo "Estación Test Uno");
 * cada {@code @Test} agrega encima la programación puntual que necesita para ejercitar su
 * camino de lógica. Las aserciones verifican comportamiento (filtrado, idempotencia,
 * cálculo de cumplimiento, etc.), no conteos que coincidan con el seed real. Ver también
 * prepararVistasH2SoloUnaVez() para cómo se resuelven las vistas SQL de V37 bajo H2 — eso
 * sí es infraestructura de test legítima, no tiene relación con el seeder.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = "app.storage.uploads-root=${java.io.tmpdir}/subestaciones-test-uploads")
class SubstationServiceIntegrationTest {

    /** Reloj fijo: "hoy" es el 15-sep-2026 (septiembre abierto; enero–agosto cerrados). */
    @org.springframework.boot.test.context.TestConfiguration
    static class RelojFijo {
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        CalendarioMantenimiento calendarioFijo() {
            return new CalendarioMantenimiento(java.time.Clock.fixed(
                    java.time.Instant.parse("2026-09-15T17:00:00Z"), CalendarioMantenimiento.ZONA));
        }
    }

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.app.usochicamochabackend.substation.application.port.SubstationEventosPort eventos;

    @Autowired
    private SubstationCatalogUseCase catalogUseCase;

    @Autowired
    private SubstationCatalogAdminUseCase catalogAdminUseCase;

    @Autowired
    private SubstationEjecucionUseCase ejecucionUseCase;

    @Autowired
    private SubstationIndicadoresUseCase indicadoresUseCase;

    @Autowired
    private SubstationHallazgoUseCase hallazgoUseCase;

    @Autowired
    private HallazgoSeguimientoRepository hallazgoSeguimientoRepository;

    @Autowired
    private UserRepositoryJpa userRepositoryJpa;

    @Autowired
    private EstacionRepository estacionRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private ProgramacionRepository programacionRepository;

    @Autowired
    private SubstationCronogramaUseCase cronogramaUseCase;

    @Autowired
    private PublicacionRepository publicacionRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManager entityManager;

    private UserPrincipal usuario;
    private DisciplinaEntity civil;

    /** Fixture base sintético: 3 estaciones y 2 actividades CIVIL capturables, con nombres inventados. */
    private EstacionEntity estacionUno;
    private EstacionEntity estacionDos;
    private EstacionEntity estacionTres;
    private ActividadEntity actividadUno;
    private ActividadEntity actividadDos;

    /**
     * v_mant_cumplimiento / v_mant_indicadores_estacion / v_mant_resumen_actividad (V37)
     * son VIEWs reales en Postgres. Bajo H2 con ddl-auto=create-drop, Hibernate no sabe
     * que CumplimientoView/IndicadorEstacionView/ResumenActividadView (@Immutable,
     * @Table) mapean vistas — genera TABLAS físicas vacías con ese nombre igual que para
     * cualquier otra @Entity. Se reemplazan una sola vez por contexto de Spring, con una
     * conexión JDBC aparte (no la del EntityManager de la transacción del test, porque en
     * H2 el DDL hace commit inmediato y rompería el rollback esperado de un @Test si
     * corriera dentro de su transacción).
     */
    private static final AtomicBoolean VISTAS_H2_LISTAS = new AtomicBoolean(false);

    @BeforeEach
    void setUp() throws SQLException {
        prepararVistasH2SoloUnaVez();

        UserEntity user = userRepositoryJpa.save(UserEntity.builder()
                .username("tecnico.test")
                .fullName("Técnico de Prueba")
                .email("tecnico.test@example.com")
                .role("OPERARIO")
                .password("irrelevante")
                .status(true)
                .build());
        usuario = new UserPrincipal(user.getId(), user.getUsername());

        civil = disciplinaRepository.save(DisciplinaEntity.builder().codigo("CIVIL").build());

        estacionUno = crearEstacion("Estación Test Uno");
        estacionDos = crearEstacion("Estación Test Dos");
        estacionTres = crearEstacion("Estación Test Tres");

        actividadUno = crearActividadCivilCapturable("Actividad Civil Capturable Uno");
        actividadDos = crearActividadCivilCapturable("Actividad Civil Capturable Dos");
    }

    private void prepararVistasH2SoloUnaVez() throws SQLException {
        if (!VISTAS_H2_LISTAS.compareAndSet(false, true)) {
            return;
        }
        try (Connection conexion = dataSource.getConnection();
             Statement st = conexion.createStatement()) {
            st.execute("DROP TABLE IF EXISTS v_mant_cumplimiento");

            // Copia literal de v_mant_cumplimiento de V46. Los indicadores ya no usan las otras dos
            // vistas de V37 (se calculan en el servicio por año).
            st.execute("""
                    CREATE VIEW v_mant_cumplimiento AS
                    SELECT
                        p.id                AS programacion_id,
                        p.anio,
                        p.mes,
                        e.id                AS estacion_id,
                        e.nombre            AS estacion_nombre,
                        e.tipo              AS estacion_tipo,
                        a.id                AS actividad_id,
                        a.nombre            AS actividad_nombre,
                        d.codigo            AS disciplina,
                        COUNT(ej.id)        AS ejecutado,
                        (COUNT(ej.id) > 0)  AS cumple,
                        MIN(ej.fecha)       AS fecha_ejecucion
                    FROM mant_programacion p
                    JOIN mant_estacion e ON e.id = p.estacion_id
                    JOIN mant_actividad a ON a.id = p.actividad_id
                    JOIN mant_disciplina d ON d.id = a.disciplina_id
                    LEFT JOIN mant_ejecucion ej ON ej.programacion_id = p.id
                    WHERE p.status = TRUE
                      AND p.estado = 'PUBLICADA'
                    GROUP BY p.id, p.anio, p.mes, e.id, e.nombre, e.tipo, a.id, a.nombre, d.codigo
                    """);

        }
    }

    // ---------------------------------------------------------------------
    // Helpers de fixture sintético (sin relación con el seeder real V30)
    // ---------------------------------------------------------------------

    private EstacionEntity crearEstacion(String nombre) {
        return estacionRepository.save(EstacionEntity.builder()
                .nombre(nombre)
                .tipo("BOMBEO")
                .frecuenciaBase("TRIMESTRAL")
                .status(true)
                .build());
    }

    private ActividadEntity crearActividadCivilCapturable(String nombre) {
        return actividadRepository.save(ActividadEntity.builder()
                .nombre(nombre)
                .disciplina(civil)
                .capturaMovilHabilitada(true)
                .status(true)
                .build());
    }

    private ProgramacionEntity programar(EstacionEntity estacion, ActividadEntity actividad, int anio, int mes) {
        return programacionRepository.save(ProgramacionEntity.builder()
                .anio(anio)
                .mes(mes)
                .estacion(estacion)
                .actividad(actividad)
                .status(true)
                .build());
    }

    private ProgramacionEntity programarEnEstado(EstacionEntity estacion, ActividadEntity actividad,
            int anio, int mes, String estado) {
        ProgramacionEntity cita = programar(estacion, actividad, anio, mes);
        cita.setEstado(estado);
        return programacionRepository.save(cita);
    }

    // ---------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------

    @Test
    void listarEstaciones_devuelveTodasLasCreadas() {
        List<EstacionResponse> estaciones = catalogAdminUseCase.listarEstaciones(false);
        assertEquals(3, estaciones.size());
        assertTrue(estaciones.stream().anyMatch(e -> e.nombre().equals(estacionUno.getNombre())));
        assertTrue(estaciones.stream().anyMatch(e -> e.nombre().equals(estacionDos.getNombre())));
        assertTrue(estaciones.stream().anyMatch(e -> e.nombre().equals(estacionTres.getNombre())));
    }

    @Test
    void listarActividadesCapturables_filtraPorDisciplinaYCapturaMovilHabilitada() {
        // Actividad CIVIL pero no capturable, y actividad capturable de otra disciplina:
        // ninguna de las dos debe aparecer en el filtro por CIVIL capturables.
        actividadRepository.save(ActividadEntity.builder()
                .nombre("Actividad Civil No Capturable")
                .disciplina(civil)
                .capturaMovilHabilitada(false)
                .status(true)
                .build());
        DisciplinaEntity otraDisciplina = disciplinaRepository.save(DisciplinaEntity.builder().codigo("ELECTRICO").build());
        actividadRepository.save(ActividadEntity.builder()
                .nombre("Actividad Eléctrica Capturable")
                .disciplina(otraDisciplina)
                .capturaMovilHabilitada(true)
                .status(true)
                .build());

        var actividades = catalogAdminUseCase.listarActividades("CIVIL", false);

        assertEquals(2, actividades.size());
        assertTrue(actividades.stream().anyMatch(a -> a.nombre().equals(actividadUno.getNombre())));
        assertTrue(actividades.stream().anyMatch(a -> a.nombre().equals(actividadDos.getNombre())));
        assertFalse(actividades.stream().anyMatch(a -> a.nombre().equals("Actividad Civil No Capturable")));
        assertFalse(actividades.stream().anyMatch(a -> a.nombre().equals("Actividad Eléctrica Capturable")));
    }

    @Test
    void listarProgramacion_filtraPorEstacionAnioMesYDisciplina() {
        programar(estacionUno, actividadUno, 2030, 5);
        programar(estacionUno, actividadDos, 2030, 5);
        programar(estacionUno, actividadUno, 2030, 6); // otro mes, no debe aparecer
        programar(estacionDos, actividadUno, 2030, 5); // otra estación, no debe aparecer

        List<ProgramacionResponse> citas = catalogUseCase.listarProgramacion(estacionUno.getId(), 2030, 5, "CIVIL");

        assertEquals(2, citas.size());
        assertTrue(citas.stream().anyMatch(c -> c.actividadNombre().equals(actividadUno.getNombre())));
        assertTrue(citas.stream().anyMatch(c -> c.actividadNombre().equals(actividadDos.getNombre())));
    }

    @Test
    void registrarEjecucion_desdeUnaCitaProgramada_marcaEsProgramadaYQuedaConsultable() {
        ProgramacionEntity cita = programar(estacionUno, actividadUno, 2030, 2);

        EjecucionRequest request = new EjecucionRequest(
                LocalDate.of(2030, 2, 15), 2, 3, estacionUno.getId(), "CIVIL",
                "PREVENTIVO", "MANTENIMIENTO",
                actividadUno.getId(), cita.getId(), null,
                "CONFORME", "Todo en orden.", null,
                UUID.randomUUID());

        EjecucionResponse guardada = ejecucionUseCase.registrarEjecucion(request, usuario);

        assertNotNull(guardada.id());
        assertTrue(guardada.esProgramada());
        assertEquals(estacionUno.getNombre(), guardada.estacionNombre());
        assertEquals("tecnico.test", guardada.responsable());

        EjecucionResponse recuperada = ejecucionUseCase.obtenerEjecucion(guardada.id());
        assertEquals(guardada.id(), recuperada.id());
        assertTrue(recuperada.evidencias().isEmpty());
    }

    @Test
    void registrarEjecucion_actividadNoPrevista_exigeMotivoYDescripcion() {
        EjecucionRequest sinMotivo = new EjecucionRequest(
                LocalDate.now(), 9, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "MANTENIMIENTO",
                null, null, null,
                "CONFORME", "obs", null,
                UUID.randomUUID());

        assertThrows(BadRequestException.class, () -> ejecucionUseCase.registrarEjecucion(sinMotivo, usuario));

        EjecucionRequest completo = new EjecucionRequest(
                LocalDate.now(), 9, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "MANTENIMIENTO",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs", "Se pintó una baranda que se estaba oxidando.",
                UUID.randomUUID());

        EjecucionResponse guardada = ejecucionUseCase.registrarEjecucion(completo, usuario);
        assertFalse(guardada.esProgramada());
        assertNull(guardada.actividadNombre());
        assertEquals("NO_PROGRAMADO", guardada.motivoNoCatalogado());
    }

    @Test
    void registrarEjecucion_esIdempotentePorUuidCliente() {
        UUID uuidCliente = UUID.randomUUID();
        EjecucionRequest request = new EjecucionRequest(
                LocalDate.now(), 9, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "MANTENIMIENTO",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs", "algo no catalogado",
                uuidCliente);

        EjecucionResponse primera = ejecucionUseCase.registrarEjecucion(request, usuario);
        EjecucionResponse segunda = ejecucionUseCase.registrarEjecucion(request, usuario);

        assertEquals(primera.id(), segunda.id());
    }

    @Test
    void agregarEvidencia_seGuardaYApareceEnElDetalle() throws Exception {
        EjecucionRequest request = new EjecucionRequest(
                LocalDate.now(), 9, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "CON_HALLAZGOS", "obs", "revision de rutina",
                UUID.randomUUID());
        EjecucionResponse ejecucion = ejecucionUseCase.registrarEjecucion(request, usuario);
        assertTrue(ejecucion.evidenciaPendiente());

        MockMultipartFile foto = new MockMultipartFile("file", "foto.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 1, 2, 3, 4});
        var evidencia = ejecucionUseCase.agregarEvidencia(ejecucion.id(), foto);

        assertNotNull(evidencia.id());
        assertEquals("foto.jpg", evidencia.nombreOriginal());
        // La ruta pública debe incluir el prefijo "/uploads/" igual que los demás
        // módulos (documentos de vehículo, facturas) — sin esto, el navegador/app
        // piden la imagen sin ese prefijo y el backend responde 403 (bug real, V40).
        assertTrue(evidencia.rutaArchivo().startsWith("/uploads/subestaciones/ejecuciones/"));

        EjecucionResponse detalle = ejecucionUseCase.obtenerEjecucion(ejecucion.id());
        assertEquals(1, detalle.evidencias().size());
        assertFalse(detalle.evidenciaPendiente());
    }

    @Test
    void registrarEjecucion_rechazaTipoActividadOtroParaCivil() {
        EjecucionRequest request = new EjecucionRequest(
                LocalDate.now(), 9, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "OTRO",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs", "algo",
                UUID.randomUUID());

        assertThrows(BadRequestException.class, () -> ejecucionUseCase.registrarEjecucion(request, usuario));
    }

    @Test
    void registrarEjecucion_rechazaMotivoNoCatalogadoOtroParaCivil() {
        EjecucionRequest request = new EjecucionRequest(
                LocalDate.now(), 9, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "MANTENIMIENTO",
                null, null, "OTRO",
                "CONFORME", "obs", "algo no catalogado",
                UUID.randomUUID());

        assertThrows(BadRequestException.class, () -> ejecucionUseCase.registrarEjecucion(request, usuario));
    }

    @Test
    void editarEjecucion_actualizaCamposYQuedaEnElHistorial() {
        EjecucionRequest request = new EjecucionRequest(
                LocalDate.now(), 9, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "CON_HALLAZGOS", "obs original", "revision de rutina",
                UUID.randomUUID());
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(request, usuario);

        var edicion = new EjecucionEditRequest(
                creada.fecha(), creada.mesEjecucion(), creada.semanaEjecucion(),
                "NO_PROGRAMADO", "INSPECCION", null, "NO_PROGRAMADO",
                "CONFORME", "obs corregida tras revisar de nuevo", "revision de rutina, sin novedades",
                "Se corrigió el resultado: se había marcado con hallazgos por error.");

        EjecucionResponse editada = ejecucionUseCase.editarEjecucion(creada.id(), edicion, usuario);

        assertEquals("CONFORME", editada.resultado());
        assertEquals("obs corregida tras revisar de nuevo", editada.observaciones());
        assertEquals(1, editada.ediciones().size());
        assertEquals("tecnico.test", editada.ediciones().get(0).usuario());
        assertTrue(editada.ediciones().get(0).motivo().contains("hallazgos por error"));
    }

    @Test
    void editarEjecucion_exigeMotivoDeAlMenos15Caracteres() {
        EjecucionRequest request = new EjecucionRequest(
                LocalDate.now(), 9, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs", "algo",
                UUID.randomUUID());
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(request, usuario);

        var edicionCorta = new EjecucionEditRequest(
                creada.fecha(), creada.mesEjecucion(), creada.semanaEjecucion(),
                "NO_PROGRAMADO", "INSPECCION", null, "NO_PROGRAMADO",
                "CONFORME", "obs", "algo", "muy corto");

        assertThrows(BadRequestException.class, () -> ejecucionUseCase.editarEjecucion(creada.id(), edicionCorta, usuario));
    }

    @Test
    void obtenerEjecucionPorProgramacion_devuelveLaEjecucionDeEsaCita() {
        ProgramacionEntity cita = programar(estacionUno, actividadUno, 2030, 2);

        EjecucionRequest request = new EjecucionRequest(
                LocalDate.of(2030, 2, 15), 2, 3, estacionUno.getId(), "CIVIL",
                "PREVENTIVO", "MANTENIMIENTO",
                actividadUno.getId(), cita.getId(), null,
                "CONFORME", "Todo en orden.", null,
                UUID.randomUUID());
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(request, usuario);

        EjecucionResponse encontrada = ejecucionUseCase.obtenerEjecucionPorProgramacion(cita.getId());
        assertEquals(creada.id(), encontrada.id());
    }

    @Test
    void listarEjecuciones_filtraPorEstacionYFecha() {
        EjecucionRequest request = new EjecucionRequest(
                LocalDate.of(2030, 3, 1), 3, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs", "algo",
                UUID.randomUUID());
        ejecucionUseCase.registrarEjecucion(request, usuario);

        var pagina = ejecucionUseCase.listarEjecuciones(
                estacionUno.getId(), LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31), null,
                null, null, null, null, null, null, PageRequest.of(0, 10));

        assertTrue(pagina.getTotalElements() >= 1);
        assertTrue(pagina.getContent().stream().allMatch(e -> e.estacionId().equals(estacionUno.getId())));
    }

    @Test
    void listarEjecuciones_filtraPorDisciplina_comoElRestoDeLasPestanas() {
        ejecucionUseCase.registrarEjecucion(new EjecucionRequest(
                LocalDate.of(2030, 5, 1), 5, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION", null, null, "NO_PROGRAMADO",
                "CONFORME", "obs", "algo", UUID.randomUUID()), usuario);

        var civil = ejecucionUseCase.listarEjecuciones(
                null, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31), null,
                null, null, null, null, null, "CIVIL", PageRequest.of(0, 10));
        var electrico = ejecucionUseCase.listarEjecuciones(
                null, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31), null,
                null, null, null, null, null, "ELECTRICO", PageRequest.of(0, 10));

        assertEquals(1, civil.getTotalElements());
        assertEquals(0, electrico.getTotalElements());
    }

    @Test
    void listarEjecuciones_sinEstacionId_trueDeTodasLasEstaciones() {
        EjecucionRequest enEstacionUno = new EjecucionRequest(
                LocalDate.of(2030, 4, 1), 4, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs estacion uno", "algo",
                UUID.randomUUID());
        EjecucionRequest enEstacionDos = new EjecucionRequest(
                LocalDate.of(2030, 4, 2), 4, 1, estacionDos.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs estacion dos", "algo",
                UUID.randomUUID());
        ejecucionUseCase.registrarEjecucion(enEstacionUno, usuario);
        ejecucionUseCase.registrarEjecucion(enEstacionDos, usuario);

        var pagina = ejecucionUseCase.listarEjecuciones(
                null, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31), null,
                null, null, null, null, null, null, PageRequest.of(0, 10));

        assertTrue(pagina.getTotalElements() >= 2);
        assertTrue(pagina.getContent().stream().anyMatch(e -> e.estacionId().equals(estacionUno.getId())));
        assertTrue(pagina.getContent().stream().anyMatch(e -> e.estacionId().equals(estacionDos.getId())));
    }

    @Test
    void listarEjecuciones_filtraPorEsProgramadaFalse_soloTraeNoProgramadas() {
        ProgramacionEntity cita = programar(estacionTres, actividadUno, 2030, 5);
        EjecucionRequest programada = new EjecucionRequest(
                LocalDate.of(2030, 5, 10), 5, 2, estacionTres.getId(), "CIVIL",
                "PREVENTIVO", "MANTENIMIENTO",
                actividadUno.getId(), cita.getId(), null,
                "CONFORME", "obs programada", null,
                UUID.randomUUID());
        EjecucionRequest noProgramada = new EjecucionRequest(
                LocalDate.of(2030, 5, 11), 5, 2, estacionTres.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs no programada", "algo",
                UUID.randomUUID());
        ejecucionUseCase.registrarEjecucion(programada, usuario);
        EjecucionResponse noProgramadaCreada = ejecucionUseCase.registrarEjecucion(noProgramada, usuario);

        var pagina = ejecucionUseCase.listarEjecuciones(
                null, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31), false,
                null, null, null, null, null, null, PageRequest.of(0, 10));

        assertTrue(pagina.getContent().stream().allMatch(e -> Boolean.FALSE.equals(e.esProgramada())));
        assertTrue(pagina.getContent().stream().anyMatch(e -> e.id().equals(noProgramadaCreada.id())));
    }

    @Test
    void listarEjecuciones_filtraPorResultado_aceptaVariosValores() {
        EjecucionRequest conforme = new EjecucionRequest(
                LocalDate.of(2030, 6, 1), 6, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs conforme", "algo",
                UUID.randomUUID());
        EjecucionRequest conHallazgos = new EjecucionRequest(
                LocalDate.of(2030, 6, 2), 6, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "CON_HALLAZGOS", "obs con hallazgos", "algo",
                UUID.randomUUID());
        EjecucionRequest requiereIntervencion = new EjecucionRequest(
                LocalDate.of(2030, 6, 3), 6, 1, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "REQUIERE_INTERVENCION", "obs requiere intervencion", "algo",
                UUID.randomUUID());
        ejecucionUseCase.registrarEjecucion(conforme, usuario);
        EjecucionResponse conHallazgosCreada = ejecucionUseCase.registrarEjecucion(conHallazgos, usuario);
        EjecucionResponse requiereIntervencionCreada = ejecucionUseCase.registrarEjecucion(requiereIntervencion, usuario);

        // Preset "solo hallazgos": dos valores en una sola llamada.
        var pagina = ejecucionUseCase.listarEjecuciones(
                null, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31), null,
                List.of("CON_HALLAZGOS", "REQUIERE_INTERVENCION"), null, null, null, null, null, PageRequest.of(0, 10));

        assertTrue(pagina.getContent().stream().noneMatch(e -> "CONFORME".equals(e.resultado())));
        assertTrue(pagina.getContent().stream().anyMatch(e -> e.id().equals(conHallazgosCreada.id())));
        assertTrue(pagina.getContent().stream().anyMatch(e -> e.id().equals(requiereIntervencionCreada.id())));
    }

    @Test
    void listarEjecuciones_filtraPorActividadId() {
        ProgramacionEntity cita = programar(estacionUno, actividadUno, 2030, 7);
        EjecucionRequest deActividadUno = new EjecucionRequest(
                LocalDate.of(2030, 7, 10), 7, 2, estacionUno.getId(), "CIVIL",
                "PREVENTIVO", "MANTENIMIENTO",
                actividadUno.getId(), cita.getId(), null,
                "CONFORME", "obs actividad uno", null,
                UUID.randomUUID());
        EjecucionRequest sinActividad = new EjecucionRequest(
                LocalDate.of(2030, 7, 11), 7, 2, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs sin actividad", "algo",
                UUID.randomUUID());
        EjecucionResponse deActividadUnoCreada = ejecucionUseCase.registrarEjecucion(deActividadUno, usuario);
        ejecucionUseCase.registrarEjecucion(sinActividad, usuario);

        var pagina = ejecucionUseCase.listarEjecuciones(
                null, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31), null,
                null, actividadUno.getId(), null, null, null, null, PageRequest.of(0, 10));

        assertTrue(pagina.getContent().stream().allMatch(e -> actividadUno.getId().equals(e.actividadId())));
        assertTrue(pagina.getContent().stream().anyMatch(e -> e.id().equals(deActividadUnoCreada.id())));
    }

    @Test
    void listarEjecuciones_filtraPorTipoMantenimientoYTipoActividad() {
        EjecucionRequest correctivoInspeccion = new EjecucionRequest(
                LocalDate.of(2030, 8, 1), 8, 1, estacionUno.getId(), "CIVIL",
                "CORRECTIVO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs correctivo inspeccion", "algo",
                UUID.randomUUID());
        EjecucionRequest preventivoMantenimiento = new EjecucionRequest(
                LocalDate.of(2030, 8, 2), 8, 1, estacionUno.getId(), "CIVIL",
                "PREVENTIVO", "MANTENIMIENTO",
                null, null, "NO_PROGRAMADO",
                "CONFORME", "obs preventivo mantenimiento", "algo",
                UUID.randomUUID());
        EjecucionResponse correctivoInspeccionCreada = ejecucionUseCase.registrarEjecucion(correctivoInspeccion, usuario);
        ejecucionUseCase.registrarEjecucion(preventivoMantenimiento, usuario);

        var pagina = ejecucionUseCase.listarEjecuciones(
                null, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 12, 31), null,
                null, null, "CORRECTIVO", "INSPECCION", null, null, PageRequest.of(0, 10));

        assertTrue(pagina.getContent().stream().allMatch(
                e -> "CORRECTIVO".equals(e.tipoMantenimiento()) && "INSPECCION".equals(e.tipoActividad())));
        assertTrue(pagina.getContent().stream().anyMatch(e -> e.id().equals(correctivoInspeccionCreada.id())));
    }

    @Test
    void cumplimientoPorMes_reflejaLaEjecucionRecienRegistrada() {
        ProgramacionEntity cita = programar(estacionUno, actividadUno, 2030, 2);

        List<CumplimientoResponse> antes = indicadoresUseCase.cumplimientoPorMes(2030, 2, "CIVIL");
        assertTrue(antes.stream().anyMatch(c -> c.programacionId().equals(cita.getId()) && !c.cumple()));

        EjecucionRequest request = new EjecucionRequest(
                LocalDate.of(2030, 2, 15), 2, 3, estacionUno.getId(), "CIVIL",
                "PREVENTIVO", "MANTENIMIENTO",
                actividadUno.getId(), cita.getId(), null,
                "CONFORME", "Todo en orden.", null,
                UUID.randomUUID());
        ejecucionUseCase.registrarEjecucion(request, usuario);

        // v_mant_cumplimiento es una vista sobre mant_ejecucion: Hibernate no sabe que
        // escribir en una tabla afecta las filas de una vista mapeada a otra entidad, así
        // que no la considera "sucia" para el auto-flush antes del próximo SELECT. Además,
        // el test comparte una sola sesión/persistence-context (@Transactional de clase),
        // así que sin este flush+clear el segundo query devolvería la misma instancia en
        // caché de primer nivel en vez de reflejar la fila recién insertada. En producción
        // esto no aplica: cada request HTTP usa su propia transacción/EntityManager.
        entityManager.flush();
        entityManager.clear();

        List<CumplimientoResponse> despues = indicadoresUseCase.cumplimientoPorMes(2030, 2, "CIVIL");
        CumplimientoResponse fila = despues.stream()
                .filter(c -> c.programacionId().equals(cita.getId()))
                .findFirst()
                .orElseThrow();
        assertTrue(fila.cumple());
        assertEquals(1, fila.ejecutado());
    }

    @Test
    void cumplimientoPorEstacion_devuelveSoloLasCitasDeEsaEstacion() {
        programar(estacionUno, actividadUno, 2030, 2);
        programar(estacionUno, actividadDos, 2030, 8);
        programar(estacionDos, actividadUno, 2030, 2); // otra estación, no debe aparecer

        List<CumplimientoResponse> citas = indicadoresUseCase.cumplimientoPorEstacion(estacionUno.getId(), 2030, "CIVIL");

        assertEquals(2, citas.size());
        assertTrue(citas.stream().allMatch(c -> c.estacionId().equals(estacionUno.getId())));
    }

    @Test
    void indicadoresPorEstacion_unaEstacionDesactivadaContinuaEnLosAniosEnQueTuvoCitas() {
        programar(estacionDos, actividadUno, 2030, 2);
        estacionDos.setStatus(false);
        estacionRepository.save(estacionDos);
        estacionTres.setStatus(false);
        estacionRepository.save(estacionTres); // sin citas ni registros: no sale
        entityManager.flush();

        List<IndicadorEstacionResponse> indicadores = indicadoresUseCase.indicadoresPorEstacion(2030, "CIVIL");

        IndicadorEstacionResponse dos = filaDe(indicadores, estacionDos);
        assertEquals(1, dos.programado());
        assertFalse(dos.activa());
        assertTrue(filaDe(indicadores, estacionUno).activa());
        assertTrue(indicadores.stream().noneMatch(i -> i.estacionId().equals(estacionTres.getId())));
    }

    @Test
    void indicadoresPorEstacion_incluyeTodasLasEstacionesActivasConSuProgramado() {
        programar(estacionUno, actividadUno, 2030, 2);
        // estacionDos y estacionTres quedan sin programación: deben seguir apareciendo con programado=0.

        List<IndicadorEstacionResponse> indicadores = indicadoresUseCase.indicadoresPorEstacion(2030, "CIVIL");

        assertEquals(3, indicadores.size());
        assertTrue(indicadores.stream().anyMatch(
                i -> i.estacionNombre().equals(estacionUno.getNombre()) && i.programado() > 0));
        assertTrue(indicadores.stream().anyMatch(
                i -> i.estacionNombre().equals(estacionDos.getNombre()) && i.programado() == 0));
    }

    @Test
    void resumenPorActividad_devuelveLasActividadesCivilesConSuProgramacionAnual() {
        programar(estacionUno, actividadUno, 2030, 1);
        programar(estacionDos, actividadUno, 2030, 2);
        programar(estacionTres, actividadUno, 2030, 3);
        // actividadDos no tiene ninguna cita programada: debe aparecer con programadoAnual=0.

        List<ResumenActividadResponse> resumen = indicadoresUseCase.resumenPorActividad("CIVIL", 2030);

        assertEquals(2, resumen.size());
        assertTrue(resumen.stream().anyMatch(r ->
                r.actividadNombre().equals(actividadUno.getNombre()) && r.programadoAnual() == 3));
        assertTrue(resumen.stream().anyMatch(r ->
                r.actividadNombre().equals(actividadDos.getNombre()) && r.programadoAnual() == 0));
    }

    @Test
    void indicadores_sinDisciplina_sumanTodas_yConDisciplina_filtranComoAntes() {
        DisciplinaEntity electrico = disciplinaRepository.save(DisciplinaEntity.builder().codigo("ELECTRICO").build());
        ActividadEntity alternador = actividadRepository.save(ActividadEntity.builder()
                .nombre("Arreglar el alternador").disciplina(electrico).capturaMovilHabilitada(true).status(true).build());
        programar(estacionUno, actividadUno, 2030, 2);
        programar(estacionUno, alternador, 2030, 3);

        assertEquals(2, filaDe(indicadoresUseCase.indicadoresPorEstacion(2030, null), estacionUno).programado());
        assertEquals(1, filaDe(indicadoresUseCase.indicadoresPorEstacion(2030, "CIVIL"), estacionUno).programado());
        assertEquals(1, filaDe(indicadoresUseCase.indicadoresPorEstacion(2030, "ELECTRICO"), estacionUno).programado());

        List<ResumenActividadResponse> todas = indicadoresUseCase.resumenPorActividad(null, 2030);
        assertTrue(todas.stream().anyMatch(r -> r.actividadNombre().equals("Arreglar el alternador")
                && r.disciplina().equals("ELECTRICO") && r.programadoAnual() == 1));
        assertTrue(todas.stream().anyMatch(r -> r.actividadId().equals(actividadUno.getId()) && r.disciplina().equals("CIVIL")));
        assertTrue(indicadoresUseCase.resumenPorActividad("CIVIL", 2030).stream()
                .noneMatch(r -> r.actividadNombre().equals("Arreglar el alternador")));
    }

    // ---------------------------------------------------------------------
    // SUB-03: seguimiento de hallazgos
    // ---------------------------------------------------------------------

    private EjecucionRequest ejecucionLibre(String resultado, LocalDate fecha, Long estacionId, UUID uuid) {
        return new EjecucionRequest(
                fecha, fecha.getMonthValue(), 1, estacionId, "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                null, null, "NO_PROGRAMADO",
                resultado, "obs", "revision de rutina",
                uuid);
    }

    private EjecucionEditRequest edicionConResultado(EjecucionResponse e, String resultado) {
        return new EjecucionEditRequest(
                e.fecha(), e.mesEjecucion(), e.semanaEjecucion(),
                "NO_PROGRAMADO", "INSPECCION", null, "NO_PROGRAMADO",
                resultado, "obs", "revision de rutina",
                "Cambio de resultado para la prueba de seguimiento.");
    }

    @Test
    void registrarEjecucion_conHallazgo_creaSeguimientoAbierto_yConformeNo() {
        EjecucionResponse conHallazgo = ejecucionUseCase.registrarEjecucion(
                ejecucionLibre("CON_HALLAZGOS", LocalDate.of(2030, 5, 1), estacionUno.getId(), UUID.randomUUID()), usuario);
        EjecucionResponse conforme = ejecucionUseCase.registrarEjecucion(
                ejecucionLibre("CONFORME", LocalDate.of(2030, 5, 2), estacionUno.getId(), UUID.randomUUID()), usuario);

        assertNotNull(conHallazgo.seguimiento());
        assertEquals("ABIERTO", conHallazgo.seguimiento().estado());
        assertEquals("Técnico de Prueba", conHallazgo.seguimiento().actualizadoPor());
        assertNull(conforme.seguimiento());
        assertTrue(hallazgoSeguimientoRepository.findByEjecucion_Id(conforme.id()).isEmpty());
    }

    @Test
    void registrarEjecucion_reintentoMismoUuid_noDuplicaSeguimiento() {
        UUID uuid = UUID.randomUUID();
        EjecucionResponse primera = ejecucionUseCase.registrarEjecucion(
                ejecucionLibre("REQUIERE_INTERVENCION", LocalDate.of(2030, 5, 3), estacionUno.getId(), uuid), usuario);
        EjecucionResponse reintento = ejecucionUseCase.registrarEjecucion(
                ejecucionLibre("REQUIERE_INTERVENCION", LocalDate.of(2030, 5, 3), estacionUno.getId(), uuid), usuario);

        assertEquals(primera.id(), reintento.id());
        long seguimientos = hallazgoSeguimientoRepository.findAll().stream()
                .filter(s -> s.getEjecucion().getId().equals(primera.id()))
                .count();
        assertEquals(1, seguimientos);
    }

    @Test
    void editarEjecucion_aConforme_ocultaSeguimiento_yDeVueltaLoReabreLimpio() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(
                ejecucionLibre("CON_HALLAZGOS", LocalDate.of(2030, 6, 1), estacionUno.getId(), UUID.randomUUID()), usuario);
        hallazgoUseCase.resolver(creada.id(), new ResolverHallazgoRequest("Se corrigió en sitio.", null, true), usuario);

        EjecucionResponse aConforme = ejecucionUseCase.editarEjecucion(creada.id(), edicionConResultado(creada, "CONFORME"), usuario);
        assertNull(aConforme.seguimiento());
        assertNull(ejecucionUseCase.obtenerEjecucion(creada.id()).seguimiento());

        EjecucionResponse deVuelta = ejecucionUseCase.editarEjecucion(creada.id(), edicionConResultado(creada, "CON_HALLAZGOS"), usuario);
        assertEquals("ABIERTO", deVuelta.seguimiento().estado());
        assertNull(deVuelta.seguimiento().observacionesCierre());
        assertNull(deVuelta.seguimiento().cerradoPor());
        assertFalse(deVuelta.seguimiento().resueltoMismaVisita());
    }

    @Test
    void resolver_conEjecucionPosteriorDeLaMismaEstacion_quedaResueltoConResponsable() {
        EjecucionResponse hallazgo = ejecucionUseCase.registrarEjecucion(
                ejecucionLibre("CON_HALLAZGOS", LocalDate.of(2030, 7, 1), estacionUno.getId(), UUID.randomUUID()), usuario);
        EjecucionResponse posterior = ejecucionUseCase.registrarEjecucion(
                ejecucionLibre("CONFORME", LocalDate.of(2030, 7, 20), estacionUno.getId(), UUID.randomUUID()), usuario);

        hallazgoUseCase.marcarEnProceso(hallazgo.id(), usuario);
        var resuelto = hallazgoUseCase.resolver(hallazgo.id(),
                new ResolverHallazgoRequest("Se selló la fisura.", posterior.id(), false), usuario);

        assertEquals("RESUELTO", resuelto.estado());
        assertEquals("Técnico de Prueba", resuelto.cerradoPor());
        assertNotNull(resuelto.cerradoEn());
        assertEquals(posterior.id(), resuelto.resueltoEnEjecucionId());
        assertEquals(LocalDate.of(2030, 7, 20), resuelto.resueltoEnEjecucionFecha());
    }

    @Test
    void listarEjecuciones_filtroSeguimiento_soloTraeEsosEstados() {
        LocalDate dia = LocalDate.of(2030, 8, 1);
        EjecucionResponse abierto = ejecucionUseCase.registrarEjecucion(
                ejecucionLibre("CON_HALLAZGOS", dia, estacionDos.getId(), UUID.randomUUID()), usuario);
        EjecucionResponse enProceso = ejecucionUseCase.registrarEjecucion(
                ejecucionLibre("CON_HALLAZGOS", dia, estacionDos.getId(), UUID.randomUUID()), usuario);
        EjecucionResponse resuelto = ejecucionUseCase.registrarEjecucion(
                ejecucionLibre("CON_HALLAZGOS", dia, estacionDos.getId(), UUID.randomUUID()), usuario);
        ejecucionUseCase.registrarEjecucion(ejecucionLibre("CONFORME", dia, estacionDos.getId(), UUID.randomUUID()), usuario);
        hallazgoUseCase.marcarEnProceso(enProceso.id(), usuario);
        hallazgoUseCase.resolver(resuelto.id(), new ResolverHallazgoRequest("Listo.", null, false), usuario);

        var pagina = ejecucionUseCase.listarEjecuciones(
                estacionDos.getId(), dia, dia, null,
                null, null, null, null, List.of("ABIERTO", "EN_PROCESO"), null, PageRequest.of(0, 10));

        assertEquals(2, pagina.getTotalElements());
        assertTrue(pagina.getContent().stream().map(EjecucionResponse::id).toList()
                .containsAll(List.of(abierto.id(), enProceso.id())));
    }

    // ---------------------------------------------------------------------
    // Edición: campos cambiados, actividad bloqueada en registros de cita, "sin cambios"
    // ---------------------------------------------------------------------

    private EjecucionResponse registrarDeCita(ProgramacionEntity cita, ActividadEntity actividad) {
        return ejecucionUseCase.registrarEjecucion(new EjecucionRequest(
                LocalDate.of(2030, 3, 10), 3, 2, cita.getEstacion().getId(), "CIVIL",
                "PREVENTIVO", "MANTENIMIENTO",
                actividad.getId(), cita.getId(), null,
                "CONFORME", "Todo en orden.", null,
                UUID.randomUUID()), usuario);
    }

    private EjecucionEditRequest edicionDe(EjecucionResponse e, LocalDate fecha, Long actividadId, String resultado) {
        return new EjecucionEditRequest(
                fecha, e.mesEjecucion(), e.semanaEjecucion(),
                e.tipoMantenimiento(), e.tipoActividad(), actividadId, e.motivoNoCatalogado(),
                resultado, e.observaciones(), e.descripcionLibre(),
                "Corrección tras revisar la planilla de campo.");
    }

    @Test
    void editarEjecucion_guardaQueCamposCambiaronConAntesYDespues() {
        EjecucionResponse creada = registrarDeCita(programar(estacionUno, actividadUno, 2030, 3), actividadUno);

        EjecucionResponse editada = ejecucionUseCase.editarEjecucion(creada.id(),
                edicionDe(creada, LocalDate.of(2030, 3, 12), actividadUno.getId(), "CON_HALLAZGOS"), usuario);

        var ultima = editada.ediciones().get(editada.ediciones().size() - 1);
        assertEquals(2, ultima.cambios().size());
        assertTrue(ultima.cambios().contains(new CambioCampo("fecha", "2030-03-10", "2030-03-12")));
        assertTrue(ultima.cambios().contains(new CambioCampo("resultado", "CONFORME", "CON_HALLAZGOS")));
        // Y el hallazgo nuevo abre su seguimiento.
        assertEquals("ABIERTO", editada.seguimiento().estado());
    }

    @Test
    void editarEjecucion_deCita_noPermiteCambiarLaActividad() {
        EjecucionResponse creada = registrarDeCita(programar(estacionUno, actividadUno, 2030, 3), actividadUno);

        var otraActividad = assertThrows(ResponseStatusException.class, () -> ejecucionUseCase.editarEjecucion(
                creada.id(), edicionDe(creada, creada.fecha(), actividadDos.getId(), "CON_HALLAZGOS"), usuario));
        assertEquals(400, otraActividad.getStatusCode().value());
        assertEquals("La actividad de un registro del cronograma no se puede cambiar", otraActividad.getReason());

        var sinActividad = assertThrows(ResponseStatusException.class, () -> ejecucionUseCase.editarEjecucion(
                creada.id(), edicionDe(creada, creada.fecha(), null, "CON_HALLAZGOS"), usuario));
        assertEquals(400, sinActividad.getStatusCode().value());
    }

    @Test
    void editarEjecucion_deCita_conLaMismaActividad_comoElMovil_funciona() {
        EjecucionResponse creada = registrarDeCita(programar(estacionUno, actividadUno, 2030, 3), actividadUno);

        EjecucionResponse editada = ejecucionUseCase.editarEjecucion(creada.id(),
                edicionDe(creada, creada.fecha(), actividadUno.getId(), "REQUIERE_INTERVENCION"), usuario);

        assertEquals("REQUIERE_INTERVENCION", editada.resultado());
        assertEquals(actividadUno.getId(), editada.actividadId());
    }

    @Test
    void editarEjecucion_sinNingunCambio_responde400() {
        EjecucionResponse creada = registrarDeCita(programar(estacionUno, actividadUno, 2030, 3), actividadUno);

        var ex = assertThrows(ResponseStatusException.class, () -> ejecucionUseCase.editarEjecucion(
                creada.id(), edicionDe(creada, creada.fecha(), actividadUno.getId(), creada.resultado()), usuario));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals("La edición no modifica ningún campo", ex.getReason());
        assertTrue(ejecucionUseCase.obtenerEjecucion(creada.id()).ediciones().isEmpty());
    }

    @Test
    void editarEjecucion_libre_puedeCambiarDeActividadYGuardaSuNombre() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(new EjecucionRequest(
                LocalDate.of(2030, 3, 10), 3, 2, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "INSPECCION",
                actividadUno.getId(), null, null,
                "CONFORME", "Todo en orden.", null,
                UUID.randomUUID()), usuario);

        EjecucionResponse editada = ejecucionUseCase.editarEjecucion(creada.id(),
                edicionDe(creada, creada.fecha(), actividadDos.getId(), "CONFORME"), usuario);

        assertEquals(List.of(new CambioCampo("actividad", actividadUno.getNombre(), actividadDos.getNombre())),
                editada.ediciones().get(0).cambios());
    }

    // ---------------------------------------------------------------------
    // SUB-07: borrador/publicado. Estos casos protegen al móvil.
    // ---------------------------------------------------------------------

    @Test
    void citaNueva_quedaPublicadaPorDefecto() {
        ProgramacionEntity cita = programar(estacionUno, actividadUno, 2030, 4);

        assertEquals(ProgramacionEntity.PUBLICADA, cita.getEstado());
        assertFalse(cita.getPendienteRetiro());
    }

    @Test
    void borradorYRetirada_noLleganAlMovil_niPorCumplimientoNiPorProgramacion() {
        ProgramacionEntity publicada = programar(estacionUno, actividadUno, 2030, 5);
        ProgramacionEntity borrador = programarEnEstado(estacionUno, actividadDos, 2030, 5, ProgramacionEntity.BORRADOR);
        ProgramacionEntity retirada = programarEnEstado(estacionDos, actividadUno, 2030, 5, ProgramacionEntity.RETIRADA);
        entityManager.flush();
        entityManager.clear();

        List<Long> porMes = indicadoresUseCase.cumplimientoPorMes(2030, 5, "CIVIL").stream()
                .map(CumplimientoResponse::programacionId).toList();
        assertEquals(List.of(publicada.getId()), porMes);

        List<Long> porEstacion = indicadoresUseCase.cumplimientoPorEstacion(estacionUno.getId(), 2030, "CIVIL").stream()
                .map(CumplimientoResponse::programacionId).toList();
        assertEquals(List.of(publicada.getId()), porEstacion);
        assertTrue(indicadoresUseCase.cumplimientoPorEstacion(estacionDos.getId(), 2030, "CIVIL").isEmpty());

        List<Long> programacion = catalogUseCase.listarProgramacion(estacionUno.getId(), 2030, 5, "CIVIL").stream()
                .map(ProgramacionResponse::id).toList();
        assertEquals(List.of(publicada.getId()), programacion);
        assertFalse(programacion.contains(borrador.getId()));
        assertFalse(programacion.contains(retirada.getId()));
    }

    @Test
    void publicadaPendienteDeRetiro_sigueLlegandoAlMovilHastaPublicar() {
        ProgramacionEntity cita = programar(estacionUno, actividadUno, 2030, 7);
        cita.setPendienteRetiro(true);
        programacionRepository.save(cita);
        entityManager.flush();
        entityManager.clear();

        assertTrue(indicadoresUseCase.cumplimientoPorMes(2030, 7, "CIVIL").stream()
                .anyMatch(c -> c.programacionId().equals(cita.getId())));
    }

    @Test
    void ejecucionDelMovil_paraUnaCitaRetirada_seRegistraIgual() {
        // Técnico sin señal: ejecutó una cita que mientras tanto se quitó del cronograma.
        // Si el backend la rechazara, la ejecución quedaría atascada en la cola del móvil.
        ProgramacionEntity retirada = programarEnEstado(estacionUno, actividadUno, 2030, 3, ProgramacionEntity.RETIRADA);

        EjecucionRequest request = new EjecucionRequest(
                LocalDate.of(2030, 3, 10), 3, 2, estacionUno.getId(), "CIVIL",
                "PREVENTIVO", "MANTENIMIENTO",
                actividadUno.getId(), retirada.getId(), null,
                "CONFORME", "Ejecutada sin señal.", null,
                UUID.randomUUID());

        EjecucionResponse guardada = ejecucionUseCase.registrarEjecucion(request, usuario);

        assertNotNull(guardada.id());
    }

    @Test
    void citasDelAnioEnConfiguracion_cuentaSoloLasPublicadas() {
        int anioActual = 2026; // reloj fijo del test
        programar(estacionUno, actividadUno, anioActual, 11);
        programar(estacionDos, actividadUno, anioActual, 11);
        programarEnEstado(estacionTres, actividadUno, anioActual, 12, ProgramacionEntity.BORRADOR);
        programarEnEstado(estacionTres, actividadUno, anioActual, 11, ProgramacionEntity.RETIRADA);
        entityManager.flush();

        var actividad = catalogAdminUseCase.listarActividades("CIVIL", false).stream()
                .filter(a -> a.id().equals(actividadUno.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(2, actividad.citasPublicadasAnio());
    }

    // ---------------------------------------------------------------------
    // SUB-08: cronograma anual (lectura)
    // ---------------------------------------------------------------------

    private CronogramaResponse.Cita citaDe(CronogramaResponse cronograma, Long id) {
        return cronograma.citas().stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
    }

    private PublicacionEntity publicar(int anio, boolean inicial) {
        return publicacionRepository.save(PublicacionEntity.builder()
                .anio(anio)
                .usuario(inicial ? null : userRepositoryJpa.findById(usuario.id()).orElseThrow())
                .publicadoEn(java.time.LocalDateTime.of(anio, 1, 20, 16, 5))
                .altas(3)
                .bajas(1)
                .inicial(inicial)
                .build());
    }

    @Test
    void cronograma_anioSinCitas_vieneVacioYSinPublicacion() {
        CronogramaResponse cronograma = cronogramaUseCase.obtenerCronograma(2031, null);

        assertEquals(2031, cronograma.anio());
        assertTrue(cronograma.citas().isEmpty());
        assertNull(cronograma.ultimaPublicacion());
        assertEquals(0, cronograma.borrador().altas());
        assertEquals(0, cronograma.borrador().bajas());
        assertFalse(cronograma.puedeDeshacer());
        int anioActual = 2026; // reloj fijo del test
        assertEquals(anioActual, cronograma.anioActual());
        assertTrue(cronograma.mesActual() >= 1 && cronograma.mesActual() <= 12);
    }

    @Test
    void cronograma_traeBorradorYPublicada_conEjecucion_ySinRetiradasNiInactivas() {
        ProgramacionEntity ejecutada = programar(estacionUno, actividadUno, 2030, 2);
        ProgramacionEntity borrador = programarEnEstado(estacionUno, actividadDos, 2030, 11, ProgramacionEntity.BORRADOR);
        ProgramacionEntity porQuitar = programar(estacionDos, actividadUno, 2030, 10);
        porQuitar.setPendienteRetiro(true);
        programacionRepository.save(porQuitar);
        ProgramacionEntity retirada = programarEnEstado(estacionDos, actividadDos, 2030, 3, ProgramacionEntity.RETIRADA);
        ProgramacionEntity desactivada = programar(estacionDos, actividadDos, 2030, 4);
        desactivada.setStatus(false);
        programacionRepository.save(desactivada);
        ProgramacionEntity deInactiva = programar(estacionTres, actividadUno, 2030, 5);
        estacionTres.setStatus(false);
        estacionRepository.save(estacionTres);

        ejecucionUseCase.registrarEjecucion(new EjecucionRequest(
                LocalDate.of(2030, 2, 15), 2, 3, estacionUno.getId(), "CIVIL",
                "PREVENTIVO", "MANTENIMIENTO",
                actividadUno.getId(), ejecutada.getId(), null,
                "CONFORME", "Todo en orden.", null,
                UUID.randomUUID()), usuario);
        entityManager.flush();
        entityManager.clear();

        CronogramaResponse cronograma = cronogramaUseCase.obtenerCronograma(2030, null);

        assertEquals(3, cronograma.citas().size());
        CronogramaResponse.Cita c1 = citaDe(cronograma, ejecutada.getId());
        assertEquals(ProgramacionEntity.PUBLICADA, c1.estado());
        assertTrue(c1.tieneEjecucion());
        assertEquals(LocalDate.of(2030, 2, 15), c1.fechaEjecucion());
        assertEquals(2, c1.mes());
        assertEquals(estacionUno.getId(), c1.estacionId());
        assertEquals(actividadUno.getId(), c1.actividadId());
        assertEquals("CIVIL", c1.disciplina());

        CronogramaResponse.Cita c2 = citaDe(cronograma, borrador.getId());
        assertEquals(ProgramacionEntity.BORRADOR, c2.estado());
        assertFalse(c2.tieneEjecucion());
        assertNull(c2.fechaEjecucion());

        assertTrue(citaDe(cronograma, porQuitar.getId()).pendienteRetiro());
        assertNull(citaDe(cronograma, retirada.getId()));
        assertNull(citaDe(cronograma, desactivada.getId()));
        assertNull(citaDe(cronograma, deInactiva.getId()));

        assertEquals(1, cronograma.borrador().altas());
        assertEquals(1, cronograma.borrador().bajas());
    }

    @Test
    void cronograma_filtroDisciplina_soloFiltraCitas_elBorradorSeCuentaCompleto() {
        DisciplinaEntity electrico = disciplinaRepository.save(DisciplinaEntity.builder().codigo("ELECTRICO").build());
        ActividadEntity actividadElectrica = actividadRepository.save(ActividadEntity.builder()
                .nombre("Actividad Eléctrica").disciplina(electrico).capturaMovilHabilitada(true).status(true).build());
        ProgramacionEntity civil = programar(estacionUno, actividadUno, 2030, 6);
        programarEnEstado(estacionUno, actividadElectrica, 2030, 6, ProgramacionEntity.BORRADOR);
        entityManager.flush();

        CronogramaResponse soloCivil = cronogramaUseCase.obtenerCronograma(2030, "CIVIL");
        CronogramaResponse todas = cronogramaUseCase.obtenerCronograma(2030, null);

        assertEquals(List.of(civil.getId()), soloCivil.citas().stream().map(CronogramaResponse.Cita::id).toList());
        assertEquals(1, soloCivil.borrador().altas());
        assertEquals(2, todas.citas().size());
    }

    @Test
    void cronograma_ultimaPublicacionYPuedeDeshacer() {
        programar(estacionUno, actividadUno, 2030, 6);
        publicar(2030, true);
        entityManager.flush();

        CronogramaResponse soloInicial = cronogramaUseCase.obtenerCronograma(2030, null);
        assertTrue(soloInicial.ultimaPublicacion().inicial());
        assertEquals("Carga inicial", soloInicial.ultimaPublicacion().usuario());
        assertFalse(soloInicial.puedeDeshacer()); // la carga inicial nunca se deshace

        PublicacionEntity normal = publicar(2030, false);
        entityManager.flush();
        CronogramaResponse conPublicacion = cronogramaUseCase.obtenerCronograma(2030, null);
        assertEquals(normal.getId(), conPublicacion.ultimaPublicacion().id());
        assertEquals("Técnico de Prueba", conPublicacion.ultimaPublicacion().usuario());
        assertEquals(3, conPublicacion.ultimaPublicacion().altas());
        assertTrue(conPublicacion.puedeDeshacer());

        programarEnEstado(estacionDos, actividadUno, 2030, 12, ProgramacionEntity.BORRADOR);
        entityManager.flush();
        assertFalse(cronogramaUseCase.obtenerCronograma(2030, null).puedeDeshacer()); // hay borrador

        normal.setRevertida(true);
        publicacionRepository.save(normal);
        entityManager.flush();
        assertTrue(cronogramaUseCase.obtenerCronograma(2030, null).ultimaPublicacion().inicial());
    }

    // ---------------------------------------------------------------------
    // SUB-09: asignar, quitar, restaurar, copiar año, descartar
    // (2027+ = año futuro, todos los meses abiertos; 2020 = año pasado, todos cerrados)
    // ---------------------------------------------------------------------

    private ProgramacionEntity recargar(ProgramacionEntity cita) {
        entityManager.flush();
        entityManager.clear();
        return programacionRepository.findById(cita.getId()).orElseThrow();
    }

    private void ejecutar(ProgramacionEntity cita, EstacionEntity estacion, ActividadEntity actividad) {
        ejecucionUseCase.registrarEjecucion(new EjecucionRequest(
                LocalDate.of(cita.getAnio(), cita.getMes(), 10), cita.getMes(), 2, estacion.getId(), "CIVIL",
                "PREVENTIVO", "MANTENIMIENTO", actividad.getId(), cita.getId(), null,
                "CONFORME", "Hecho.", null, UUID.randomUUID()), usuario);
    }

    private ResponseStatusException conStatus(int status, org.junit.jupiter.api.function.Executable accion) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, accion);
        assertEquals(status, ex.getStatusCode().value());
        return ex;
    }

    @Test
    void asignar_3x3_conUnDuplicado_crea8EnBorrador_yElMovilNoLasVe() {
        programar(estacionUno, actividadUno, 2027, 10); // ya publicada: se omite como duplicada

        AsignacionResultado r = cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadUno.getId(),
                List.of(estacionUno.getId(), estacionDos.getId(), estacionTres.getId()), List.of(10, 11, 12)), usuario);

        assertEquals(8, r.creadas());
        assertEquals(1, r.omitidasDuplicadas());
        assertEquals(0, r.omitidasMesCerrado());
        assertEquals(0, r.omitidasEstacionInactiva());
        entityManager.flush();
        entityManager.clear();

        CronogramaResponse cron = cronogramaUseCase.obtenerCronograma(2027, null);
        assertEquals(8, cron.borrador().altas());
        assertEquals(8, programacionRepository.findByAnioAndStatusTrueAndEstado(2027, ProgramacionEntity.BORRADOR).stream()
                .filter(c -> c.getCreadaPor() != null && c.getCreadaPor().getId().equals(usuario.id())).count());
        // El móvil solo ve la publicada
        assertEquals(1, indicadoresUseCase.cumplimientoPorMes(2027, 10, "CIVIL").size());
        assertTrue(indicadoresUseCase.cumplimientoPorMes(2027, 11, "CIVIL").isEmpty());
    }

    @Test
    void asignar_omiteMesesCerradosYEstacionesInactivas() {
        AsignacionResultado pasado = cronogramaUseCase.asignar(new AsignarCitasRequest(2026, actividadUno.getId(),
                List.of(estacionUno.getId()), List.of(3, 4)), usuario);
        assertEquals(0, pasado.creadas());
        assertEquals(2, pasado.omitidasMesCerrado());

        estacionDos.setStatus(false);
        estacionRepository.save(estacionDos);
        AsignacionResultado conInactiva = cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadUno.getId(),
                List.of(estacionUno.getId(), estacionDos.getId()), List.of(5)), usuario);
        assertEquals(1, conInactiva.creadas());
        assertEquals(1, conInactiva.omitidasEstacionInactiva());
    }

    @Test
    void asignar_datosInvalidos_responde400() {
        ActividadEntity inactiva = actividadRepository.save(ActividadEntity.builder()
                .nombre("Actividad Inactiva").disciplina(civil).capturaMovilHabilitada(true).status(false).build());

        conStatus(400, () -> cronogramaUseCase.asignar(new AsignarCitasRequest(2027, inactiva.getId(),
                List.of(estacionUno.getId()), List.of(5)), usuario));
        conStatus(400, () -> cronogramaUseCase.asignar(new AsignarCitasRequest(2027, 999999L,
                List.of(estacionUno.getId()), List.of(5)), usuario));
        conStatus(400, () -> cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadUno.getId(),
                List.of(estacionUno.getId()), List.of(13)), usuario));
        conStatus(400, () -> cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadUno.getId(),
                List.of(999999L), List.of(5)), usuario));
    }

    @Test
    void quitar_borradorSeDescarta_publicadaQuedaPendiente_yElMovilLaSigueViendo() {
        ProgramacionEntity borrador = programarEnEstado(estacionUno, actividadUno, 2027, 5, ProgramacionEntity.BORRADOR);
        ProgramacionEntity publicada = programar(estacionUno, actividadDos, 2027, 5);

        cronogramaUseCase.quitar(borrador.getId(), usuario);
        cronogramaUseCase.quitar(publicada.getId(), usuario);

        assertFalse(recargar(borrador).getStatus());
        ProgramacionEntity p = recargar(publicada);
        assertTrue(p.getStatus());
        assertTrue(p.getPendienteRetiro());
        assertEquals(ProgramacionEntity.PUBLICADA, p.getEstado());
        assertEquals(1, indicadoresUseCase.cumplimientoPorMes(2027, 5, "CIVIL").size());
    }

    @Test
    void quitar_conEjecucionOMesCerrado_409_yCitaRetirada_404() {
        ProgramacionEntity ejecutada = programar(estacionUno, actividadUno, 2027, 2);
        ejecutar(ejecutada, estacionUno, actividadUno);
        ProgramacionEntity cerrada = programar(estacionUno, actividadDos, 2020, 6);
        ProgramacionEntity retirada = programarEnEstado(estacionDos, actividadUno, 2027, 3, ProgramacionEntity.RETIRADA);
        entityManager.flush();

        conStatus(409, () -> cronogramaUseCase.quitar(ejecutada.getId(), usuario));
        conStatus(409, () -> cronogramaUseCase.quitar(cerrada.getId(), usuario));
        assertThrows(com.app.usochicamochabackend.exception.ResourceNotFoundException.class,
                () -> cronogramaUseCase.quitar(retirada.getId(), usuario));
    }

    @Test
    void restaurar_soloSiEstabaMarcadaParaQuitar() {
        ProgramacionEntity publicada = programar(estacionUno, actividadUno, 2027, 8);
        conStatus(409, () -> cronogramaUseCase.restaurar(publicada.getId(), usuario));

        cronogramaUseCase.quitar(publicada.getId(), usuario);
        cronogramaUseCase.restaurar(publicada.getId(), usuario);

        assertFalse(recargar(publicada).getPendienteRetiro());
    }

    @Test
    void quitarYVolverAAsignarLaMismaCita_laCreaDeNuevo() {
        AsignarCitasRequest req = new AsignarCitasRequest(2027, actividadUno.getId(), List.of(estacionUno.getId()), List.of(9));
        cronogramaUseCase.asignar(req, usuario);
        entityManager.flush();
        ProgramacionEntity creada = programacionRepository.findByAnioAndStatusTrueAndEstado(2027, ProgramacionEntity.BORRADOR).get(0);
        cronogramaUseCase.quitar(creada.getId(), usuario);
        entityManager.flush();

        assertEquals(1, cronogramaUseCase.asignar(req, usuario).creadas());
    }

    @Test
    void copiarAnio_copiaSoloPublicadasActivasComoBorrador() {
        programar(estacionUno, actividadUno, 2025, 3);
        programar(estacionDos, actividadUno, 2025, 6);
        programarEnEstado(estacionUno, actividadDos, 2025, 4, ProgramacionEntity.BORRADOR); // no se copia
        programar(estacionTres, actividadUno, 2025, 7); // estación que se desactiva
        estacionTres.setStatus(false);
        estacionRepository.save(estacionTres);
        ActividadEntity desactivada = actividadRepository.save(ActividadEntity.builder()
                .nombre("Actividad Que Se Desactiva").disciplina(civil).capturaMovilHabilitada(true).status(true).build());
        programar(estacionUno, desactivada, 2025, 8);
        desactivada.setStatus(false);
        actividadRepository.save(desactivada);
        entityManager.flush();

        AsignacionResultado r = cronogramaUseCase.copiarAnio(new CopiarAnioRequest(2025, 2027), usuario);

        assertEquals(2, r.creadas());
        assertEquals(1, r.omitidasEstacionInactiva());
        entityManager.flush();
        entityManager.clear();
        assertEquals(2, programacionRepository.findByAnioAndStatusTrueAndEstado(2027, ProgramacionEntity.BORRADOR).size());
        assertTrue(indicadoresUseCase.cumplimientoPorEstacion(estacionUno.getId(), 2027, "CIVIL").isEmpty());

        // Repetir la copia no duplica
        assertEquals(0, cronogramaUseCase.copiarAnio(new CopiarAnioRequest(2025, 2027), usuario).creadas());
    }

    @Test
    void copiarAnio_destinoConPublicadas409_yMismoAnio400() {
        programar(estacionUno, actividadUno, 2025, 3);
        programar(estacionUno, actividadUno, 2027, 3);
        entityManager.flush();

        conStatus(409, () -> cronogramaUseCase.copiarAnio(new CopiarAnioRequest(2025, 2027), usuario));
        conStatus(400, () -> cronogramaUseCase.copiarAnio(new CopiarAnioRequest(2027, 2027), usuario));
    }

    @Test
    void descartarBorrador_bajaAltasYAnulaRetiros_peroNoTocaCitasConEjecucion() {
        ProgramacionEntity alta = programarEnEstado(estacionUno, actividadUno, 2027, 5, ProgramacionEntity.BORRADOR);
        ProgramacionEntity altaEjecutada = programarEnEstado(estacionDos, actividadUno, 2027, 5, ProgramacionEntity.BORRADOR);
        ejecutar(altaEjecutada, estacionDos, actividadUno);
        ProgramacionEntity publicada = programar(estacionUno, actividadDos, 2027, 6);
        cronogramaUseCase.quitar(publicada.getId(), usuario);
        entityManager.flush();

        cronogramaUseCase.descartarBorrador(2027, usuario);

        assertFalse(recargar(alta).getStatus());
        assertTrue(recargar(altaEjecutada).getStatus());
        assertFalse(recargar(publicada).getPendienteRetiro());
    }

    // ---------------------------------------------------------------------
    // SUB-10: publicar y deshacer. La prueba más importante para el móvil:
    // en cada paso se mira lo que devuelve /indicadores/cumplimiento.
    // ---------------------------------------------------------------------

    private List<Long> loQueVeElMovil(int anio, int mes) {
        entityManager.flush();
        entityManager.clear();
        return indicadoresUseCase.cumplimientoPorMes(anio, mes, "CIVIL").stream()
                .map(CumplimientoResponse::programacionId).sorted().toList();
    }

    private ProgramacionEntity unicaBorrador(int anio) {
        List<ProgramacionEntity> b = programacionRepository.findByAnioAndStatusTrueAndEstado(anio, ProgramacionEntity.BORRADOR);
        assertEquals(1, b.size());
        return b.get(0);
    }

    @Test
    void asignarPublicarDeshacer_deExtremoAExtremo_segunLoQueVeElMovil() {
        publicar(2027, true); // carga inicial
        ProgramacionEntity vieja = programar(estacionUno, actividadUno, 2027, 5);
        assertEquals(List.of(vieja.getId()), loQueVeElMovil(2027, 5));

        // Borrador: alta en estación dos y baja de la vieja. El móvil no ve nada distinto.
        cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadUno.getId(),
                List.of(estacionDos.getId()), List.of(5)), usuario);
        ProgramacionEntity nueva = unicaBorrador(2027);
        cronogramaUseCase.quitar(vieja.getId(), usuario);
        assertEquals(List.of(vieja.getId()), loQueVeElMovil(2027, 5));

        // Publicar: el móvil ve la nueva y deja de ver la vieja.
        PublicacionResultado pub = cronogramaUseCase.publicar(2027, usuario);
        assertEquals(1, pub.altas());
        assertEquals(1, pub.bajas());
        assertTrue(pub.noAplicadas().isEmpty());
        assertEquals(List.of(nueva.getId()), loQueVeElMovil(2027, 5));

        ProgramacionEntity retirada = programacionRepository.findById(vieja.getId()).orElseThrow();
        assertEquals(ProgramacionEntity.RETIRADA, retirada.getEstado());
        assertEquals(pub.publicacionId(), retirada.getRetiradaEn().getId());
        assertEquals(pub.publicacionId(), programacionRepository.findById(nueva.getId()).orElseThrow().getPublicadaEn().getId());

        CronogramaResponse trasPublicar = cronogramaUseCase.obtenerCronograma(2027, null);
        assertEquals(0, trasPublicar.borrador().altas() + trasPublicar.borrador().bajas());
        assertEquals(pub.publicacionId(), trasPublicar.ultimaPublicacion().id());
        assertTrue(trasPublicar.puedeDeshacer());

        // Deshacer: el móvil vuelve a ver la vieja y deja de ver la nueva; ambas quedan en el borrador.
        PublicacionResultado deshecha = cronogramaUseCase.deshacerUltimaPublicacion(2027, usuario);
        assertEquals(1, deshecha.altas());
        assertEquals(1, deshecha.bajas());
        assertEquals(List.of(vieja.getId()), loQueVeElMovil(2027, 5));

        assertEquals(ProgramacionEntity.BORRADOR, programacionRepository.findById(nueva.getId()).orElseThrow().getEstado());
        ProgramacionEntity restaurada = programacionRepository.findById(vieja.getId()).orElseThrow();
        assertEquals(ProgramacionEntity.PUBLICADA, restaurada.getEstado());
        assertTrue(restaurada.getPendienteRetiro());

        CronogramaResponse trasDeshacer = cronogramaUseCase.obtenerCronograma(2027, null);
        assertEquals(1, trasDeshacer.borrador().altas());
        assertEquals(1, trasDeshacer.borrador().bajas());
        assertTrue(trasDeshacer.ultimaPublicacion().inicial());
        assertFalse(trasDeshacer.puedeDeshacer());

        List<PublicacionResponse> historial = cronogramaUseCase.historialPublicaciones(2027);
        assertEquals(2, historial.size());
        assertTrue(historial.get(0).revertida());
        assertEquals("Técnico de Prueba", historial.get(0).revertidaPor());
        assertEquals(PublicacionResponse.CARGA_INICIAL, historial.get(1).usuario());
    }

    @Test
    void publicar_citaMarcadaParaQuitarQueSeEjecutoSinSenal_noSeRetira() {
        ProgramacionEntity cita = programar(estacionUno, actividadUno, 2027, 4);
        cronogramaUseCase.quitar(cita.getId(), usuario);
        ejecutar(cita, estacionUno, actividadUno); // el técnico la hizo sin señal

        PublicacionResultado pub = cronogramaUseCase.publicar(2027, usuario);

        assertEquals(0, pub.bajas());
        assertEquals(List.of(cita.getId()), pub.noAplicadas().stream().map(PublicacionResultado.NoAplicada::citaId).toList());
        assertEquals(List.of(cita.getId()), loQueVeElMovil(2027, 4));
        ProgramacionEntity p = programacionRepository.findById(cita.getId()).orElseThrow();
        assertEquals(ProgramacionEntity.PUBLICADA, p.getEstado());
        assertFalse(p.getPendienteRetiro());
    }

    @Test
    void deshacer_altaYaEjecutadaSeQueda() {
        cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadUno.getId(),
                List.of(estacionUno.getId()), List.of(6)), usuario);
        ProgramacionEntity alta = unicaBorrador(2027);
        cronogramaUseCase.publicar(2027, usuario);
        ejecutar(programacionRepository.findById(alta.getId()).orElseThrow(), estacionUno, actividadUno);

        PublicacionResultado r = cronogramaUseCase.deshacerUltimaPublicacion(2027, usuario);

        assertEquals(0, r.altas());
        assertEquals(1, r.noAplicadas().size());
        assertEquals(List.of(alta.getId()), loQueVeElMovil(2027, 6));
    }

    @Test
    void publicarYDeshacer_casosQueResponden409() {
        publicar(2027, true);
        conStatus(409, () -> cronogramaUseCase.publicar(2027, usuario)); // borrador vacío
        conStatus(409, () -> cronogramaUseCase.deshacerUltimaPublicacion(2027, usuario)); // solo la carga inicial

        cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadUno.getId(),
                List.of(estacionUno.getId()), List.of(7)), usuario);
        cronogramaUseCase.publicar(2027, usuario);
        cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadDos.getId(),
                List.of(estacionUno.getId()), List.of(7)), usuario);
        entityManager.flush();
        conStatus(409, () -> cronogramaUseCase.deshacerUltimaPublicacion(2027, usuario)); // hay borrador
    }

    @Test
    void deshacer_seRepiteUnaPublicacionPorVez() {
        cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadUno.getId(),
                List.of(estacionUno.getId()), List.of(8)), usuario);
        PublicacionResultado primera = cronogramaUseCase.publicar(2027, usuario);
        cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadUno.getId(),
                List.of(estacionDos.getId()), List.of(8)), usuario);
        PublicacionResultado segunda = cronogramaUseCase.publicar(2027, usuario);
        assertEquals(2, loQueVeElMovil(2027, 8).size());

        assertEquals(segunda.publicacionId(), cronogramaUseCase.deshacerUltimaPublicacion(2027, usuario).publicacionId());
        assertEquals(1, loQueVeElMovil(2027, 8).size());
        cronogramaUseCase.descartarBorrador(2027, usuario);
        assertEquals(primera.publicacionId(), cronogramaUseCase.deshacerUltimaPublicacion(2027, usuario).publicacionId());
        assertTrue(loQueVeElMovil(2027, 8).isEmpty());
    }

    @Test
    void resumenBorrador_agrupaPorEstacionYOrdenaPorMes() {
        ProgramacionEntity publicada = programar(estacionDos, actividadDos, 2027, 3);
        cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadUno.getId(),
                List.of(estacionUno.getId(), estacionDos.getId()), List.of(11, 4)), usuario);
        cronogramaUseCase.quitar(publicada.getId(), usuario);
        entityManager.flush();

        ResumenBorradorResponse r = cronogramaUseCase.resumenBorrador(2027);

        assertEquals(4, r.altas());
        assertEquals(1, r.bajas());
        assertEquals(2, r.estacionesAfectadas());
        ResumenBorradorResponse.Estacion dos = r.porEstacion().stream()
                .filter(e -> e.estacionId().equals(estacionDos.getId())).findFirst().orElseThrow();
        assertEquals(List.of(3, 4, 11), dos.cambios().stream().map(ResumenBorradorResponse.Cambio::mes).toList());
        assertEquals("BAJA", dos.cambios().get(0).tipo());
        assertEquals(actividadDos.getNombre(), dos.cambios().get(0).actividadNombre());
    }

    // ---------------------------------------------------------------------
    // SUB-05 (reducido) y SUB-12: Dashboard y Resumen por actividad del año
    // (2020 = año pasado, todos los meses vencidos; 2030 = futuro, ninguno vencido)
    // ---------------------------------------------------------------------

    private IndicadorEstacionResponse filaDe(List<IndicadorEstacionResponse> filas, EstacionEntity estacion) {
        return filas.stream().filter(f -> f.estacionId().equals(estacion.getId())).findFirst().orElseThrow();
    }

    private ResumenActividadResponse filaDe(List<ResumenActividadResponse> filas, ActividadEntity actividad) {
        return filas.stream().filter(f -> f.actividadId().equals(actividad.getId())).findFirst().orElseThrow();
    }

    @Test
    void dashboard_4VencidasY3Ejecutadas_da75_ySoloCuentaElAnioYLoPublicado() {
        for (int mes = 1; mes <= 4; mes++) {
            ProgramacionEntity c = programar(estacionUno, actividadUno, 2020, mes);
            if (mes <= 3) {
                ejecutar(c, estacionUno, actividadUno);
            }
        }
        programarEnEstado(estacionUno, actividadDos, 2020, 5, ProgramacionEntity.BORRADOR); // no cuenta
        ejecutar(programar(estacionUno, actividadDos, 2021, 2), estacionUno, actividadDos);  // otro año
        entityManager.flush();
        entityManager.clear();

        List<IndicadorEstacionResponse> filas = indicadoresUseCase.indicadoresPorEstacion(2020, "CIVIL");

        IndicadorEstacionResponse uno = filaDe(filas, estacionUno);
        assertEquals(2020, uno.anio());
        assertEquals(4, uno.programado());
        assertEquals(3, uno.cumple());
        assertEquals(1, uno.noCumple());
        assertEquals(4, uno.vencidas());
        assertEquals(3, uno.ejecutadasVencidas());
        assertEquals(0, new java.math.BigDecimal("75.0").compareTo(uno.porcentajeCumplimiento()));
        assertEquals(3, uno.ejecutadoTotal()); // la ejecución de 2021 no entra
        assertEquals(3, uno.ejecutadoProgramado());

        IndicadorEstacionResponse dos = filaDe(filas, estacionDos);
        assertEquals(0, dos.programado());
        assertNull(dos.porcentajeCumplimiento());
    }

    @Test
    void dashboard_citasFuturasNoCuentanComoIncumplidas() {
        programar(estacionUno, actividadUno, 2030, 3);
        programar(estacionUno, actividadUno, 2030, 9);
        entityManager.flush();

        IndicadorEstacionResponse uno = filaDe(indicadoresUseCase.indicadoresPorEstacion(2030, "CIVIL"), estacionUno);

        assertEquals(2, uno.programado());
        assertEquals(0, uno.vencidas());
        assertNull(uno.porcentajeCumplimiento());
    }

    @Test
    void dashboard_citaEjecutadaDeMesAbierto_sumaAlPorcentaje_noQueda0De0() {
        ejecutar(programar(estacionUno, actividadUno, 2030, 3), estacionUno, actividadUno);
        programar(estacionUno, actividadUno, 2030, 9); // pendiente de un mes abierto: no cuenta
        entityManager.flush();
        entityManager.clear();

        IndicadorEstacionResponse uno = filaDe(indicadoresUseCase.indicadoresPorEstacion(2030, "CIVIL"), estacionUno);
        ResumenActividadResponse act = filaDe(indicadoresUseCase.resumenPorActividad("CIVIL", 2030), actividadUno);

        assertEquals(2, uno.programado());
        assertEquals(1, uno.vencidas());
        assertEquals(1, uno.ejecutadasVencidas());
        assertEquals(0, new java.math.BigDecimal("100.0").compareTo(uno.porcentajeCumplimiento()));
        assertEquals(1, act.vencidas());
        assertEquals(1, act.ejecutadasVencidas());
    }

    @Test
    void dashboard_cortaElMesEnCurso_conCitasDelMesEImprevistos() {
        // Hoy (reloj fijo) es 15-sep-2026: mes en curso = septiembre, va la mitad del mes.
        ejecutar(programar(estacionUno, actividadUno, 2026, 9), estacionUno, actividadUno);
        programar(estacionUno, actividadDos, 2026, 9);
        ejecutar(programar(estacionUno, actividadUno, 2026, 3), estacionUno, actividadUno); // otro mes
        ejecucionUseCase.registrarEjecucion(new EjecucionRequest(
                LocalDate.of(2026, 9, 12), 9, 2, estacionUno.getId(), "CIVIL",
                "NO_PROGRAMADO", "MANTENIMIENTO", null, null, "NO_PROGRAMADO",
                "CONFORME", "obs", "Se destapó un desagüe.", UUID.randomUUID()), usuario);
        entityManager.flush();
        entityManager.clear();

        IndicadorEstacionResponse uno = filaDe(indicadoresUseCase.indicadoresPorEstacion(2026, "CIVIL"), estacionUno);

        assertEquals(9, uno.mes());
        assertEquals(2, uno.programadoMes());
        assertEquals(1, uno.cumpleMes());
        assertEquals(2, uno.ejecutadoTotalMes());       // la cita de septiembre + el imprevisto
        assertEquals(1, uno.ejecutadoNoProgramadoMes());
        assertEquals(1, uno.ejecutadoNoProgramado());   // en el año
        assertEquals(0, new java.math.BigDecimal("50.0").compareTo(uno.porcentajeMesTranscurrido()));

        IndicadorEstacionResponse otroAnio = filaDe(indicadoresUseCase.indicadoresPorEstacion(2030, "CIVIL"), estacionUno);
        assertNull(otroAnio.mes());
        assertNull(otroAnio.programadoMes());
        assertNull(otroAnio.porcentajeMesTranscurrido());
    }

    @Test
    void resumenPorActividad_avanceDelAnio_estaciones_yCorteDelMesEnCurso() {
        // Hoy (reloj fijo) es 15-sep-2026: mes en curso = septiembre, va la mitad del mes.
        ejecutar(programar(estacionUno, actividadUno, 2026, 9), estacionUno, actividadUno);
        programar(estacionDos, actividadUno, 2026, 9);
        ejecutar(programar(estacionUno, actividadUno, 2026, 3), estacionUno, actividadUno); // otro mes
        programar(estacionTres, actividadUno, 2026, 11);
        // Imprevisto de la actividad: del catálogo, pero sin cita.
        ejecucionUseCase.registrarEjecucion(new EjecucionRequest(
                LocalDate.of(2026, 9, 12), 9, 2, estacionTres.getId(), "CIVIL",
                "CORRECTIVO", "MANTENIMIENTO", actividadUno.getId(), null, null,
                "CONFORME", "Se reparó una grieta.", null, UUID.randomUUID()), usuario);
        entityManager.flush();
        entityManager.clear();

        ResumenActividadResponse uno = filaDe(indicadoresUseCase.resumenPorActividad("CIVIL", 2026), actividadUno);

        assertEquals(4, uno.programadoAnual());
        assertEquals(2, uno.cumple());                  // avance del año: 2 de 4
        assertEquals(3, uno.estaciones());
        assertEquals(9, uno.mes());
        assertEquals(2, uno.programadoMes());
        assertEquals(1, uno.cumpleMes());
        assertEquals(2, uno.ejecutadoTotalMes());       // la cita de septiembre + el imprevisto
        assertEquals(1, uno.ejecutadoNoProgramadoMes());
        assertEquals(1, uno.ejecutadoNoProgramado());   // en el año
        assertEquals(0, new java.math.BigDecimal("50.0").compareTo(uno.porcentajeMesTranscurrido()));

        ResumenActividadResponse otroAnio = filaDe(indicadoresUseCase.resumenPorActividad("CIVIL", 2030), actividadUno);
        assertNull(otroAnio.mes());
        assertNull(otroAnio.programadoMes());
        assertNull(otroAnio.porcentajeMesTranscurrido());
        assertEquals(0, otroAnio.estaciones());
    }

    @Test
    void registrarEjecucion_avisaALaWeb() {
        ejecutar(programar(estacionUno, actividadUno, 2026, 9), estacionUno, actividadUno);

        org.mockito.Mockito.verify(eventos).ejecucionCambio(
                org.mockito.ArgumentMatchers.eq(estacionUno.getId()), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.eq("REGISTRADA"));
    }

    @Test
    void dashboard_soloEstacionesActivas() {
        estacionTres.setStatus(false);
        estacionRepository.save(estacionTres);

        List<IndicadorEstacionResponse> filas = indicadoresUseCase.indicadoresPorEstacion(2030, "CIVIL");

        assertEquals(2, filas.size());
        assertTrue(filas.stream().noneMatch(f -> f.estacionId().equals(estacionTres.getId())));
    }

    @Test
    void resumenPorActividad_soloElAnioPedido_soloPublicado_yMismoPorcentaje() {
        ejecutar(programar(estacionUno, actividadUno, 2020, 1), estacionUno, actividadUno);
        programar(estacionDos, actividadUno, 2020, 2);
        programarEnEstado(estacionTres, actividadUno, 2020, 3, ProgramacionEntity.BORRADOR); // no cuenta
        ejecutar(programar(estacionUno, actividadUno, 2021, 1), estacionUno, actividadUno);  // otro año
        ActividadEntity inactiva = actividadRepository.save(ActividadEntity.builder()
                .nombre("Actividad Inactiva Resumen").disciplina(civil).capturaMovilHabilitada(true).status(false).build());
        entityManager.flush();
        entityManager.clear();

        List<ResumenActividadResponse> filas = indicadoresUseCase.resumenPorActividad("CIVIL", 2020);

        ResumenActividadResponse uno = filaDe(filas, actividadUno);
        assertEquals(2020, uno.anio());
        assertEquals(2, uno.programadoAnual());
        assertEquals(1, uno.ejecutadoAnual());
        assertEquals(2, uno.vencidas());
        assertEquals(1, uno.ejecutadasVencidas());
        assertEquals(0, new java.math.BigDecimal("50.0").compareTo(uno.porcentajeCumplimiento()));
        assertTrue(filas.stream().noneMatch(f -> f.actividadId().equals(inactiva.getId())));
        assertNull(filaDe(filas, actividadDos).porcentajeCumplimiento());
    }

    // ---------------------------------------------------------------------
    // SUB-14: datos del Detalle por estación
    // ---------------------------------------------------------------------

    @Test
    void dashboard_conHallazgosDelAnioYHallazgosAbiertos() {
        ejecucionUseCase.registrarEjecucion(ejecucionLibre("CON_HALLAZGOS", LocalDate.of(2020, 3, 5), estacionUno.getId(), UUID.randomUUID()), usuario);
        EjecucionResponse resuelta = ejecucionUseCase.registrarEjecucion(
                ejecucionLibre("REQUIERE_INTERVENCION", LocalDate.of(2020, 4, 5), estacionUno.getId(), UUID.randomUUID()), usuario);
        ejecucionUseCase.registrarEjecucion(ejecucionLibre("CONFORME", LocalDate.of(2020, 5, 5), estacionUno.getId(), UUID.randomUUID()), usuario);
        ejecucionUseCase.registrarEjecucion(ejecucionLibre("CON_HALLAZGOS", LocalDate.of(2019, 5, 5), estacionUno.getId(), UUID.randomUUID()), usuario);
        entityManager.flush();
        var seguimiento = hallazgoSeguimientoRepository.findByEjecucion_Id(resuelta.id()).orElseThrow();
        seguimiento.setEstado("RESUELTO");
        hallazgoSeguimientoRepository.save(seguimiento);
        entityManager.flush();
        entityManager.clear();

        IndicadorEstacionResponse uno = filaDe(indicadoresUseCase.indicadoresPorEstacion(2020, "CIVIL"), estacionUno);

        assertEquals(2, uno.conHallazgos());      // del año 2020
        assertEquals(2, uno.hallazgosAbiertos()); // 2020 abierto + 2019 abierto (todos los años)
        assertEquals(0, filaDe(indicadoresUseCase.indicadoresPorEstacion(2020, "CIVIL"), estacionDos).hallazgosAbiertos());
    }

    @Test
    void criticidad_intervencionesHistoricasPorActividad_deMasAMenos() {
        for (int mes = 1; mes <= 3; mes++) {
            ejecutar(programar(estacionUno, actividadUno, 2020, mes), estacionUno, actividadUno);
        }
        ejecutar(programar(estacionUno, actividadUno, 2021, 1), estacionUno, actividadUno);
        ejecutar(programar(estacionUno, actividadDos, 2021, 2), estacionUno, actividadDos);
        ejecutar(programar(estacionDos, actividadDos, 2021, 2), estacionDos, actividadDos); // otra estación
        ejecucionUseCase.registrarEjecucion(ejecucionLibre("CONFORME", LocalDate.of(2021, 6, 1), estacionUno.getId(), UUID.randomUUID()), usuario); // libre: no cuenta
        entityManager.flush();
        entityManager.clear();

        List<CriticidadResponse> criticidad = indicadoresUseCase.criticidadPorEstacion(estacionUno.getId(), "CIVIL");

        assertEquals(List.of(actividadUno.getId(), actividadDos.getId()),
                criticidad.stream().map(CriticidadResponse::actividadId).toList());
        assertEquals(4, criticidad.get(0).intervenciones());
        assertEquals(1, criticidad.get(1).intervenciones());
    }

    // ---------------------------------------------------------------------
    // Pruebas de ruptura (29-sep): arreglos de los fallos encontrados
    // ---------------------------------------------------------------------

    @Test
    void asignarYCopiar_soloAnioActualOSiguiente() {
        conStatus(400, () -> cronogramaUseCase.asignar(new AsignarCitasRequest(2028, actividadUno.getId(),
                List.of(estacionUno.getId()), List.of(5)), usuario));
        conStatus(400, () -> cronogramaUseCase.asignar(new AsignarCitasRequest(2025, actividadUno.getId(),
                List.of(estacionUno.getId()), List.of(5)), usuario));
        conStatus(400, () -> cronogramaUseCase.copiarAnio(new CopiarAnioRequest(2026, 3000), usuario));
        assertEquals(1, cronogramaUseCase.asignar(new AsignarCitasRequest(2027, actividadUno.getId(),
                List.of(estacionUno.getId()), List.of(5)), usuario).creadas());
    }

    @Test
    void ejecucionSobreCitaEnBorrador_laPublica_yDescartarNoDejaUnBorradorFantasma() {
        programarEnEstado(estacionUno, actividadUno, 2027, 5, ProgramacionEntity.BORRADOR);
        ProgramacionEntity conEjecucion = programarEnEstado(estacionDos, actividadUno, 2027, 5, ProgramacionEntity.BORRADOR);
        ejecutar(conEjecucion, estacionDos, actividadUno);
        ProgramacionEntity publicada = programar(estacionTres, actividadUno, 2027, 6);
        cronogramaUseCase.quitar(publicada.getId(), usuario);
        entityManager.flush();

        assertEquals(ProgramacionEntity.PUBLICADA, recargar(conEjecucion).getEstado());
        DescarteResultado r = cronogramaUseCase.descartarBorrador(2027, usuario);

        assertEquals(1, r.altasDescartadas());
        assertEquals(1, r.retirosAnulados());
        assertEquals(0, r.conservadasConEjecucion());
        assertEquals(0, cronogramaUseCase.obtenerCronograma(2027, null).borrador().altas());
    }

    @Test
    void ejecucionConCitaDeOtraEstacionOActividad_seGuardaSinEnlazarla() {
        ProgramacionEntity cita = programar(estacionUno, actividadUno, 2026, 12);
        entityManager.flush();

        ejecutar(cita, estacionDos, actividadUno);
        ejecutar(cita, estacionUno, actividadDos);

        assertFalse(indicadoresUseCase.cumplimientoPorMes(2026, 12, "CIVIL").get(0).cumple());
        var registros = ejecucionUseCase.listarEjecuciones(null, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31),
                null, null, null, null, null, null, null, org.springframework.data.domain.Pageable.unpaged());
        assertEquals(2, registros.getTotalElements());
        assertTrue(registros.stream().noneMatch(EjecucionResponse::esProgramada));
    }

    @Test
    void ejecucionSobreCitaRetirada_laVuelveAPublicarYElMovilLaVeCumplida() {
        ProgramacionEntity cita = programar(estacionUno, actividadUno, 2026, 12);
        entityManager.flush();
        cronogramaUseCase.quitar(cita.getId(), usuario);
        cronogramaUseCase.publicar(2026, usuario);
        assertEquals(ProgramacionEntity.RETIRADA, recargar(cita).getEstado());

        ejecutar(cita, estacionUno, actividadUno); // el técnico la hizo sin señal

        assertEquals(ProgramacionEntity.PUBLICADA, recargar(cita).getEstado());
        assertEquals(List.of(cita.getId()), loQueVeElMovil(2026, 12));
        assertTrue(indicadoresUseCase.cumplimientoPorMes(2026, 12, "CIVIL").get(0).cumple());
    }

    @Test
    void ejecucionSobreCitaRetirada_conOtraVigenteIgual_seEnlazaALaVigente() {
        ProgramacionEntity vieja = programar(estacionUno, actividadUno, 2026, 12);
        entityManager.flush();
        cronogramaUseCase.quitar(vieja.getId(), usuario);
        cronogramaUseCase.publicar(2026, usuario);
        ProgramacionEntity nueva = programar(estacionUno, actividadUno, 2026, 12);
        entityManager.flush();

        ejecutar(vieja, estacionUno, actividadUno);

        assertEquals(ProgramacionEntity.RETIRADA, recargar(vieja).getEstado());
        assertEquals(List.of(nueva.getId()), loQueVeElMovil(2026, 12));
        assertTrue(indicadoresUseCase.cumplimientoPorMes(2026, 12, "CIVIL").get(0).cumple());
    }

    @Test
    void desactivarEstacion_retiraSusCitasFuturas_yAlPublicarElMovilDejaDeVerlas() {
        ProgramacionEntity futura = programar(estacionUno, actividadUno, 2026, 10);
        ProgramacionEntity borrador = programarEnEstado(estacionUno, actividadDos, 2026, 11, ProgramacionEntity.BORRADOR);
        ProgramacionEntity cerrada = programar(estacionUno, actividadDos, 2026, 3);
        ProgramacionEntity ejecutada = programar(estacionUno, actividadUno, 2026, 12);
        ejecutar(ejecutada, estacionUno, actividadUno);
        entityManager.flush();

        EstacionResponse r = catalogAdminUseCase.cambiarEstadoEstacion(estacionUno.getId(), false, usuario);

        assertEquals(2, r.citasRetiradas()); // la futura publicada y el borrador
        assertTrue(recargar(futura).getPendienteRetiro());
        assertFalse(recargar(borrador).getStatus());
        assertFalse(recargar(cerrada).getPendienteRetiro()); // mes cerrado: no se toca
        assertFalse(recargar(ejecutada).getPendienteRetiro()); // con ejecución: no se toca

        // La barra del Cronograma cuenta la baja aunque la estación ya no salga en la grilla
        assertEquals(1, cronogramaUseCase.obtenerCronograma(2026, null).borrador().bajas());
        assertEquals(List.of(futura.getId()), loQueVeElMovil(2026, 10)); // hasta publicar la sigue viendo
        cronogramaUseCase.publicar(2026, usuario);
        assertTrue(loQueVeElMovil(2026, 10).isEmpty());
    }

    @Test
    void desactivarActividad_retiraSusCitasFuturas() {
        ProgramacionEntity futura = programar(estacionDos, actividadDos, 2026, 11);
        entityManager.flush();

        var r = catalogAdminUseCase.cambiarEstadoActividad(actividadDos.getId(), false, usuario);

        assertEquals(1, r.citasRetiradas());
        assertTrue(recargar(futura).getPendienteRetiro());
    }

    @Test
    void reactivarEstacion_antesDePublicar_devuelveSusCitasAlCronograma() {
        ProgramacionEntity futura = programar(estacionUno, actividadUno, 2026, 10);
        entityManager.flush();
        catalogAdminUseCase.cambiarEstadoEstacion(estacionUno.getId(), false, usuario);
        assertTrue(recargar(futura).getPendienteRetiro());

        catalogAdminUseCase.cambiarEstadoEstacion(estacionUno.getId(), true, usuario);

        assertFalse(recargar(futura).getPendienteRetiro());
        assertEquals(0, cronogramaUseCase.obtenerCronograma(2026, null).borrador().bajas());
    }

    @Test
    void reactivarActividad_noDevuelveCitasDeUnaEstacionQueSigueInactiva() {
        ProgramacionEntity deActiva = programar(estacionUno, actividadDos, 2026, 11);
        ProgramacionEntity deInactiva = programar(estacionDos, actividadDos, 2026, 11);
        entityManager.flush();
        catalogAdminUseCase.cambiarEstadoActividad(actividadDos.getId(), false, usuario);
        catalogAdminUseCase.cambiarEstadoEstacion(estacionDos.getId(), false, usuario);

        catalogAdminUseCase.cambiarEstadoActividad(actividadDos.getId(), true, usuario);

        assertFalse(recargar(deActiva).getPendienteRetiro());
        assertTrue(recargar(deInactiva).getPendienteRetiro());
    }

    @Test
    void activarUnaEstacionYaActiva_noDeshaceLosQuitarManuales() {
        ProgramacionEntity quitada = programar(estacionUno, actividadUno, 2026, 12);
        entityManager.flush();
        cronogramaUseCase.quitar(quitada.getId(), usuario);

        catalogAdminUseCase.cambiarEstadoEstacion(estacionUno.getId(), true, usuario);

        assertTrue(recargar(quitada).getPendienteRetiro());
    }

    @Test
    void cambiarDisciplina_bloqueadoSiLaActividadYaSeUso() {
        DisciplinaEntity electrico = disciplinaRepository.save(DisciplinaEntity.builder().codigo("ELECTRICO").build());
        programar(estacionUno, actividadUno, 2026, 10);
        entityManager.flush();

        conStatus(409, () -> catalogAdminUseCase.actualizarActividad(actividadUno.getId(),
                new ActividadRequest(actividadUno.getNombre(), "ELECTRICO", true, null), usuario));
        // Sin uso sí se puede
        var libre = catalogAdminUseCase.actualizarActividad(actividadDos.getId(),
                new ActividadRequest(actividadDos.getNombre(), "ELECTRICO", true, null), usuario);
        assertEquals("ELECTRICO", libre.disciplina());
        assertNotNull(electrico.getId());

        var lista = catalogAdminUseCase.listarActividades(null, true);
        assertTrue(lista.stream().filter(a -> a.id().equals(actividadUno.getId())).findFirst().orElseThrow().enUso());
        assertFalse(lista.stream().filter(a -> a.id().equals(actividadDos.getId())).findFirst().orElseThrow().enUso());
    }
}
