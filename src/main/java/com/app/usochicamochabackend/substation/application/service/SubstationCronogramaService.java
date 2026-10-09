package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.substation.application.dto.AniosCronogramaResponse;
import com.app.usochicamochabackend.substation.application.dto.AsignacionResultado;
import com.app.usochicamochabackend.substation.application.dto.AsignarCitasRequest;
import com.app.usochicamochabackend.substation.application.dto.CopiarAnioRequest;
import com.app.usochicamochabackend.substation.application.dto.CronogramaResponse;
import com.app.usochicamochabackend.substation.application.dto.DescarteResultado;
import com.app.usochicamochabackend.substation.application.dto.PublicacionResponse;
import com.app.usochicamochabackend.substation.application.dto.PublicacionResultado;
import com.app.usochicamochabackend.substation.application.dto.ResumenBorradorResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationCronogramaUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EstacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ProgramacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.PublicacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ActividadRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EjecucionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EstacionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ProgramacionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SubstationCronogramaService implements SubstationCronogramaUseCase {

    private final ProgramacionRepository programacionRepository;
    private final PublicacionRepository publicacionRepository;
    private final CalendarioMantenimiento calendario;
    private final ActividadRepository actividadRepository;
    private final EstacionRepository estacionRepository;
    private final EjecucionRepository ejecucionRepository;
    private final UserRepositoryJpa userRepositoryJpa;
    private final SaveActionUseCase saveActionUseCase;

    @Override
    @Transactional(readOnly = true)
    public CronogramaResponse obtenerCronograma(Integer anio, String disciplina) {
        List<CronogramaResponse.Cita> todas = programacionRepository.citasDelCronograma(anio).stream()
                .map(SubstationCronogramaService::aCita)
                .toList();

        // El borrador se publica completo (todas las disciplinas), así que la barra lo cuenta
        // entero aunque la grilla esté filtrada.
        int altas = (int) todas.stream().filter(c -> ProgramacionEntity.BORRADOR.equals(c.estado())).count();
        // Las bajas incluyen las de estaciones desactivadas (no salen en la grilla, pero se publican).
        int bajas = programacionRepository.countByAnioAndStatusTrueAndPendienteRetiroTrue(anio);

        PublicacionEntity ultima = publicacionRepository.findFirstByAnioAndRevertidaFalseOrderByIdDesc(anio)
                .orElse(null);
        boolean puedeDeshacer = ultima != null && !ultima.getInicial() && altas + bajas == 0;

        List<CronogramaResponse.Cita> citas = disciplina == null || disciplina.isBlank()
                ? todas
                : todas.stream().filter(c -> c.disciplina().equals(disciplina)).toList();

        return new CronogramaResponse(
                anio,
                calendario.anioActual(),
                calendario.mesActual(),
                ultima == null ? null : aUltimaPublicacion(ultima),
                new CronogramaResponse.Borrador(altas, bajas),
                puedeDeshacer,
                citas);
    }

    @Override
    @Transactional
    public AsignacionResultado asignar(AsignarCitasRequest request, UserPrincipal usuario) {
        validarAnioProgramable(request.anio());
        ActividadEntity actividad = actividadRepository.findById(request.actividadId())
                .filter(ActividadEntity::getStatus)
                .orElseThrow(() -> badRequest("Actividad inexistente o inactiva: id=" + request.actividadId()));
        List<Integer> meses = request.meses().stream().distinct().sorted().toList();
        if (meses.stream().anyMatch(m -> m == null || m < 1 || m > 12)) {
            throw badRequest("Los meses deben estar entre 1 y 12");
        }
        List<Long> estacionIds = request.estacionIds().stream().distinct().toList();
        Map<Long, EstacionEntity> estaciones = estacionRepository.findAllById(estacionIds).stream()
                .collect(Collectors.toMap(EstacionEntity::getId, Function.identity()));
        if (estaciones.size() != estacionIds.size()) {
            throw badRequest("Alguna estación no existe");
        }

        Set<String> vigentes = clavesVigentes(request.anio());
        UserEntity autor = userRepositoryJpa.getUserEntityById(usuario.id());
        int creadas = 0, duplicadas = 0, mesCerrado = 0, inactivas = 0;
        for (Long estacionId : estacionIds) {
            EstacionEntity estacion = estaciones.get(estacionId);
            for (Integer mes : meses) {
                if (!estacion.getStatus()) {
                    inactivas++;
                } else if (calendario.mesCerrado(request.anio(), mes)) {
                    mesCerrado++;
                } else if (!vigentes.add(clave(estacionId, actividad.getId(), mes))) {
                    duplicadas++;
                } else {
                    guardarNueva(nuevoBorrador(request.anio(), mes, estacion, actividad, autor));
                    creadas++;
                }
            }
        }

        if (creadas > 0) {
            saveActionUseCase.save("El usuario " + autor.getUsername() + " ha asignado " + creadas
                    + " citas de " + actividad.getNombre() + " en el cronograma " + request.anio() + " (borrador)");
        }
        return new AsignacionResultado(creadas, duplicadas, mesCerrado, inactivas);
    }

    @Override
    @Transactional
    public void quitar(Long citaId, UserPrincipal usuario) {
        ProgramacionEntity cita = citaVigente(citaId);
        if (ejecucionRepository.existsByProgramacion_Id(citaId)) {
            throw conflicto("No se puede quitar una cita que ya tiene una ejecución registrada");
        }
        if (calendario.mesCerrado(cita.getAnio(), cita.getMes())) {
            throw conflicto("No se puede quitar una cita de un mes cerrado");
        }
        if (ProgramacionEntity.BORRADOR.equals(cita.getEstado())) {
            cita.setStatus(false); // alta sin publicar: se descarta
        } else {
            cita.setPendienteRetiro(true); // el móvil la sigue viendo hasta publicar
        }
        programacionRepository.save(cita);
        saveActionUseCase.save("El usuario " + usuario.username() + " ha quitado del cronograma "
                + cita.getAnio() + " la cita " + describir(cita) + " (borrador)");
    }

    @Override
    @Transactional
    public void restaurar(Long citaId, UserPrincipal usuario) {
        ProgramacionEntity cita = citaVigente(citaId);
        if (!cita.getPendienteRetiro()) {
            throw conflicto("La cita no está marcada para quitar");
        }
        cita.setPendienteRetiro(false);
        programacionRepository.save(cita);
        saveActionUseCase.save("El usuario " + usuario.username() + " ha restaurado en el cronograma "
                + cita.getAnio() + " la cita " + describir(cita));
    }

    @Override
    @Transactional
    public AsignacionResultado copiarAnio(CopiarAnioRequest request, UserPrincipal usuario) {
        int origen = request.anioOrigen(), destino = request.anioDestino();
        validarAnioProgramable(destino);
        if (origen == destino) {
            throw badRequest("El año de destino debe ser distinto al de origen");
        }
        if (programacionRepository.existsByAnioAndStatusTrueAndEstado(destino, ProgramacionEntity.PUBLICADA)) {
            throw conflicto("El cronograma " + destino + " ya tiene citas publicadas");
        }

        Set<String> vigentes = clavesVigentes(destino);
        UserEntity autor = userRepositoryJpa.getUserEntityById(usuario.id());
        int creadas = 0, duplicadas = 0, mesCerrado = 0, inactivas = 0;
        for (ProgramacionEntity c : programacionRepository.findByAnioAndStatusTrueAndEstado(origen, ProgramacionEntity.PUBLICADA)) {
            if (!c.getActividad().getStatus()) {
                continue; // actividad desactivada: no se programa más
            }
            if (!c.getEstacion().getStatus()) {
                inactivas++;
            } else if (calendario.mesCerrado(destino, c.getMes())) {
                mesCerrado++;
            } else if (!vigentes.add(clave(c.getEstacion().getId(), c.getActividad().getId(), c.getMes()))) {
                duplicadas++;
            } else {
                guardarNueva(nuevoBorrador(destino, c.getMes(), c.getEstacion(), c.getActividad(), autor));
                creadas++;
            }
        }

        saveActionUseCase.save("El usuario " + autor.getUsername() + " ha copiado " + creadas
                + " citas del cronograma " + origen + " al " + destino + " (borrador)");
        return new AsignacionResultado(creadas, duplicadas, mesCerrado, inactivas);
    }

    @Override
    @Transactional
    public DescarteResultado descartarBorrador(Integer anio, UserPrincipal usuario) {
        int altas = 0, conservadas = 0;
        for (ProgramacionEntity c : programacionRepository.findByAnioAndStatusTrueAndEstado(anio, ProgramacionEntity.BORRADOR)) {
            if (ejecucionRepository.existsByProgramacion_Id(c.getId())) {
                conservadas++; // nunca se toca una cita con ejecución
                continue;
            }
            c.setStatus(false);
            programacionRepository.save(c);
            altas++;
        }
        List<ProgramacionEntity> retiros = programacionRepository.findByAnioAndStatusTrueAndPendienteRetiroTrue(anio);
        retiros.forEach(c -> c.setPendienteRetiro(false));
        programacionRepository.saveAll(retiros);

        saveActionUseCase.save("El usuario " + usuario.username() + " ha descartado el borrador del cronograma "
                + anio + " (" + altas + " altas, " + retiros.size() + " retiros)");
        return new DescarteResultado(altas, retiros.size(), conservadas);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenBorradorResponse resumenBorrador(Integer anio) {
        List<ProgramacionEntity> cambios = programacionRepository.cambiosPendientes(anio);
        Map<EstacionEntity, List<ProgramacionEntity>> porEstacion = cambios.stream()
                .collect(Collectors.groupingBy(ProgramacionEntity::getEstacion));
        List<ResumenBorradorResponse.Estacion> estaciones = porEstacion.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getNombre()))
                .map(e -> new ResumenBorradorResponse.Estacion(
                        e.getKey().getId(),
                        e.getKey().getNombre(),
                        e.getValue().stream()
                                .map(c -> new ResumenBorradorResponse.Cambio(c.getId(), c.getMes(),
                                        c.getActividad().getNombre(), esAlta(c) ? "ALTA" : "BAJA"))
                                .sorted(Comparator.comparing(ResumenBorradorResponse.Cambio::mes)
                                        .thenComparing(ResumenBorradorResponse.Cambio::tipo)
                                        .thenComparing(ResumenBorradorResponse.Cambio::actividadNombre))
                                .toList()))
                .toList();
        int altas = (int) cambios.stream().filter(SubstationCronogramaService::esAlta).count();
        return new ResumenBorradorResponse(altas, cambios.size() - altas, estaciones.size(), estaciones);
    }

    @Override
    @Transactional
    public PublicacionResultado publicar(Integer anio, UserPrincipal usuario) {
        List<ProgramacionEntity> cambios = programacionRepository.cambiosPendientesParaPublicar(anio);
        if (cambios.isEmpty()) {
            throw conflicto("No hay cambios en borrador para publicar");
        }
        UserEntity autor = userRepositoryJpa.getUserEntityById(usuario.id());
        PublicacionEntity publicacion = publicacionRepository.save(PublicacionEntity.builder()
                .anio(anio)
                .usuario(autor)
                .publicadoEn(calendario.ahora())
                .altas(0)
                .bajas(0)
                .build());

        int altas = 0, bajas = 0;
        List<PublicacionResultado.NoAplicada> noAplicadas = new ArrayList<>();
        for (ProgramacionEntity c : cambios) {
            if (esAlta(c)) {
                c.setEstado(ProgramacionEntity.PUBLICADA);
                c.setPublicadaEn(publicacion);
                altas++;
            } else if (ejecucionRepository.existsByProgramacion_Id(c.getId())) {
                // El técnico la ejecutó mientras estaba marcada para quitar: se conserva.
                c.setPendienteRetiro(false);
                noAplicadas.add(new PublicacionResultado.NoAplicada(c.getId(), "Tiene ejecución registrada"));
            } else {
                c.setEstado(ProgramacionEntity.RETIRADA);
                c.setPendienteRetiro(false);
                c.setRetiradaEn(publicacion);
                bajas++;
            }
        }
        programacionRepository.saveAll(cambios);
        publicacion.setAltas(altas);
        publicacion.setBajas(bajas);
        publicacionRepository.save(publicacion);

        saveActionUseCase.save("El usuario " + autor.getUsername() + " ha publicado a móvil el cronograma "
                + anio + ": +" + altas + " −" + bajas);
        return new PublicacionResultado(publicacion.getId(), altas, bajas, noAplicadas);
    }

    @Override
    @Transactional
    public PublicacionResultado deshacerUltimaPublicacion(Integer anio, UserPrincipal usuario) {
        PublicacionEntity publicacion = publicacionRepository
                .findFirstByAnioAndRevertidaFalseAndInicialFalseOrderByIdDesc(anio)
                .orElseThrow(() -> conflicto("No hay publicaciones para deshacer"));
        if (!programacionRepository.cambiosPendientes(anio).isEmpty()) {
            throw conflicto("Hay cambios en borrador: publíquelos o descártelos antes de deshacer");
        }

        int altas = 0, bajas = 0;
        List<PublicacionResultado.NoAplicada> noAplicadas = new ArrayList<>();
        List<ProgramacionEntity> modificadas = new ArrayList<>();
        // Altas de esa publicación: vuelven al borrador, salvo las que ya se ejecutaron.
        for (ProgramacionEntity c : programacionRepository
                .findByPublicadaEn_IdAndStatusTrueAndEstado(publicacion.getId(), ProgramacionEntity.PUBLICADA)) {
            if (ejecucionRepository.existsByProgramacion_Id(c.getId())) {
                noAplicadas.add(new PublicacionResultado.NoAplicada(c.getId(), "Tiene ejecución registrada"));
                continue;
            }
            c.setEstado(ProgramacionEntity.BORRADOR);
            c.setPublicadaEn(null);
            modificadas.add(c);
            altas++;
        }
        // Bajas de esa publicación: el móvil las vuelve a ver y quedan en el borrador como "se quitará".
        for (ProgramacionEntity c : programacionRepository
                .findByRetiradaEn_IdAndStatusTrueAndEstado(publicacion.getId(), ProgramacionEntity.RETIRADA)) {
            if (programacionRepository.existsByAnioAndMesAndEstacion_IdAndActividad_IdAndStatusTrueAndEstadoNot(
                    c.getAnio(), c.getMes(), c.getEstacion().getId(), c.getActividad().getId(), ProgramacionEntity.RETIRADA)) {
                noAplicadas.add(new PublicacionResultado.NoAplicada(c.getId(), "Ya hay una cita vigente igual"));
                continue;
            }
            c.setEstado(ProgramacionEntity.PUBLICADA);
            c.setRetiradaEn(null);
            c.setPendienteRetiro(true);
            modificadas.add(c);
            bajas++;
        }
        programacionRepository.saveAll(modificadas);

        UserEntity autor = userRepositoryJpa.getUserEntityById(usuario.id());
        publicacion.setRevertida(true);
        publicacion.setRevertidaPor(autor);
        publicacion.setRevertidaEn(calendario.ahora());
        publicacionRepository.save(publicacion);

        saveActionUseCase.save("El usuario " + autor.getUsername() + " ha deshecho la publicación #"
                + publicacion.getId() + " del cronograma " + anio + ": " + altas + " altas y " + bajas
                + " bajas vuelven al borrador");
        return new PublicacionResultado(publicacion.getId(), altas, bajas, noAplicadas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicacionResponse> historialPublicaciones(Integer anio) {
        return publicacionRepository.findByAnioOrderByIdDesc(anio).stream()
                .map(PublicacionResponse::fromEntity)
                .toList();
    }

    private static boolean esAlta(ProgramacionEntity c) {
        return ProgramacionEntity.BORRADOR.equals(c.getEstado());
    }

    @Override
    @Transactional(readOnly = true)
    public AniosCronogramaResponse anios() {
        int actual = calendario.anioActual();
        return new AniosCronogramaResponse(actual, List.of(actual, actual + 1),
                programacionRepository.aniosConDatos().stream().map(Number::intValue).toList());
    }

    /** Solo el año actual y el siguiente se programan (la grilla ofrece esos dos). */
    private void validarAnioProgramable(Integer anio) {
        int actual = calendario.anioActual();
        if (anio == null || !calendario.esAnioProgramable(anio)) {
            throw badRequest("Solo se puede programar el año actual (" + actual + ") o el siguiente (" + (actual + 1) + ")");
        }
    }

    /**
     * Inserta una cita nueva. Si otro usuario asignó la misma cita al mismo tiempo, el índice
     * único salta aquí (no al confirmar) y se responde un 409 que se entiende.
     */
    private void guardarNueva(ProgramacionEntity cita) {
        try {
            programacionRepository.saveAndFlush(cita);
        } catch (DataIntegrityViolationException e) {
            throw conflicto("Otro usuario modificó el cronograma al mismo tiempo: recargue e intente de nuevo");
        }
    }

    /** Con bloqueo: si hay una publicación en curso, espera a que termine y lee el estado nuevo. */
    private ProgramacionEntity citaVigente(Long citaId) {
        return programacionRepository.findByIdParaActualizar(citaId)
                .filter(c -> c.getStatus() && !ProgramacionEntity.RETIRADA.equals(c.getEstado()))
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada en el cronograma: id=" + citaId));
    }

    /** estación-actividad-mes de las citas vigentes del año (no se crean duplicados). */
    private Set<String> clavesVigentes(int anio) {
        return programacionRepository.findByAnioAndStatusTrueAndEstadoNot(anio, ProgramacionEntity.RETIRADA).stream()
                .map(c -> clave(c.getEstacion().getId(), c.getActividad().getId(), c.getMes()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static String clave(Long estacionId, Long actividadId, Integer mes) {
        return estacionId + "-" + actividadId + "-" + mes;
    }

    private static ProgramacionEntity nuevoBorrador(int anio, int mes, EstacionEntity estacion,
            ActividadEntity actividad, UserEntity autor) {
        return ProgramacionEntity.builder()
                .anio(anio)
                .mes(mes)
                .estacion(estacion)
                .actividad(actividad)
                .estado(ProgramacionEntity.BORRADOR)
                .creadaPor(autor)
                .build();
    }

    private static String describir(ProgramacionEntity c) {
        return c.getActividad().getNombre() + " · " + c.getEstacion().getNombre() + " · mes " + c.getMes();
    }

    private static ResponseStatusException badRequest(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }

    private static ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }

    private static CronogramaResponse.Cita aCita(Object[] fila) {
        long ejecuciones = ((Number) fila[8]).longValue();
        return new CronogramaResponse.Cita(
                (Long) fila[0],
                ((Number) fila[1]).intValue(),
                (Long) fila[2],
                (Long) fila[3],
                (String) fila[4],
                (String) fila[5],
                (Boolean) fila[6],
                ejecuciones > 0,
                (LocalDate) fila[7]);
    }

    private static CronogramaResponse.UltimaPublicacion aUltimaPublicacion(PublicacionEntity p) {
        String usuario = p.getInicial() || p.getUsuario() == null
                ? PublicacionResponse.CARGA_INICIAL
                : p.getUsuario().getFullName();
        return new CronogramaResponse.UltimaPublicacion(
                p.getId(), p.getPublicadoEn(), usuario, p.getAltas(), p.getBajas(), p.getInicial());
    }
}
