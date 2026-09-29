package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.BadRequestException;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.substation.application.dto.*;
import com.app.usochicamochabackend.substation.application.port.SubstationCatalogUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationIndicadoresUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionEdicionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EvidenciaEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.HallazgoSeguimientoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ProgramacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ActividadRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.CumplimientoViewRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.DisciplinaRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EjecucionEdicionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EjecucionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EjecucionSpecifications;
import com.app.usochicamochabackend.substation.infrastructure.repository.EstacionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EvidenciaRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.HallazgoSeguimientoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.IndicadorEstacionViewRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ProgramacionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ResumenActividadViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubstationService implements SubstationCatalogUseCase, SubstationEjecucionUseCase, SubstationIndicadoresUseCase {

    private final EstacionRepository estacionRepository;
    private final ActividadRepository actividadRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final ProgramacionRepository programacionRepository;
    private final EjecucionRepository ejecucionRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final UserRepositoryJpa userRepositoryJpa;
    private final EvidenciaStorageService evidenciaStorageService;
    private final SaveActionUseCase saveActionUseCase;
    private final CumplimientoViewRepository cumplimientoViewRepository;
    private final IndicadorEstacionViewRepository indicadorEstacionViewRepository;
    private final ResumenActividadViewRepository resumenActividadViewRepository;
    private final EjecucionEdicionRepository ejecucionEdicionRepository;
    private final HallazgoSeguimientoRepository hallazgoSeguimientoRepository;

    private static final Set<String> TIPO_MANTENIMIENTO_VALIDOS =
            Set.of("PREVENTIVO", "CORRECTIVO", "PREDICTIVO", "NO_PROGRAMADO");
    private static final Set<String> RESULTADO_VALIDOS =
            Set.of("CONFORME", "CON_HALLAZGOS", "REQUIERE_INTERVENCION");
    private static final Set<String> TIPO_ACTIVIDAD_VALIDOS =
            Set.of("INSPECCION", "MANTENIMIENTO", "NO_PROGRAMADO", "OTRO");
    private static final Set<String> MOTIVO_NO_CATALOGADO_VALIDOS =
            Set.of("NO_PROGRAMADO", "OTRO");
    private static final Set<String> TIPO_ACTIVIDAD_VALIDOS_CIVIL =
            Set.of("INSPECCION", "MANTENIMIENTO", "NO_PROGRAMADO");
    private static final Set<String> MOTIVO_NO_CATALOGADO_VALIDOS_CIVIL =
            Set.of("NO_PROGRAMADO");



    @Override
    public List<ProgramacionResponse> listarProgramacion(Long estacionId, Integer anio, Integer mes, String disciplina) {
        return programacionRepository
                .findByEstacion_IdAndAnioAndMesAndActividad_Disciplina_CodigoAndStatusTrue(estacionId, anio, mes, disciplina)
                .stream()
                .map(ProgramacionResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public EjecucionResponse registrarEjecucion(EjecucionRequest request, UserPrincipal usuario) {
        var existente = ejecucionRepository.findByUuidCliente(request.uuidCliente());
        if (existente.isPresent()) {
            return toResponse(existente.get());
        }

        validarCoherencia(request.disciplina(), request.tipoMantenimiento(), request.tipoActividad(),
                request.actividadId(), request.motivoNoCatalogado(), request.resultado(),
                request.observaciones(), request.descripcionLibre());

        var estacion = estacionRepository.findById(request.estacionId())
                .orElseThrow(() -> new ResourceNotFoundException("Estación no encontrada: id=" + request.estacionId()));

        DisciplinaEntity disciplina = disciplinaRepository.findByCodigo(request.disciplina())
                .orElseThrow(() -> new BadRequestException("Disciplina desconocida: " + request.disciplina()));

        ActividadEntity actividad = null;
        ProgramacionEntity programacion = null;
        if (request.actividadId() != null) {
            actividad = actividadRepository.findById(request.actividadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: id=" + request.actividadId()));
        }
        if (request.programacionId() != null) {
            programacion = programacionRepository.findById(request.programacionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cita de programación no encontrada: id=" + request.programacionId()));
        }

        UserEntity usuarioEntity = userRepositoryJpa.getUserEntityById(usuario.id());

        EjecucionEntity entity = EjecucionEntity.builder()
                .fecha(request.fecha())
                .mesEjecucion(request.mesEjecucion())
                .semanaEjecucion(request.semanaEjecucion())
                .usuario(usuarioEntity)
                .estacion(estacion)
                .disciplina(disciplina)
                .tipoMantenimiento(request.tipoMantenimiento())
                .tipoActividad(request.tipoActividad())
                .actividad(actividad)
                .programacion(programacion)
                .esProgramada(programacion != null)
                .motivoNoCatalogado(actividad == null ? request.motivoNoCatalogado() : null)
                .resultado(request.resultado())
                .observaciones(request.observaciones())
                .descripcionLibre(actividad == null ? request.descripcionLibre() : null)
                .uuidCliente(request.uuidCliente())
                .build();

        EjecucionEntity guardada = ejecucionRepository.save(entity);

        // todo hallazgo arranca con seguimiento ABIERTO, en la misma transacción.
        // Inserción trivial y sin validaciones a propósito: si este POST respondiera error, el
        // móvil marcaría la ejecución como fallida en su cola. Los reintentos con el mismo
        // uuidCliente no llegan aquí (salen por el return temprano de arriba).
        if (!"CONFORME".equals(guardada.getResultado())) {
            hallazgoSeguimientoRepository.save(HallazgoSeguimientoEntity.builder()
                    .ejecucion(guardada)
                    .actualizadoPor(usuarioEntity)
                    .build());
        }

        saveActionUseCase.save("El usuario " + usuarioEntity.getUsername()
                + " ha registrado una ejecución de mantenimiento en la estación " + estacion.getNombre());

        return toResponse(guardada);
    }

    /**
     * Compara el registro guardado con lo que llega en la edición, campo por campo, con los
     * mismos valores que quedarían guardados (motivoNoCatalogado y descripcionLibre solo aplican
     * sin actividad). La actividad se guarda por nombre para que el historial se lea sin
     * consultar el catálogo.
     */
    private List<CambioCampo> calcularCambios(EjecucionEntity actual, EjecucionEditRequest request,
            ActividadEntity nuevaActividad) {
        List<CambioCampo> cambios = new ArrayList<>();
        agregarSiCambio(cambios, "fecha", actual.getFecha(), request.fecha());
        agregarSiCambio(cambios, "mesEjecucion", actual.getMesEjecucion(), request.mesEjecucion());
        agregarSiCambio(cambios, "semanaEjecucion", actual.getSemanaEjecucion(), request.semanaEjecucion());
        agregarSiCambio(cambios, "tipoMantenimiento", actual.getTipoMantenimiento(), request.tipoMantenimiento());
        agregarSiCambio(cambios, "tipoActividad", actual.getTipoActividad(), request.tipoActividad());
        agregarSiCambio(cambios, "actividad",
                actual.getActividad() != null ? actual.getActividad().getNombre() : null,
                nuevaActividad != null ? nuevaActividad.getNombre() : null);
        agregarSiCambio(cambios, "motivoNoCatalogado", actual.getMotivoNoCatalogado(),
                nuevaActividad == null ? request.motivoNoCatalogado() : null);
        agregarSiCambio(cambios, "resultado", actual.getResultado(), request.resultado());
        agregarSiCambio(cambios, "observaciones", actual.getObservaciones(), request.observaciones());
        agregarSiCambio(cambios, "descripcionLibre", actual.getDescripcionLibre(),
                nuevaActividad == null ? request.descripcionLibre() : null);
        return cambios;
    }

    private void agregarSiCambio(List<CambioCampo> cambios, String campo, Object antes, Object despues) {
        if (!Objects.equals(antes, despues)) {
            cambios.add(new CambioCampo(campo,
                    antes != null ? antes.toString() : null,
                    despues != null ? despues.toString() : null));
        }
    }

    /**
     * mantiene el seguimiento alineado con el resultado editado.
     * hallazgo → CONFORME: se desactiva. CONFORME → hallazgo: se crea, o se reactiva en ABIERTO
     * con los datos de cierre limpios. hallazgo → hallazgo: no cambia.
     */
    private void sincronizarSeguimiento(EjecucionEntity ejecucion, String resultadoAnterior, UserEntity usuario) {
        boolean teniaHallazgo = !"CONFORME".equals(resultadoAnterior);
        boolean tieneHallazgo = !"CONFORME".equals(ejecucion.getResultado());
        if (teniaHallazgo == tieneHallazgo) {
            return;
        }

        var existente = hallazgoSeguimientoRepository.findByEjecucion_Id(ejecucion.getId());
        if (!tieneHallazgo) {
            existente.ifPresent(seguimiento -> {
                seguimiento.setStatus(false);
                seguimiento.setActualizadoPor(usuario);
                seguimiento.setActualizadoEn(LocalDateTime.now());
                hallazgoSeguimientoRepository.save(seguimiento);
            });
            return;
        }

        HallazgoSeguimientoEntity seguimiento = existente.orElseGet(() ->
                HallazgoSeguimientoEntity.builder().ejecucion(ejecucion).build());
        seguimiento.setEstado("ABIERTO");
        seguimiento.setStatus(true);
        seguimiento.setResueltoEnEjecucion(null);
        seguimiento.setResueltoMismaVisita(false);
        seguimiento.setObservacionesCierre(null);
        seguimiento.setCerradoPor(null);
        seguimiento.setCerradoEn(null);
        seguimiento.setActualizadoPor(usuario);
        seguimiento.setActualizadoEn(LocalDateTime.now());
        hallazgoSeguimientoRepository.save(seguimiento);
    }

    private void validarCoherencia(String disciplina, String tipoMantenimiento, String tipoActividad,
            Long actividadId, String motivoNoCatalogado, String resultado, String observaciones,
            String descripcionLibre) {
        if (!TIPO_MANTENIMIENTO_VALIDOS.contains(tipoMantenimiento)) {
            throw new BadRequestException("tipoMantenimiento inválido: " + tipoMantenimiento);
        }
        if (!RESULTADO_VALIDOS.contains(resultado)) {
            throw new BadRequestException("resultado inválido: " + resultado);
        }
        Set<String> tipoActividadPermitidos =
                "CIVIL".equals(disciplina) ? TIPO_ACTIVIDAD_VALIDOS_CIVIL : TIPO_ACTIVIDAD_VALIDOS;
        if (!tipoActividadPermitidos.contains(tipoActividad)) {
            throw new BadRequestException(
                    "tipoActividad inválido para disciplina " + disciplina + ": " + tipoActividad);
        }
        if (observaciones == null || observaciones.isBlank()) {
            throw new BadRequestException("observaciones es obligatorio.");
        }

        boolean tieneActividad = actividadId != null;
        if (!tieneActividad) {
            if (motivoNoCatalogado == null) {
                throw new BadRequestException("motivoNoCatalogado es obligatorio cuando actividadId es null.");
            }
            Set<String> motivoPermitidos =
                    "CIVIL".equals(disciplina) ? MOTIVO_NO_CATALOGADO_VALIDOS_CIVIL : MOTIVO_NO_CATALOGADO_VALIDOS;
            if (!motivoPermitidos.contains(motivoNoCatalogado)) {
                throw new BadRequestException(
                        "motivoNoCatalogado inválido para disciplina " + disciplina + ": " + motivoNoCatalogado);
            }
            if (descripcionLibre == null || descripcionLibre.isBlank()) {
                throw new BadRequestException("descripcionLibre es obligatoria cuando actividadId es null.");
            }
        } else if (motivoNoCatalogado != null) {
            throw new BadRequestException("motivoNoCatalogado debe ser null cuando actividadId no es null.");
        }
    }

    @Override
    @Transactional
    public EjecucionResponse editarEjecucion(Long id, EjecucionEditRequest request, UserPrincipal usuario) {
        if (request.motivoEdicion() == null || request.motivoEdicion().trim().length() < 15) {
            throw new BadRequestException("motivoEdicion debe tener al menos 15 caracteres.");
        }

        EjecucionEntity entity = ejecucionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejecución no encontrada: id=" + id));

        // Un registro que viene de una cita del cronograma conserva su actividad.
        Long actividadActualId = entity.getActividad() != null ? entity.getActividad().getId() : null;
        if (entity.getProgramacion() != null && !Objects.equals(request.actividadId(), actividadActualId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La actividad de un registro del cronograma no se puede cambiar");
        }

        String disciplina = entity.getDisciplina().getCodigo();
        validarCoherencia(disciplina, request.tipoMantenimiento(), request.tipoActividad(),
                request.actividadId(), request.motivoNoCatalogado(), request.resultado(),
                request.observaciones(), request.descripcionLibre());

        ActividadEntity actividad = null;
        if (request.actividadId() != null) {
            actividad = actividadRepository.findById(request.actividadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Actividad no encontrada: id=" + request.actividadId()));
        }

        List<CambioCampo> cambios = calcularCambios(entity, request, actividad);
        if (cambios.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La edición no modifica ningún campo");
        }

        String resultadoAnterior = entity.getResultado();

        entity.setFecha(request.fecha());
        entity.setMesEjecucion(request.mesEjecucion());
        entity.setSemanaEjecucion(request.semanaEjecucion());
        entity.setTipoMantenimiento(request.tipoMantenimiento());
        entity.setTipoActividad(request.tipoActividad());
        entity.setActividad(actividad);
        entity.setMotivoNoCatalogado(actividad == null ? request.motivoNoCatalogado() : null);
        entity.setResultado(request.resultado());
        entity.setObservaciones(request.observaciones());
        entity.setDescripcionLibre(actividad == null ? request.descripcionLibre() : null);
        ejecucionRepository.save(entity);

        UserEntity usuarioEntity = userRepositoryJpa.getUserEntityById(usuario.id());
        sincronizarSeguimiento(entity, resultadoAnterior, usuarioEntity);
        ejecucionEdicionRepository.save(EjecucionEdicionEntity.builder()
                .ejecucion(entity)
                .usuario(usuarioEntity)
                .motivo(request.motivoEdicion().trim())
                .cambios(CambioCampo.aJson(cambios))
                .build());

        saveActionUseCase.save("El usuario " + usuarioEntity.getUsername()
                + " editó la ejecución #" + entity.getId() + " en la estación " + entity.getEstacion().getNombre());

        return toResponse(entity);
    }

    @Override
    @Transactional
    public EvidenciaResponse agregarEvidencia(Long ejecucionId, MultipartFile file) throws IOException {
        EjecucionEntity ejecucion = ejecucionRepository.findById(ejecucionId)
                .orElseThrow(() -> new ResourceNotFoundException("Ejecución no encontrada: id=" + ejecucionId));

        var stored = evidenciaStorageService.store(file, ejecucionId);

        // Idempotente por (ejecucionId, hash): WorkManager reintenta la subida ante
        // cualquier fallo de red, incluido un timeout justo después de que el servidor
        // ya guardó el archivo. Sin esto, ese reintento crearía una foto duplicada.
        var existente = evidenciaRepository.findByEjecucion_IdAndHashSha256(ejecucionId, stored.hashSha256());
        if (existente.isPresent()) {
            evidenciaStorageService.delete(stored.rutaRelativa());
            return EvidenciaResponse.fromEntity(existente.get());
        }

        EvidenciaEntity entity = EvidenciaEntity.builder()
                .ejecucion(ejecucion)
                .rutaArchivo(stored.rutaRelativa())
                .nombreOriginal(stored.nombreOriginal())
                .hashSha256(stored.hashSha256())
                .build();

        return EvidenciaResponse.fromEntity(evidenciaRepository.save(entity));
    }

    @Override
    public EjecucionResponse obtenerEjecucion(Long id) {
        EjecucionEntity entity = ejecucionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejecución no encontrada: id=" + id));
        return toResponse(entity);
    }

    @Override
    public EjecucionResponse obtenerEjecucionPorProgramacion(Long programacionId) {
        EjecucionEntity entity = ejecucionRepository.findFirstByProgramacion_IdOrderByIdDesc(programacionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay ejecución registrada para la cita: programacionId=" + programacionId));
        return toResponse(entity);
    }

    @Override
    public Page<EjecucionResponse> listarEjecuciones(
            Long estacionId, LocalDate fechaInicio, LocalDate fechaFin, Boolean esProgramada,
            List<String> resultado, Long actividadId, String tipoMantenimiento, String tipoActividad,
            List<String> seguimiento, Pageable pageable) {
        LocalDate desde = fechaInicio != null ? fechaInicio : LocalDate.of(2000, 1, 1);
        LocalDate hasta = fechaFin != null ? fechaFin : LocalDate.now();
        var spec = EjecucionSpecifications.filtrar(
                estacionId, desde, hasta, esProgramada, resultado, actividadId, tipoMantenimiento, tipoActividad,
                seguimiento);
        return ejecucionRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private EjecucionResponse toResponse(EjecucionEntity entity) {
        List<EvidenciaResponse> evidencias = evidenciaRepository.findByEjecucion_Id(entity.getId()).stream()
                .map(EvidenciaResponse::fromEntity)
                .toList();
        List<EjecucionEdicionResponse> ediciones = ejecucionEdicionRepository
                .findByEjecucion_IdOrderByEditadoEnAsc(entity.getId()).stream()
                .map(EjecucionEdicionResponse::fromEntity)
                .toList();
        SeguimientoResponse seguimiento = hallazgoSeguimientoRepository.findByEjecucion_Id(entity.getId())
                .filter(HallazgoSeguimientoEntity::getStatus)
                .map(SeguimientoResponse::fromEntity)
                .orElse(null);
        return EjecucionResponse.fromEntity(entity, evidencias, ediciones, seguimiento);
    }

    @Override
    public List<CumplimientoResponse> cumplimientoPorMes(Integer anio, Integer mes, String disciplina) {
        return cumplimientoViewRepository.findByAnioAndMesAndDisciplinaOrderByEstacionNombreAsc(anio, mes, disciplina)
                .stream()
                .map(CumplimientoResponse::fromEntity)
                .toList();
    }

    @Override
    public List<CumplimientoResponse> cumplimientoPorEstacion(Long estacionId, Integer anio, String disciplina) {
        return cumplimientoViewRepository.findByEstacionIdAndAnioAndDisciplinaOrderByMesAsc(estacionId, anio, disciplina)
                .stream()
                .map(CumplimientoResponse::fromEntity)
                .toList();
    }

    @Override
    public List<IndicadorEstacionResponse> indicadoresPorEstacion() {
        return indicadorEstacionViewRepository.findAllByOrderByEstacionNombreAsc().stream()
                .map(IndicadorEstacionResponse::fromEntity)
                .toList();
    }

    @Override
    public List<ResumenActividadResponse> resumenPorActividad(String disciplina) {
        return resumenActividadViewRepository.findByDisciplinaOrderByActividadNombreAsc(disciplina).stream()
                .map(ResumenActividadResponse::fromEntity)
                .toList();
    }
}
