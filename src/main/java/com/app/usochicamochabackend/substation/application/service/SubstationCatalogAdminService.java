package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.substation.application.dto.ActividadRequest;
import com.app.usochicamochabackend.substation.application.dto.ActividadResponse;
import com.app.usochicamochabackend.substation.application.dto.EstacionRequest;
import com.app.usochicamochabackend.substation.application.dto.EstacionResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationCatalogAdminUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EstacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ProgramacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ActividadRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.DisciplinaRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EstacionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ProgramacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubstationCatalogAdminService implements SubstationCatalogAdminUseCase {

    private final EstacionRepository estacionRepository;
    private final ActividadRepository actividadRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final ProgramacionRepository programacionRepository;
    private final SaveActionUseCase saveActionUseCase;
    private final CalendarioMantenimiento calendario;

    // ---------------------------------------------------------------------
    // Estaciones
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<EstacionResponse> listarEstaciones(boolean incluirInactivas) {
        List<EstacionEntity> estaciones = incluirInactivas
                ? estacionRepository.findAllByOrderByNombreAsc()
                : estacionRepository.findByStatusTrueOrderByNombreAsc();
        return estaciones.stream()
                .map(EstacionResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public EstacionResponse crearEstacion(EstacionRequest request, UserPrincipal usuario) {
        String nombre = normalizarNombre(request.nombre());
        if (estacionRepository.existsByNombreIgnoreCase(nombre)) {
            throw conflicto("Ya existe una estación con el nombre \"" + nombre + "\"");
        }

        EstacionEntity guardada = estacionRepository.save(EstacionEntity.builder()
                .nombre(nombre)
                .tipo(request.tipo())
                .frecuenciaBase(request.frecuenciaBase())
                .build());

        saveActionUseCase.save("El usuario " + usuario.username() + " ha creado la estación " + nombre);
        return EstacionResponse.fromEntity(guardada);
    }

    @Override
    @Transactional
    public EstacionResponse actualizarEstacion(Long id, EstacionRequest request, UserPrincipal usuario) {
        EstacionEntity estacion = buscarEstacion(id);
        String nombre = normalizarNombre(request.nombre());
        if (estacionRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw conflicto("Ya existe una estación con el nombre \"" + nombre + "\"");
        }

        estacion.setNombre(nombre);
        estacion.setTipo(request.tipo());
        estacion.setFrecuenciaBase(request.frecuenciaBase());
        EstacionEntity guardada = estacionRepository.save(estacion);

        saveActionUseCase.save("El usuario " + usuario.username() + " ha editado la estación " + nombre);
        return EstacionResponse.fromEntity(guardada);
    }

    @Override
    @Transactional
    public EstacionResponse cambiarEstadoEstacion(Long id, boolean activa, UserPrincipal usuario) {
        EstacionEntity estacion = buscarEstacion(id);
        estacion.setStatus(activa);
        EstacionEntity guardada = estacionRepository.save(estacion);

        saveActionUseCase.save("El usuario " + usuario.username() + " ha "
                + (activa ? "reactivado" : "desactivado") + " la estación " + estacion.getNombre());
        return EstacionResponse.fromEntity(guardada);
    }

    // ---------------------------------------------------------------------
    // Actividades
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<ActividadResponse> listarActividades(String disciplina, boolean incluirInactivas) {
        List<ActividadEntity> actividades;
        if (incluirInactivas) {
            actividades = disciplina == null
                    ? actividadRepository.findAllByOrderByNombreAsc()
                    : actividadRepository.findByDisciplina_CodigoOrderByNombreAsc(disciplina);
        } else {
            actividades = disciplina == null
                    ? actividadRepository.findByCapturaMovilHabilitadaTrueAndStatusTrueOrderByNombreAsc()
                    : actividadRepository.findByDisciplina_CodigoAndCapturaMovilHabilitadaTrueAndStatusTrueOrderByNombreAsc(disciplina);
        }

        Map<Long, Integer> citasPorActividad = programacionRepository
                .contarCitasPorActividad(calendario.anioActual()).stream()
                .collect(Collectors.toMap(fila -> (Long) fila[0], fila -> ((Long) fila[1]).intValue()));

        return actividades.stream()
                .map(a -> ActividadResponse.fromEntity(a, citasPorActividad.getOrDefault(a.getId(), 0)))
                .toList();
    }

    @Override
    @Transactional
    public ActividadResponse crearActividad(ActividadRequest request, UserPrincipal usuario) {
        DisciplinaEntity disciplina = buscarDisciplina(request.disciplina());
        String nombre = normalizarNombre(request.nombre());
        if (actividadRepository.existsByNombreIgnoreCaseAndDisciplina_Id(nombre, disciplina.getId())) {
            throw conflicto("Ya existe la actividad \"" + nombre + "\" en " + disciplina.getCodigo());
        }

        ActividadEntity guardada = actividadRepository.save(ActividadEntity.builder()
                .nombre(nombre)
                .disciplina(disciplina)
                .capturaMovilHabilitada(request.capturaMovilHabilitada())
                .build());

        saveActionUseCase.save("El usuario " + usuario.username() + " ha creado la actividad " + nombre
                + " (" + disciplina.getCodigo() + ")");
        // Recién creada: todavía no tiene citas.
        return ActividadResponse.fromEntity(guardada, 0);
    }

    @Override
    @Transactional
    public ActividadResponse actualizarActividad(Long id, ActividadRequest request, UserPrincipal usuario) {
        ActividadEntity actividad = buscarActividad(id);
        DisciplinaEntity disciplina = buscarDisciplina(request.disciplina());
        String nombre = normalizarNombre(request.nombre());
        if (actividadRepository.existsByNombreIgnoreCaseAndDisciplina_IdAndIdNot(nombre, disciplina.getId(), id)) {
            throw conflicto("Ya existe la actividad \"" + nombre + "\" en " + disciplina.getCodigo());
        }

        actividad.setNombre(nombre);
        actividad.setDisciplina(disciplina);
        actividad.setCapturaMovilHabilitada(request.capturaMovilHabilitada());
        ActividadEntity guardada = actividadRepository.save(actividad);

        saveActionUseCase.save("El usuario " + usuario.username() + " ha editado la actividad " + nombre
                + " (" + disciplina.getCodigo() + ")");
        return ActividadResponse.fromEntity(guardada, citasDelAnio(id));
    }

    @Override
    @Transactional
    public ActividadResponse cambiarEstadoActividad(Long id, boolean activa, UserPrincipal usuario) {
        ActividadEntity actividad = buscarActividad(id);
        actividad.setStatus(activa);
        ActividadEntity guardada = actividadRepository.save(actividad);

        saveActionUseCase.save("El usuario " + usuario.username() + " ha "
                + (activa ? "reactivado" : "desactivado") + " la actividad " + actividad.getNombre());
        return ActividadResponse.fromEntity(guardada, citasDelAnio(id));
    }

    // ---------------------------------------------------------------------
    // Auxiliares
    // ---------------------------------------------------------------------

    private EstacionEntity buscarEstacion(Long id) {
        return estacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estación no encontrada: " + id));
    }

    private ActividadEntity buscarActividad(Long id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: " + id));
    }

    private DisciplinaEntity buscarDisciplina(String codigo) {
        return disciplinaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Disciplina inexistente: " + codigo));
    }

    private int citasDelAnio(Long actividadId) {
        return programacionRepository
                .countByActividad_IdAndAnioAndStatusTrueAndEstado(actividadId,
                        calendario.anioActual(), ProgramacionEntity.PUBLICADA);
    }

    /** Quita espacios al inicio/final y deja uno solo entre palabras ("  Dren   Cuche " → "Dren Cuche"). */
    private String normalizarNombre(String nombre) {
        return nombre.trim().replaceAll("\\s+", " ");
    }

    private ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
