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
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationEventosPort;
import com.app.usochicamochabackend.substation.application.port.SubstationOrdenUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EstacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ActividadRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.DisciplinaRepository;
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

    private CrearOrdenRequest pedido(Long ejecucionId) {
        return new CrearOrdenRequest(ejecucionId, "ALTA", HOY.plusDays(7), operarioEntity.getId(), correccion.getId(),
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
        assertThat(detalle.totalCostos()).isZero();

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

        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "URGENTE", plazo, op, act, "x"), supervisor));
        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", HOY.minusDays(1), op, act, "x"), supervisor));
        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", plazo, supervisor.id(), act, "x"), supervisor));
        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", plazo, op, electrica.getId(), "x"), supervisor));
        assertThrows(BadRequestException.class, () -> ordenUseCase.crear(new CrearOrdenRequest(id, "ALTA", plazo, op, act, "  "), supervisor));
        // Hoy mismo sí vale como fecha límite.
        assertThat(ordenUseCase.crear(new CrearOrdenRequest(id, "MEDIA", HOY, op, act, "x"), supervisor).prioridad()).isEqualTo("MEDIA");
    }

    @Test
    void editar_reasignaYCambiaElPlazoDejandoLosCambiosEnLaLineaDeTiempo() {
        OrdenResumenResponse orden = ordenUseCase.crear(pedido(visita("CON_HALLAZGOS").id()), supervisor);

        OrdenResumenResponse editada = ordenUseCase.editar(orden.id(),
                new EditarOrdenRequest(null, HOY.plusDays(20), otroOperario.getId(), null, null), supervisor);

        assertThat(editada.asignadoANombre()).isEqualTo("Ana Operaria");
        assertThat(editada.fechaLimite()).isEqualTo(HOY.plusDays(20));
        var eventos = ordenUseCase.detalle(orden.id()).eventos();
        assertThat(eventos).extracting("tipo").containsExactly("CREADA", "EDITADA");
        assertThat(eventos.get(1).comentario()).contains("Reasignada de Técnico OT a Ana Operaria").contains("22/10/2026 → 04/11/2026");

        // Sin cambios reales no se agrega nada.
        ordenUseCase.editar(orden.id(), new EditarOrdenRequest("ALTA", null, null, null, null), supervisor);
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
                new EditarOrdenRequest("MEDIA", null, null, null, null), supervisor));

        assertThat(ordenUseCase.crear(pedido(hallazgo.id()), supervisor).estado()).isEqualTo("ASIGNADA");
    }

    @Test
    void listar_filtraPorEstadoPrioridadYVencidas() {
        OrdenResumenResponse alta = ordenUseCase.crear(pedido(visita("REQUIERE_INTERVENCION").id()), supervisor);
        OrdenResumenResponse media = ordenUseCase.crear(new CrearOrdenRequest(visita("CON_HALLAZGOS").id(), "MEDIA",
                HOY, operarioEntity.getId(), correccion.getId(), "Revisar"), supervisor);
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
    void operarios_soloLosOperariosActivosOrdenadosPorNombre() {
        assertThat(ordenUseCase.operarios()).extracting("nombre").containsSubsequence("Ana Operaria", "Técnico OT")
                .doesNotContain("Operario Inactivo", "Carlos Rivas");
    }
}
