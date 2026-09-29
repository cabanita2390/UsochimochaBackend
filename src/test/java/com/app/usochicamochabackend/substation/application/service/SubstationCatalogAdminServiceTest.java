package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.substation.application.dto.ActividadRequest;
import com.app.usochicamochabackend.substation.application.dto.ActividadResponse;
import com.app.usochicamochabackend.substation.application.dto.EstacionRequest;
import com.app.usochicamochabackend.substation.application.dto.EstacionResponse;
import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EstacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ActividadRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.DisciplinaRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ProgramacionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EstacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubstationCatalogAdminServiceTest {

    private static final UserPrincipal ADMIN = new UserPrincipal(1L, "admin");

    @Mock
    private EstacionRepository estacionRepository;

    @Mock
    private ActividadRepository actividadRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private ProgramacionRepository programacionRepository;

    @Mock
    private SaveActionUseCase saveActionUseCase;

    @InjectMocks
    private SubstationCatalogAdminService service;

    private EstacionEntity estacion(Long id, String nombre, boolean activa) {
        return EstacionEntity.builder().id(id).nombre(nombre).tipo("BOMBEO").frecuenciaBase("TRIMESTRAL").status(activa).build();
    }

    @Test
    void listar_porDefectoSoloActivas_conFlagTodas() {
        when(estacionRepository.findByStatusTrueOrderByNombreAsc()).thenReturn(List.of(estacion(1L, "Ayalas", true)));
        when(estacionRepository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(estacion(1L, "Ayalas", true), estacion(2L, "Cuche", false)));

        assertThat(service.listarEstaciones(false)).hasSize(1);
        List<EstacionResponse> todas = service.listarEstaciones(true);
        assertThat(todas).hasSize(2);
        assertThat(todas.get(1).activa()).isFalse();
    }

    @Test
    void crear_normalizaNombre_guardaYAudita() {
        when(estacionRepository.existsByNombreIgnoreCase("Dren Cuche")).thenReturn(false);
        when(estacionRepository.save(any())).thenAnswer(inv -> {
            EstacionEntity e = inv.getArgument(0);
            e.setId(10L);
            return e;
        });

        EstacionResponse creada = service.crearEstacion(
                new EstacionRequest("  Dren   Cuche ", "BOMBEO", "MENSUAL"), ADMIN);

        ArgumentCaptor<EstacionEntity> guardada = ArgumentCaptor.forClass(EstacionEntity.class);
        verify(estacionRepository).save(guardada.capture());
        assertThat(guardada.getValue().getNombre()).isEqualTo("Dren Cuche");
        assertThat(guardada.getValue().getStatus()).isTrue();
        assertThat(creada.id()).isEqualTo(10L);
        assertThat(creada.frecuenciaBase()).isEqualTo("MENSUAL");
        verify(saveActionUseCase).save(contains("creado la estación Dren Cuche"));
    }

    @Test
    void crear_nombreDuplicado_responde409YNoGuarda() {
        when(estacionRepository.existsByNombreIgnoreCase("Ayalas")).thenReturn(true);

        assertThatThrownBy(() -> service.crearEstacion(new EstacionRequest("Ayalas", "BOMBEO", "TRIMESTRAL"), ADMIN))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(estacionRepository, never()).save(any());
        verify(saveActionUseCase, never()).save(any());
    }

    @Test
    void actualizar_mismoNombreDeLaPropiaEstacion_noEsDuplicado() {
        EstacionEntity actual = estacion(3L, "Holanda", true);
        when(estacionRepository.findById(3L)).thenReturn(Optional.of(actual));
        when(estacionRepository.existsByNombreIgnoreCaseAndIdNot("Holanda", 3L)).thenReturn(false);
        when(estacionRepository.save(actual)).thenReturn(actual);

        EstacionResponse editada = service.actualizarEstacion(3L,
                new EstacionRequest("Holanda", "COMPLEMENTARIA", "ANUAL"), ADMIN);

        assertThat(editada.tipo()).isEqualTo("COMPLEMENTARIA");
        assertThat(editada.frecuenciaBase()).isEqualTo("ANUAL");
        verify(saveActionUseCase).save(contains("editado la estación Holanda"));
    }

    @Test
    void actualizar_nombreDeOtraEstacion_responde409() {
        when(estacionRepository.findById(3L)).thenReturn(Optional.of(estacion(3L, "Holanda", true)));
        when(estacionRepository.existsByNombreIgnoreCaseAndIdNot("Ayalas", 3L)).thenReturn(true);

        assertThatThrownBy(() -> service.actualizarEstacion(3L, new EstacionRequest("Ayalas", "BOMBEO", "TRIMESTRAL"), ADMIN))
                .isInstanceOf(ResponseStatusException.class);
        verify(estacionRepository, never()).save(any());
    }

    @Test
    void cambiarEstado_desactivaYReactiva() {
        EstacionEntity actual = estacion(3L, "Holanda", true);
        when(estacionRepository.findById(3L)).thenReturn(Optional.of(actual));
        when(estacionRepository.save(actual)).thenReturn(actual);

        assertThat(service.cambiarEstadoEstacion(3L, false, ADMIN).activa()).isFalse();
        verify(saveActionUseCase).save(contains("desactivado la estación Holanda"));

        assertThat(service.cambiarEstadoEstacion(3L, true, ADMIN).activa()).isTrue();
        verify(saveActionUseCase).save(contains("reactivado la estación Holanda"));
    }

    @Test
    void estacionInexistente_responde404() {
        when(estacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cambiarEstadoEstacion(99L, false, ADMIN))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ----- Actividades -----

    private static final DisciplinaEntity CIVIL = DisciplinaEntity.builder().id(1L).codigo("CIVIL").build();

    private ActividadEntity actividad(Long id, String nombre, boolean capturable, boolean activa) {
        return ActividadEntity.builder().id(id).nombre(nombre).disciplina(CIVIL)
                .capturaMovilHabilitada(capturable).status(activa).build();
    }

    @Test
    void listarActividades_porDefectoFiltroDelMovil_yAgregaCitasDelAnio() {
        when(actividadRepository.findByDisciplina_CodigoAndCapturaMovilHabilitadaTrueAndStatusTrueOrderByNombreAsc("CIVIL"))
                .thenReturn(List.of(actividad(1L, "Pintura", true, true), actividad(2L, "Compuertas", true, true)));
        when(programacionRepository.contarCitasPorActividad(anyInt()))
                .thenReturn(List.<Object[]>of(new Object[]{1L, 12L}));

        List<ActividadResponse> actividades = service.listarActividades("CIVIL", false);

        assertThat(actividades).extracting(ActividadResponse::citasPublicadasAnio).containsExactly(12, 0);
    }

    @Test
    void listarActividades_conInactivasYSinDisciplina_devuelveTodas() {
        when(actividadRepository.findAllByOrderByNombreAsc())
                .thenReturn(List.of(actividad(1L, "Pintura", false, false), actividad(2L, "Compuertas", true, true)));
        when(programacionRepository.contarCitasPorActividad(anyInt())).thenReturn(List.of());

        List<ActividadResponse> actividades = service.listarActividades(null, true);

        assertThat(actividades).hasSize(2);
        assertThat(actividades.get(0).activa()).isFalse();
        assertThat(actividades.get(0).capturaMovilHabilitada()).isFalse();
    }

    @Test
    void crearActividad_normalizaNombre_guardaYAudita() {
        when(disciplinaRepository.findByCodigo("CIVIL")).thenReturn(Optional.of(CIVIL));
        when(actividadRepository.existsByNombreIgnoreCaseAndDisciplina_Id("Pintura muros", 1L)).thenReturn(false);
        when(actividadRepository.save(any())).thenAnswer(inv -> {
            ActividadEntity a = inv.getArgument(0);
            a.setId(20L);
            return a;
        });

        ActividadResponse creada = service.crearActividad(new ActividadRequest(" Pintura  muros ", "CIVIL", true), ADMIN);

        assertThat(creada.id()).isEqualTo(20L);
        assertThat(creada.nombre()).isEqualTo("Pintura muros");
        assertThat(creada.disciplina()).isEqualTo("CIVIL");
        assertThat(creada.activa()).isTrue();
        assertThat(creada.citasPublicadasAnio()).isZero();
        verify(saveActionUseCase).save(contains("creado la actividad Pintura muros (CIVIL)"));
    }

    @Test
    void crearActividad_disciplinaInexistente_responde400() {
        when(disciplinaRepository.findByCodigo("NUCLEAR")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crearActividad(new ActividadRequest("X", "NUCLEAR", true), ADMIN))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(actividadRepository, never()).save(any());
    }

    @Test
    void crearActividad_duplicadaEnLaMismaDisciplina_responde409() {
        when(disciplinaRepository.findByCodigo("CIVIL")).thenReturn(Optional.of(CIVIL));
        when(actividadRepository.existsByNombreIgnoreCaseAndDisciplina_Id("Pintura", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.crearActividad(new ActividadRequest("Pintura", "CIVIL", true), ADMIN))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(actividadRepository, never()).save(any());
    }

    @Test
    void actualizarActividad_cambiaCapturaYDevuelveSusCitas() {
        ActividadEntity actual = actividad(5L, "Pintura", true, true);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actual));
        when(disciplinaRepository.findByCodigo("CIVIL")).thenReturn(Optional.of(CIVIL));
        when(actividadRepository.existsByNombreIgnoreCaseAndDisciplina_IdAndIdNot("Pintura", 1L, 5L)).thenReturn(false);
        when(actividadRepository.save(actual)).thenReturn(actual);
        when(programacionRepository.countByActividad_IdAndAnioAndStatusTrueAndEstado(eq(5L), anyInt(), eq("PUBLICADA"))).thenReturn(12);

        ActividadResponse editada = service.actualizarActividad(5L, new ActividadRequest("Pintura", "CIVIL", false), ADMIN);

        assertThat(editada.capturaMovilHabilitada()).isFalse();
        assertThat(editada.citasPublicadasAnio()).isEqualTo(12);
        verify(saveActionUseCase).save(contains("editado la actividad Pintura"));
    }

    @Test
    void cambiarEstadoActividad_desactiva() {
        ActividadEntity actual = actividad(5L, "Pintura", true, true);
        when(actividadRepository.findById(5L)).thenReturn(Optional.of(actual));
        when(actividadRepository.save(actual)).thenReturn(actual);

        assertThat(service.cambiarEstadoActividad(5L, false, ADMIN).activa()).isFalse();
        verify(saveActionUseCase).save(contains("desactivado la actividad Pintura"));
    }
}
