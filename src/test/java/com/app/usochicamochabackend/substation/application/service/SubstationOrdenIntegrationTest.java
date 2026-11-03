package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.BadRequestException;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.substation.application.dto.ComentarioOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.CrearOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.EditarOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenDetalleResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenResumenResponse;
import com.app.usochicamochabackend.substation.application.dto.ResolverHallazgoRequest;
import com.app.usochicamochabackend.substation.application.port.SubstationHallazgoUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationEventosPort;
import com.app.usochicamochabackend.substation.application.port.SubstationOrdenUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EstacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ActividadRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.DisciplinaRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EjecucionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EstacionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.HallazgoSeguimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/** Órdenes de trabajo de estación (SUB-15) contra el esquema H2 que genera Hibernate. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubstationOrdenIntegrationTest {

    /** "Hoy" es el 15-oct-2026. */
    @TestConfiguration
    static class RelojFijo {
        @Bean
        @Primary
        CalendarioMantenimiento calendarioFijo() {
            return new CalendarioMantenimiento(Clock.fixed(Instant.parse("2026-10-15T17:00:00Z"), CalendarioMantenimiento.ZONA));
        }
    }

    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);

    @MockitoBean
    private SubstationEventosPort eventos;

    @Autowired private SubstationOrdenUseCase ordenUseCase;
    @Autowired private SubstationEjecucionUseCase ejecucionUseCase;
    @Autowired private HallazgoSeguimientoRepository seguimientoRepository;
    @Autowired private UserRepositoryJpa userRepositoryJpa;
    @Autowired private EstacionRepository estacionRepository;
    @Autowired private ActividadRepository actividadRepository;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private EjecucionRepository ejecucionRepository;
    @Autowired private SubstationHallazgoUseCase hallazgoUseCase;

    private UserPrincipal operario;
    private UserPrincipal supervisor;
    private UserEntity operarioEntity;
    private UserEntity otroOperario;
    private EstacionEntity estacion;
    private ActividadEntity correccion;
    private ActividadEntity electrica;

    @BeforeEach
    void setUp() {
        operarioEntity = usuario("tecnico.ot", "Técnico OT", "OPERARIO");
        otroOperario = usuario("tecnico.dos", "Ana Operaria", "OPERARIO");
        UserEntity sup = usuario("supervisor.ot", "Carlos Rivas", "SUPERVISOR_OPERATIVO");
        usuario("inactivo.ot", "Operario Inactivo", "OPERARIO").setStatus(false);
        operario = new UserPrincipal(operarioEntity.getId(), operarioEntity.getUsername());
        supervisor = new UserPrincipal(sup.getId(), sup.getUsername());

        DisciplinaEntity civil = disciplinaRepository.save(DisciplinaEntity.builder().codigo("CIVIL").build());
        DisciplinaEntity electrico = disciplinaRepository.save(DisciplinaEntity.builder().codigo("ELECTRICO").build());
        estacion = estacionRepository.save(EstacionEntity.builder().nombre("Estación OT").tipo("BOMBEO")
                .frecuenciaBase("TRIMESTRAL").status(true).build());
        correccion = actividadRepository.save(ActividadEntity.builder().nombre("Corrección hallazgos civiles")
                .disciplina(civil).capturaMovilHabilitada(true).status(true).build());
        electrica = actividadRepository.save(ActividadEntity.builder().nombre("Termografía")
                .disciplina(electrico).capturaMovilHabilitada(true).status(true).build());
    }

    private UserEntity usuario(String username, String nombre, String rol) {
        return userRepositoryJpa.save(UserEntity.builder().username(username).fullName(nombre)
                .email(username + "@example.com").role(rol).password("x").status(true).build());
    }

    private EjecucionResponse visita(String resultado) {
        return ejecucionUseCase.registrarEjecucion(new EjecucionRequest(
                HOY.minusDays(3), 10, 2, estacion.getId(), "CIVIL", "PREVENTIVO", "INSPECCION",
                null, null, "NO_PROGRAMADO", resultado, "Fisura en el muro norte", "revisión de rutina",
                UUID.randomUUID()), operario);
    }

    /** La visita del operario que ejecuta una orden, como la manda el móvil. */
    private EjecucionResponse visitaDeOrden(Long ordenId, Long estacionId, String resultado) {
        return ejecucionUseCase.registrarEjecucion(new EjecucionRequest(
                HOY, 10, 3, estacionId, "CIVIL", "CORRECTIVO", "MANTENIMIENTO",
                correccion.getId(), null, null, resultado, "Se selló la fisura", null,
                UUID.randomUUID(), ordenId), operario);
    }

    private CrearOrdenRequest pedido(Long ejecucionId) {
        return new CrearOrdenRequest(ejecucionId, "ALTA", HOY.plusDays(7), operarioEntity.getId(), null, correccion.getId(),
                "Sellar la fisura con mortero epóxico.");
    }

    private static void conEstado(HttpStatus esperado, Runnable accion) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, accion::run);
        assertThat(ex.getStatusCode()).isEqualTo(esperado);
    }

    @Test
    void crear_asignaConsecutivoPasaElHallazgoAEnProcesoYDejaElEvento() {
        EjecucionResponse hallazgo = visita("REQUIERE_INTERVENCION");

        OrdenResumenResponse orden = ordenUseCase.crear(pedido(hallazgo.id()), supervisor);

        assertThat(orden.consecutivo()).isEqualTo("OT-EB-00001");
        assertThat(orden.estado()).isEqualTo("ASIGNADA");
        assertThat(orden.asignadoANombre()).isEqualTo("Técnico OT");
        assertThat(orden.estacionNombre()).isEqualTo("Estación OT");
        assertThat(orden.hallazgoEjecucionId()).isEqualTo(hallazgo.id());
        assertThat(orden.vencida()).isFalse();
        assertThat(seguimientoRepository.findByEjecucion_Id(hallazgo.id()).orElseThrow().getEstado()).isEqualTo("EN_PROCESO");

        OrdenDetalleResponse detalle = ordenUseCase.detalle(orden.id());
        assertThat(detalle.eventos()).extracting("tipo").containsExactly("CREADA");
        assertThat(detalle.eventos().get(0).usuario()).isEqualTo("Carlos Rivas");
        assertThat(detalle.hallazgo().id()).isEqualTo(hallazgo.id());
        assertThat(detalle.ejecucion()).isNull();

        // El consecutivo sigue: la segunda orden es la 00002.
        assertThat(ordenUseCase.crear(pedido(visita("CON_HALLAZGOS").id()), supervisor).consecutivo()).isEqualTo("OT-EB-00002");
    }

    @Test
    void crear_unaSolaOrdenVigentePorHallazgo_yNoSobreUnaVisitaConforme() {
        EjecucionResponse hallazgo = visita("CON_HALLAZGOS");
        ordenUseCase.crear(pedido(hallazgo.id()), supervisor);

        conEstado(HttpStatus.CONFLICT, () -> ordenUseCase.crear(pedido(hallazgo.id()), supervisor));
        conEstado(HttpStatus.CONFLICT, () -> ordenUseCase.crear(pedido(visita("CONFORME").id()), supervisor));
        assertThrows(ResourceNotFoundException.class, () -> ordenUseCase.crear(pedido(999_999L), supervisor));
    }

    @Test
    void crear_validaPrioridadPlazoOperarioActividadEInstrucciones() {
        Long id = visita("CON_HALLAZGOS").id();
        Long op = operarioEntity.getId();
        Long act = correccion.getId();
        LocalDate plazo = HOY.plusDays(3);

        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "URGENTE", plazo, op, null, act, "x"), supervisor));
        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", HOY.minusDays(1), op, null, act, "x"), supervisor));
        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", plazo, supervisor.id(), null, act, "x"), supervisor));
        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", plazo, op, null, electrica.getId(), "x"), supervisor));
        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", plazo, op, null, act, "  "), supervisor));
        // Hoy mismo sí vale como fecha límite.
        assertThat(ordenUseCase.crear(new CrearOrdenRequest(id, "MEDIA", HOY, op, null, act, "x"), supervisor).prioridad()).isEqualTo("MEDIA");
    }

    @Test
    void editar_reasignaYCambiaElPlazoDejandoLosCambiosEnLaLineaDeTiempo() {
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(visita("CON_HALLAZGOS").id()), supervisor);

        OrdenResumenResponse editada = ordenUseCase.editar(orden.id(),
                new EditarOrdenRequest(null, HOY.plusDays(20), otroOperario.getId(), null, null, null), supervisor);

        assertThat(editada.asignadoANombre()).isEqualTo("Ana Operaria");
        assertThat(editada.fechaLimite()).isEqualTo(HOY.plusDays(20));
        var eventos = ordenUseCase.detalle(orden.id()).eventos();
        assertThat(eventos).extracting("tipo").containsExactly("CREADA", "EDITADA");
        assertThat(eventos.get(1).comentario()).contains("Reasignada de Técnico OT a Ana Operaria").contains("22/10/2026 → 04/11/2026");

        // Sin cambios reales no se agrega nada.
        ordenUseCase.editar(orden.id(), new EditarOrdenRequest("ALTA", null, null, null, null, null), supervisor);
        assertThat(ordenUseCase.detalle(orden.id()).eventos()).hasSize(2);
    }

    @Test
    void anular_devuelveElHallazgoAAbiertoYPermiteCrearleOtraOrden() {
        EjecucionResponse hallazgo = visita("CON_HALLAZGOS");
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(hallazgo.id()), supervisor);

        assertThrows(BadRequestException.class, () -> ordenUseCase.anular(orden.id(), new ComentarioOrdenRequest(" "), supervisor));
        OrdenResumenResponse anulada = ordenUseCase.anular(orden.id(), new ComentarioOrdenRequest("Lo corrigió el contratista de la obra."), supervisor);

        assertThat(anulada.estado()).isEqualTo("ANULADA");
        assertThat(seguimientoRepository.findByEjecucion_Id(hallazgo.id()).orElseThrow().getEstado()).isEqualTo("ABIERTO");
        assertThat(ordenUseCase.detalle(orden.id()).motivoAnulacion()).isEqualTo("Lo corrigió el contratista de la obra.");
        conEstado(HttpStatus.CONFLICT, () -> ordenUseCase.anular(orden.id(), new ComentarioOrdenRequest("otra vez"), supervisor));
        conEstado(HttpStatus.CONFLICT, () -> ordenUseCase.editar(orden.id(),
                new EditarOrdenRequest("MEDIA", null, null, null, null, null), supervisor));

        assertThat(ordenUseCase.crear(pedido(hallazgo.id()), supervisor).estado()).isEqualTo("ASIGNADA");
    }

    @Test
    void listar_filtraPorEstadoPrioridadYVencidas() {
        OrdenResumenResponse alta = ordenUseCase.crear(pedido(visita("REQUIERE_INTERVENCION").id()), supervisor);
        OrdenResumenResponse media = ordenUseCase.crear(new CrearOrdenRequest(visita("CON_HALLAZGOS").id(), "MEDIA",
                HOY, operarioEntity.getId(), null, correccion.getId(), "Revisar"), supervisor);
        ordenUseCase.anular(media.id(), new ComentarioOrdenRequest("No procede"), supervisor);

        var pagina = PageRequest.of(0, 20);
        assertThat(ordenUseCase.listar(null, null, null, null, null, pagina).getContent()).hasSize(2);
        assertThat(ordenUseCase.listar(List.of("ASIGNADA", "POR_VERIFICAR"), null, null, null, null, pagina).getContent())
                .extracting("id").containsExactly(alta.id());
        assertThat(ordenUseCase.listar(null, null, "MEDIA", null, null, pagina).getContent()).extracting("id").containsExactly(media.id());
        assertThat(ordenUseCase.listar(null, estacion.getId(), null, null, "CIVIL", pagina).getContent()).hasSize(2);
        // Ninguna en curso tiene la fecha límite pasada todavía.
        assertThat(ordenUseCase.listar(null, null, null, true, null, pagina).getContent()).isEmpty();
    }

    @Test
    void listar_soloOrdenaPorCamposDeLaOrden() {
        ordenUseCase.crear(pedido(visita("CON_HALLAZGOS").id()), supervisor);

        assertThat(ordenUseCase.listar(null, null, null, null, null,
                PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("fechaLimite"))).getContent()).hasSize(1);
        assertThrows(BadRequestException.class, () -> ordenUseCase.listar(null, null, null, null, null,
                PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("asignadoA.password"))));
    }

    @Test
    void operarios_soloLosOperariosActivosOrdenadosPorNombre() {
        assertThat(ordenUseCase.operarios()).extracting("nombre").containsSubsequence("Ana Operaria", "Técnico OT")
                .doesNotContain("Operario Inactivo", "Carlos Rivas");
    }

    // ---- SUB-16: la visita del operario ----

    @Test
    void visitaDeLaOrden_laPasaAPorVerificarYQuedaEnLaLineaDeTiempo() {
        EjecucionResponse hallazgo = visita("REQUIERE_INTERVENCION");
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(hallazgo.id()), supervisor);

        EjecucionResponse correccionHecha = visitaDeOrden(orden.id(), estacion.getId(), "CONFORME");

        assertThat(correccionHecha.orden().consecutivo()).isEqualTo("OT-EB-00001");
        assertThat(correccionHecha.esProgramada()).isFalse();
        assertThat(correccionHecha.programacionId()).isNull();
        OrdenDetalleResponse detalle = ordenUseCase.detalle(orden.id());
        assertThat(detalle.orden().estado()).isEqualTo("POR_VERIFICAR");
        assertThat(detalle.ejecucion().id()).isEqualTo(correccionHecha.id());
        assertThat(detalle.eventos()).extracting("tipo").containsExactly("CREADA", "EJECUTADA");
        assertThat(detalle.eventos().get(1).ejecucionId()).isEqualTo(correccionHecha.id());
        // El seguimiento del hallazgo muestra su orden.
        assertThat(ejecucionUseCase.obtenerEjecucion(hallazgo.id()).seguimiento().orden().estado()).isEqualTo("POR_VERIFICAR");
        // Ya no está en la lista del operario.
        assertThat(ordenUseCase.asignadas(operario)).isEmpty();
    }

    @Test
    void visitaDeUnaOrdenQueYaNoEstaAsignada_seGuardaIgualYSeAvisaEnLaLineaDeTiempo() {
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(visita("CON_HALLAZGOS").id()), supervisor);
        ordenUseCase.anular(orden.id(), new ComentarioOrdenRequest("Duplicada"), supervisor);

        EjecucionResponse tarde = visitaDeOrden(orden.id(), estacion.getId(), "CONFORME");

        assertThat(tarde.id()).isNotNull();
        assertThat(tarde.orden().id()).isEqualTo(orden.id());
        OrdenDetalleResponse detalle = ordenUseCase.detalle(orden.id());
        assertThat(detalle.orden().estado()).isEqualTo("ANULADA");
        assertThat(detalle.eventos()).extracting("tipo").endsWith("EJECUCION_FUERA_DE_ESTADO");
    }

    @Test
    void visitaConUnaOrdenInexistenteODeOtraEstacion_seGuardaSinEnlazar() {
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(visita("CON_HALLAZGOS").id()), supervisor);
        EstacionEntity otra = estacionRepository.save(EstacionEntity.builder().nombre("Otra").tipo("BOMBEO")
                .frecuenciaBase("ANUAL").status(true).build());

        assertThat(visitaDeOrden(999_999L, estacion.getId(), "CONFORME").orden()).isNull();
        assertThat(visitaDeOrden(orden.id(), otra.getId(), "CONFORME").orden()).isNull();
        assertThat(ordenUseCase.detalle(orden.id()).orden().estado()).isEqualTo("ASIGNADA");
    }

    @Test
    void visitaDeOtraDisciplina_noEjecutaLaOrden() {
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(visita("CON_HALLAZGOS").id()), supervisor);

        EjecucionResponse electricaHecha = ejecucionUseCase.registrarEjecucion(new EjecucionRequest(
                HOY, 10, 3, estacion.getId(), "ELECTRICO", "CORRECTIVO", "MANTENIMIENTO",
                electrica.getId(), null, null, "CONFORME", "Termografía del tablero", null,
                UUID.randomUUID(), orden.id()), operario);

        assertThat(electricaHecha.orden()).isNull();
        assertThat(ordenUseCase.detalle(orden.id()).orden().estado()).isEqualTo("ASIGNADA");
    }

    @Test
    void lasVisitasDeOrdenesNoSonImprevistosNiCuentanEnLosIndicadores() {
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(visita("CON_HALLAZGOS").id()), supervisor);
        EjecucionResponse deOrden = visitaDeOrden(orden.id(), estacion.getId(), "CONFORME");
        var pagina = PageRequest.of(0, 20);

        var imprevistos = ejecucionUseCase.listarEjecuciones(estacion.getId(), HOY.minusDays(30), HOY, false,
                null, null, null, null, null, null, pagina).getContent();
        assertThat(imprevistos).extracting("id").doesNotContain(deOrden.id()).hasSize(1);
        var deOrdenes = ejecucionUseCase.listarEjecuciones(null, HOY.minusDays(30), HOY, null,
                null, null, null, null, null, null, true, pagina).getContent();
        assertThat(deOrdenes).extracting("id").containsExactly(deOrden.id());

        // estacionId, total, programadas, noProgramadas, …: solo la visita del hallazgo.
        Object[] fila = ejecucionRepository.contarPorEstacion("CIVIL", HOY.minusDays(30), HOY).get(0);
        assertThat(((Number) fila[1]).longValue()).isEqualTo(1);
        assertThat(((Number) fila[3]).longValue()).isEqualTo(1);
    }

    @Test
    void asignadas_soloLasDelOperarioPrimeroLasDePrioridadAlta() {
        OrdenResumenResponse media = ordenUseCase.crear(new CrearOrdenRequest(visita("CON_HALLAZGOS").id(), "MEDIA",
                HOY.plusDays(2), operarioEntity.getId(), null, correccion.getId(), "Revisar"), supervisor);
        OrdenResumenResponse alta = ordenUseCase.crear(pedido(visita("REQUIERE_INTERVENCION").id()), supervisor);
        ordenUseCase.crear(new CrearOrdenRequest(visita("CON_HALLAZGOS").id(), "ALTA", HOY.plusDays(1),
                otroOperario.getId(), null, correccion.getId(), "De Ana"), supervisor);

        var mias = ordenUseCase.asignadas(operario);

        assertThat(mias).extracting(o -> o.orden().id()).containsExactly(alta.id(), media.id());
        assertThat(mias.get(0).evidenciasHallazgo()).isEmpty();
        assertThat(mias.get(0).motivoDevolucion()).isNull();
        assertThat(mias.get(0).orden().instrucciones()).isEqualTo("Sellar la fisura con mortero epóxico.");
    }

    // ---- SUB-17: verificar ----

    private OrdenResumenResponse ordenEjecutada(EjecucionResponse hallazgo) {
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(hallazgo.id()), supervisor);
        visitaDeOrden(orden.id(), estacion.getId(), "CONFORME");
        return orden;
    }

    @Test
    void cerrar_dejaElHallazgoResueltoEnLaVisitaDeLaOrden() {
        EjecucionResponse hallazgo = visita("REQUIERE_INTERVENCION");
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(hallazgo.id()), supervisor);
        conEstado(HttpStatus.CONFLICT, () -> ordenUseCase.cerrar(orden.id(), new ComentarioOrdenRequest("ok"), supervisor));
        EjecucionResponse arreglo = visitaDeOrden(orden.id(), estacion.getId(), "CONFORME");

        assertThrows(BadRequestException.class, () -> ordenUseCase.cerrar(orden.id(), new ComentarioOrdenRequest(""), supervisor));
        OrdenResumenResponse cerrada = ordenUseCase.cerrar(orden.id(), new ComentarioOrdenRequest("Revisado en sitio: quedó sellada."), supervisor);

        assertThat(cerrada.estado()).isEqualTo("CERRADA");
        OrdenDetalleResponse detalle = ordenUseCase.detalle(orden.id());
        assertThat(detalle.verificadoPor()).isEqualTo("Carlos Rivas");
        assertThat(detalle.observacionCierre()).isEqualTo("Revisado en sitio: quedó sellada.");
        var seguimiento = ejecucionUseCase.obtenerEjecucion(hallazgo.id()).seguimiento();
        assertThat(seguimiento.estado()).isEqualTo("RESUELTO");
        assertThat(seguimiento.resueltoEnEjecucionId()).isEqualTo(arreglo.id());
        assertThat(seguimiento.observacionesCierre()).startsWith("OT-EB-00001: ");
        verify(eventos).ordenCambio(eq(estacion.getId()), eq(orden.id()), eq("CERRADA"));
        conEstado(HttpStatus.CONFLICT, () -> ordenUseCase.devolver(orden.id(), new ComentarioOrdenRequest("x"), supervisor));
    }

    @Test
    void devolver_laRegresaAlOperarioConElMotivoYOtraVisitaLaVuelveAPorVerificar() {
        OrdenResumenResponse orden = ordenEjecutada(visita("CON_HALLAZGOS"));

        ordenUseCase.devolver(orden.id(), new ComentarioOrdenRequest("La foto no muestra el muro sellado."), supervisor);

        var mias = ordenUseCase.asignadas(operario);
        assertThat(mias).extracting(o -> o.orden().id()).containsExactly(orden.id());
        assertThat(mias.get(0).motivoDevolucion()).isEqualTo("La foto no muestra el muro sellado.");
        assertThat(ordenUseCase.detalle(orden.id()).ejecucion()).isNull();

        EjecucionResponse segunda = visitaDeOrden(orden.id(), estacion.getId(), "CONFORME");
        OrdenDetalleResponse detalle = ordenUseCase.detalle(orden.id());
        assertThat(detalle.orden().estado()).isEqualTo("POR_VERIFICAR");
        assertThat(detalle.ejecucion().id()).isEqualTo(segunda.id());
        assertThat(detalle.eventos()).extracting("tipo").containsExactly("CREADA", "EJECUTADA", "DEVUELTA", "EJECUTADA");
    }

    @Test
    void conUnaOrdenEnCurso_elHallazgoNoSeResuelveSinElla() {
        EjecucionResponse hallazgo = visita("CON_HALLAZGOS");
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(hallazgo.id()), supervisor);
        var resolver = new ResolverHallazgoRequest("Se arregló", null, true);

        conEstado(HttpStatus.CONFLICT, () -> hallazgoUseCase.resolver(hallazgo.id(), resolver, supervisor));

        ordenUseCase.anular(orden.id(), new ComentarioOrdenRequest("Se arregló en la misma visita"), supervisor);
        assertThat(hallazgoUseCase.resolver(hallazgo.id(), resolver, supervisor).estado()).isEqualTo("RESUELTO");
        verify(eventos).ordenCambio(eq(estacion.getId()), anyLong(), eq("ANULADA"));
    }

    // ---- Para cualquier operario ----

    @Test
    void paraCualquierOperario_laVenTodosYQuedaANombreDeQuienLaEjecuta() {
        OrdenResumenResponse orden = ordenUseCase.crear(new CrearOrdenRequest(visita("CON_HALLAZGOS").id(), "ALTA",
                HOY.plusDays(5), null, true, correccion.getId(), "La toma el que llegue primero"), supervisor);
        assertThat(orden.paraCualquiera()).isTrue();
        assertThat(orden.asignadoAId()).isNull();
        UserPrincipal ana = new UserPrincipal(otroOperario.getId(), otroOperario.getUsername());

        assertThat(ordenUseCase.asignadas(operario)).extracting(o -> o.orden().id()).containsExactly(orden.id());
        assertThat(ordenUseCase.asignadas(ana)).extracting(o -> o.orden().id()).containsExactly(orden.id());

        // La hace Ana: queda a su nombre y deja de aparecerle al otro.
        ejecucionUseCase.registrarEjecucion(new EjecucionRequest(HOY, 10, 3, estacion.getId(), "CIVIL", "CORRECTIVO",
                "MANTENIMIENTO", correccion.getId(), null, null, "CONFORME", "Listo", null, UUID.randomUUID(), orden.id()), ana);
        OrdenDetalleResponse detalle = ordenUseCase.detalle(orden.id());
        assertThat(detalle.orden().asignadoANombre()).isEqualTo("Ana Operaria");
        assertThat(detalle.orden().paraCualquiera()).isFalse();
        assertThat(detalle.eventos().get(1).comentario()).startsWith("Tomada por Ana Operaria.");
        assertThat(ordenUseCase.asignadas(operario)).isEmpty();

        // Si la devuelven, vuelve a quien la hizo.
        ordenUseCase.devolver(orden.id(), new ComentarioOrdenRequest("Falta una foto"), supervisor);
        assertThat(ordenUseCase.asignadas(ana)).hasSize(1);
        assertThat(ordenUseCase.asignadas(operario)).isEmpty();
    }

    @Test
    void paraCualquierOperario_noSeCombinaConUnOperarioYSeLlegaEditando() {
        Long id = visita("CON_HALLAZGOS").id();
        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", HOY.plusDays(5),
                operarioEntity.getId(), true, correccion.getId(), "x"), supervisor));
        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", HOY.plusDays(5),
                null, null, correccion.getId(), "x"), supervisor));

        OrdenResumenResponse orden = ordenUseCase.crear(pedido(id), supervisor);
        OrdenResumenResponse general = ordenUseCase.editar(orden.id(), new EditarOrdenRequest(null, null, null, true, null, null), supervisor);

        assertThat(general.paraCualquiera()).isTrue();
        assertThat(ordenUseCase.detalle(orden.id()).eventos().get(1).comentario())
                .isEqualTo("Reasignada de Técnico OT a cualquier operario");
        assertThat(ordenUseCase.asignadas(new UserPrincipal(otroOperario.getId(), otroOperario.getUsername()))).hasSize(1);
    }

    // ---- Prueba adversarial 2026-10-18 ----

    @Test
    void laFechaLimiteNoPuedePasarDeUnAnio() {
        Long id = visita("CON_HALLAZGOS").id();
        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", HOY.plusDays(366),
                operarioEntity.getId(), null, correccion.getId(), "x"), supervisor));
        assertThat(ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", HOY.plusDays(365), operarioEntity.getId(), null,
                correccion.getId(), "x"), supervisor).fechaLimite()).isEqualTo(HOY.plusDays(365));
    }

    @Test
    void noSePuedePasarAConformeUnHallazgoConOrdenEnCurso_niCambiarLaActividadDeLaVisitaDeLaOrden() {
        EjecucionResponse hallazgo = visita("CON_HALLAZGOS");
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(hallazgo.id()), supervisor);
        var aConforme = new com.app.usochicamochabackend.substation.application.dto.EjecucionEditRequest(
                hallazgo.fecha(), hallazgo.mesEjecucion(), hallazgo.semanaEjecucion(), "PREVENTIVO", "INSPECCION", null,
                "NO_PROGRAMADO", "CONFORME", "Estaba bien", "revisión de rutina", "Me equivoqué al marcar el resultado");

        conEstado(HttpStatus.CONFLICT, () -> ejecucionUseCase.editarEjecucion(hallazgo.id(), aConforme, operario));

        EjecucionResponse visitaOrden = visitaDeOrden(orden.id(), estacion.getId(), "CONFORME");
        var otraActividad = new com.app.usochicamochabackend.substation.application.dto.EjecucionEditRequest(
                visitaOrden.fecha(), visitaOrden.mesEjecucion(), visitaOrden.semanaEjecucion(), "CORRECTIVO", "MANTENIMIENTO",
                null, "NO_PROGRAMADO", "CONFORME", "Otra cosa", "otra cosa distinta", "Cambio la actividad del registro");
        conEstado(HttpStatus.BAD_REQUEST, () -> ejecucionUseCase.editarEjecucion(visitaOrden.id(), otraActividad, operario));

        // Anulada la orden, ya se puede corregir el resultado.
        ordenUseCase.anular(orden.id(), new ComentarioOrdenRequest("No había hallazgo"), supervisor);
        assertThat(ejecucionUseCase.editarEjecucion(hallazgo.id(), aConforme, operario).resultado()).isEqualTo("CONFORME");
    }
}
