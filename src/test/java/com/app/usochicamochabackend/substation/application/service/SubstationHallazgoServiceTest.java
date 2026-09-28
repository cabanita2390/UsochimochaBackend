package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.substation.application.dto.ResolverHallazgoRequest;
import com.app.usochicamochabackend.substation.application.dto.SeguimientoResponse;
import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EstacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.HallazgoSeguimientoEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.EjecucionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.HallazgoSeguimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubstationHallazgoServiceTest {

    private static final UserPrincipal SUPERVISOR = new UserPrincipal(7L, "supervisor");

    @Mock
    private EjecucionRepository ejecucionRepository;

    @Mock
    private HallazgoSeguimientoRepository hallazgoSeguimientoRepository;

    @Mock
    private UserRepositoryJpa userRepositoryJpa;

    @Mock
    private SaveActionUseCase saveActionUseCase;

    @InjectMocks
    private SubstationHallazgoService service;

    private final EstacionEntity estacion = EstacionEntity.builder().id(1L).nombre("Ayalas").build();
    private final EstacionEntity otraEstacion = EstacionEntity.builder().id(2L).nombre("Cuche").build();
    private EjecucionEntity ejecucion;
    private HallazgoSeguimientoEntity seguimiento;

    @BeforeEach
    void setUp() {
        ejecucion = EjecucionEntity.builder().id(10L).fecha(LocalDate.of(2026, 9, 1)).estacion(estacion)
                .resultado("CON_HALLAZGOS").build();
        seguimiento = HallazgoSeguimientoEntity.builder().id(100L).ejecucion(ejecucion).build();

        when(ejecucionRepository.existsById(10L)).thenReturn(true);
        when(hallazgoSeguimientoRepository.findByEjecucion_Id(10L)).thenReturn(Optional.of(seguimiento));
        when(hallazgoSeguimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userRepositoryJpa.getUserEntityById(7L))
                .thenReturn(UserEntity.builder().id(7L).username("supervisor").fullName("Carlos Rivas").build());
    }

    private ResponseStatusException conEstado(Runnable accion, HttpStatus esperado) {
        var ex = org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class, accion::run);
        assertThat(ex.getStatusCode()).isEqualTo(esperado);
        return ex;
    }

    // ----- Ciclo de estados -----

    @Test
    void cicloCompleto_abiertoEnProcesoResuelto() {
        SeguimientoResponse enProceso = service.marcarEnProceso(10L, SUPERVISOR);
        assertThat(enProceso.estado()).isEqualTo("EN_PROCESO");
        assertThat(enProceso.actualizadoPor()).isEqualTo("Carlos Rivas");
        assertThat(enProceso.cerradoPor()).isNull();

        SeguimientoResponse resuelto = service.resolver(10L,
                new ResolverHallazgoRequest("  Se selló la fisura.  ", null, true), SUPERVISOR);
        assertThat(resuelto.estado()).isEqualTo("RESUELTO");
        assertThat(resuelto.observacionesCierre()).isEqualTo("Se selló la fisura.");
        assertThat(resuelto.cerradoPor()).isEqualTo("Carlos Rivas");
        assertThat(resuelto.cerradoEn()).isNotNull();
        assertThat(resuelto.resueltoMismaVisita()).isTrue();

        verify(saveActionUseCase).save(contains("marcó en proceso el hallazgo de la ejecución #10"));
        verify(saveActionUseCase).save(contains("resolvió el hallazgo de la ejecución #10"));
    }

    @Test
    void abiertoPuedeResolverseDirecto() {
        SeguimientoResponse resuelto = service.resolver(10L, new ResolverHallazgoRequest("Listo.", null, null), SUPERVISOR);

        assertThat(resuelto.estado()).isEqualTo("RESUELTO");
        assertThat(resuelto.resueltoMismaVisita()).isFalse();
    }

    // ----- Validaciones -----

    @Test
    void ejecucionInexistente_404() {
        when(ejecucionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.marcarEnProceso(99L, SUPERVISOR)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.resolver(99L, new ResolverHallazgoRequest("x", null, null), SUPERVISOR))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void ejecucionConforme_sinSeguimiento_409() {
        when(hallazgoSeguimientoRepository.findByEjecucion_Id(10L)).thenReturn(Optional.empty());

        conEstado(() -> service.marcarEnProceso(10L, SUPERVISOR), HttpStatus.CONFLICT);
    }

    @Test
    void ejecucionEditadaAConforme_seguimientoInactivo_409() {
        seguimiento.setStatus(false);

        conEstado(() -> service.resolver(10L, new ResolverHallazgoRequest("x", null, null), SUPERVISOR), HttpStatus.CONFLICT);
    }

    @Test
    void enProcesoSobreResuelto_409() {
        seguimiento.setEstado("RESUELTO");

        conEstado(() -> service.marcarEnProceso(10L, SUPERVISOR), HttpStatus.CONFLICT);
        verify(hallazgoSeguimientoRepository, never()).save(any());
    }

    @Test
    void resolverSobreResuelto_409() {
        seguimiento.setEstado("RESUELTO");

        conEstado(() -> service.resolver(10L, new ResolverHallazgoRequest("x", null, null), SUPERVISOR), HttpStatus.CONFLICT);
    }

    @Test
    void mismaVisitaYEjecucionALaVez_400() {
        conEstado(() -> service.resolver(10L, new ResolverHallazgoRequest("x", 11L, true), SUPERVISOR), HttpStatus.BAD_REQUEST);
        verify(hallazgoSeguimientoRepository, never()).save(any());
    }

    @Test
    void resueltoEnEjecucionInexistente_400() {
        when(ejecucionRepository.findById(11L)).thenReturn(Optional.empty());

        conEstado(() -> service.resolver(10L, new ResolverHallazgoRequest("x", 11L, false), SUPERVISOR), HttpStatus.BAD_REQUEST);
    }

    @Test
    void resueltoEnEjecucionDeOtraEstacion_400() {
        when(ejecucionRepository.findById(11L)).thenReturn(Optional.of(
                EjecucionEntity.builder().id(11L).fecha(LocalDate.of(2026, 9, 20)).estacion(otraEstacion).build()));

        conEstado(() -> service.resolver(10L, new ResolverHallazgoRequest("x", 11L, false), SUPERVISOR), HttpStatus.BAD_REQUEST);
    }

    @Test
    void resueltoEnEjecucionDeLaMismaFechaOAnterior_400() {
        when(ejecucionRepository.findById(11L)).thenReturn(Optional.of(
                EjecucionEntity.builder().id(11L).fecha(LocalDate.of(2026, 9, 1)).estacion(estacion).build()));
        when(ejecucionRepository.findById(12L)).thenReturn(Optional.of(
                EjecucionEntity.builder().id(12L).fecha(LocalDate.of(2026, 8, 15)).estacion(estacion).build()));

        conEstado(() -> service.resolver(10L, new ResolverHallazgoRequest("x", 11L, false), SUPERVISOR), HttpStatus.BAD_REQUEST);
        conEstado(() -> service.resolver(10L, new ResolverHallazgoRequest("x", 12L, false), SUPERVISOR), HttpStatus.BAD_REQUEST);
    }

    @Test
    void resueltoEnEjecucionPosteriorDeLaMismaEstacion_ok() {
        when(ejecucionRepository.findById(11L)).thenReturn(Optional.of(
                EjecucionEntity.builder().id(11L).fecha(LocalDate.of(2026, 9, 20)).estacion(estacion).build()));

        SeguimientoResponse resuelto = service.resolver(10L, new ResolverHallazgoRequest("x", 11L, false), SUPERVISOR);

        assertThat(resuelto.resueltoEnEjecucionId()).isEqualTo(11L);
        assertThat(resuelto.resueltoEnEjecucionFecha()).isEqualTo(LocalDate.of(2026, 9, 20));
    }
}
