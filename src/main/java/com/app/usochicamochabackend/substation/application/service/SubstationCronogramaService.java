package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.substation.application.dto.AsignacionResultado;
import com.app.usochicamochabackend.substation.application.dto.AsignarCitasRequest;
import com.app.usochicamochabackend.substation.application.dto.CopiarAnioRequest;
import com.app.usochicamochabackend.substation.application.dto.CronogramaResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        int bajas = (int) todas.stream().filter(CronogramaResponse.Cita::pendienteRetiro).count();

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
                    programacionRepository.save(nuevoBorrador(request.anio(), mes, estacion, actividad, autor));
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
                programacionRepository.save(nuevoBorrador(destino, c.getMes(), c.getEstacion(), c.getActividad(), autor));
                creadas++;
            }
        }

        saveActionUseCase.save("El usuario " + autor.getUsername() + " ha copiado " + creadas
                + " citas del cronograma " + origen + " al " + destino + " (borrador)");
        return new AsignacionResultado(creadas, duplicadas, mesCerrado, inactivas);
    }

    @Override
    @Transactional
    public void descartarBorrador(Integer anio, UserPrincipal usuario) {
        int altas = 0;
        for (ProgramacionEntity c : programacionRepository.findByAnioAndStatusTrueAndEstado(anio, ProgramacionEntity.BORRADOR)) {
            if (ejecucionRepository.existsByProgramacion_Id(c.getId())) {
                continue; // nunca se toca una cita con ejecución
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
    }

    private ProgramacionEntity citaVigente(Long citaId) {
        return programacionRepository.findById(citaId)
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
                ? "Carga inicial"
                : p.getUsuario().getFullName();
        return new CronogramaResponse.UltimaPublicacion(
                p.getId(), p.getPublicadoEn(), usuario, p.getAltas(), p.getBajas(), p.getInicial());
    }
}
