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
import com.app.usochicamochabackend.substation.infrastructure.repository.EjecucionRepository;
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
    private final EjecucionRepository ejecucionRepository;

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
        Integer retiradas = activa ? null
                : retirarCitasFuturas(programacionRepository.findByEstacion_IdAndStatusTrueAndEstadoNot(id, ProgramacionEntity.RETIRADA));

        saveActionUseCase.save("El usuario " + usuario.username() + " ha "
                + (activa ? "reactivado" : "desactivado") + " la estación " + estacion.getNombre()
                + (retiradas != null && retiradas > 0 ? " (" + retiradas + " citas futuras quedan para quitar al publicar)" : ""));
        return EstacionResponse.fromEntity(guardada, retiradas);
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

        java.util.Set<Long> usadas = new java.util.HashSet<>(programacionRepository.actividadesProgramadas());
        usadas.addAll(ejecucionRepository.actividadesEjecutadas());
        return actividades.stream()
                .map(a -> ActividadResponse.fromEntity(a, citasPorActividad.getOrDefault(a.getId(), 0),
                        usadas.contains(a.getId()), null))
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
                .nombreCorto(normalizarNombreCorto(request.nombreCorto()))
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

        // Una actividad ya usada no cambia de disciplina: sus citas desaparecerían del móvil
        // (que filtra por disciplina) y sus ejecuciones quedarían en otra.
        if (!actividad.getDisciplina().getId().equals(disciplina.getId()) && enUso(id)) {
            throw conflicto("No se puede cambiar la disciplina: la actividad ya tiene citas o ejecuciones registradas");
        }

        actividad.setNombre(nombre);
        actividad.setDisciplina(disciplina);
        actividad.setCapturaMovilHabilitada(request.capturaMovilHabilitada());
        actividad.setNombreCorto(normalizarNombreCorto(request.nombreCorto()));
        ActividadEntity guardada = actividadRepository.save(actividad);

        saveActionUseCase.save("El usuario " + usuario.username() + " ha editado la actividad " + nombre
                + " (" + disciplina.getCodigo() + ")");
        return ActividadResponse.fromEntity(guardada, citasDelAnio(id), enUso(id), null);
    }

    @Override
    @Transactional
    public ActividadResponse cambiarEstadoActividad(Long id, boolean activa, UserPrincipal usuario) {
        ActividadEntity actividad = buscarActividad(id);
        actividad.setStatus(activa);
        ActividadEntity guardada = actividadRepository.save(actividad);
        Integer retiradas = activa ? null
                : retirarCitasFuturas(programacionRepository.findByActividad_IdAndStatusTrueAndEstadoNot(id, ProgramacionEntity.RETIRADA));

        saveActionUseCase.save("El usuario " + usuario.username() + " ha "
                + (activa ? "reactivado" : "desactivado") + " la actividad " + actividad.getNombre()
                + (retiradas != null && retiradas > 0 ? " (" + retiradas + " citas futuras quedan para quitar al publicar)" : ""));
        return ActividadResponse.fromEntity(guardada, citasDelAnio(id), enUso(id), retiradas);
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

    /**
     * Al desactivar una estación o actividad: sus citas futuras (mes no cerrado) dejan de estar
     * en el cronograma. Las publicadas sin ejecución quedan "se quitará al publicar" (el móvil las
     * deja de ver al publicar); los borradores se descartan. No se tocan citas con ejecución ni
     * de meses cerrados. Devuelve cuántas citas se retiraron.
     */
    private int retirarCitasFuturas(List<ProgramacionEntity> citas) {
        int retiradas = 0;
        for (ProgramacionEntity c : citas) {
            if (calendario.mesCerrado(c.getAnio(), c.getMes()) || c.getPendienteRetiro()
                    || ejecucionRepository.existsByProgramacion_Id(c.getId())) {
                continue;
            }
            if (ProgramacionEntity.BORRADOR.equals(c.getEstado())) {
                c.setStatus(false);
            } else {
                c.setPendienteRetiro(true);
            }
            programacionRepository.save(c);
            retiradas++;
        }
        return retiradas;
    }

    private boolean enUso(Long actividadId) {
        return programacionRepository.existsByActividad_Id(actividadId) || ejecucionRepository.existsByActividad_Id(actividadId);
    }

    private int citasDelAnio(Long actividadId) {
        return programacionRepository
                .countByActividad_IdAndAnioAndStatusTrueAndEstado(actividadId,
                        calendario.anioActual(), ProgramacionEntity.PUBLICADA);
    }

    /** Vacío o solo espacios → null (la web recorta el nombre completo). */
    private String normalizarNombreCorto(String nombreCorto) {
        if (nombreCorto == null || nombreCorto.isBlank()) {
            return null;
        }
        return normalizarNombre(nombreCorto);
    }

    /** Quita espacios al inicio/final y deja uno solo entre palabras ("  Dren   Cuche " → "Dren Cuche"). */
    private String normalizarNombre(String nombre) {
        return nombre.trim().replaceAll("\\s+", " ");
    }

    private ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
