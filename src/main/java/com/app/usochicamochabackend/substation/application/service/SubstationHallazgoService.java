package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.substation.application.dto.ResolverHallazgoRequest;
import com.app.usochicamochabackend.substation.application.dto.SeguimientoResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationHallazgoUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.HallazgoSeguimientoEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.EjecucionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.HallazgoSeguimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubstationHallazgoService implements SubstationHallazgoUseCase {

    private final EjecucionRepository ejecucionRepository;
    private final HallazgoSeguimientoRepository hallazgoSeguimientoRepository;
    private final UserRepositoryJpa userRepositoryJpa;
    private final SaveActionUseCase saveActionUseCase;

    @Override
    @Transactional
    public SeguimientoResponse marcarEnProceso(Long ejecucionId, UserPrincipal usuario) {
        HallazgoSeguimientoEntity seguimiento = buscarSeguimientoAbierto(ejecucionId);
        UserEntity usuarioEntity = userRepositoryJpa.getUserEntityById(usuario.id());

        seguimiento.setEstado("EN_PROCESO");
        seguimiento.setActualizadoPor(usuarioEntity);
        seguimiento.setActualizadoEn(LocalDateTime.now());
        HallazgoSeguimientoEntity guardado = hallazgoSeguimientoRepository.save(seguimiento);

        saveActionUseCase.save("El usuario " + usuarioEntity.getUsername()
                + " marcó en proceso el hallazgo de la ejecución #" + ejecucionId);
        return SeguimientoResponse.fromEntity(guardado);
    }

    @Override
    @Transactional
    public SeguimientoResponse resolver(Long ejecucionId, ResolverHallazgoRequest request, UserPrincipal usuario) {
        HallazgoSeguimientoEntity seguimiento = buscarSeguimientoAbierto(ejecucionId);

        boolean mismaVisita = Boolean.TRUE.equals(request.resueltoMismaVisita());
        if (mismaVisita && request.resueltoEnEjecucionId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indique la misma visita o la ejecución en que se resolvió, no ambas.");
        }

        EjecucionEntity resueltoEn = null;
        if (request.resueltoEnEjecucionId() != null) {
            resueltoEn = validarEjecucionQueResolvio(seguimiento.getEjecucion(), request.resueltoEnEjecucionId());
        }

        UserEntity usuarioEntity = userRepositoryJpa.getUserEntityById(usuario.id());
        LocalDateTime ahora = LocalDateTime.now();
        seguimiento.setEstado("RESUELTO");
        seguimiento.setObservacionesCierre(request.observacionesCierre().trim());
        seguimiento.setResueltoEnEjecucion(resueltoEn);
        seguimiento.setResueltoMismaVisita(mismaVisita);
        seguimiento.setCerradoPor(usuarioEntity);
        seguimiento.setCerradoEn(ahora);
        seguimiento.setActualizadoPor(usuarioEntity);
        seguimiento.setActualizadoEn(ahora);
        HallazgoSeguimientoEntity guardado = hallazgoSeguimientoRepository.save(seguimiento);

        saveActionUseCase.save("El usuario " + usuarioEntity.getUsername()
                + " resolvió el hallazgo de la ejecución #" + ejecucionId);
        return SeguimientoResponse.fromEntity(guardado);
    }

    /** 404 si la ejecución no existe; 409 si es CONFORME (sin seguimiento activo) o ya está RESUELTO. */
    private HallazgoSeguimientoEntity buscarSeguimientoAbierto(Long ejecucionId) {
        if (!ejecucionRepository.existsById(ejecucionId)) {
            throw new ResourceNotFoundException("Ejecución no encontrada: id=" + ejecucionId);
        }
        HallazgoSeguimientoEntity seguimiento = hallazgoSeguimientoRepository.findByEjecucion_Id(ejecucionId)
                .filter(HallazgoSeguimientoEntity::getStatus)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "La ejecución #" + ejecucionId + " es CONFORME: no tiene hallazgo que seguir."));
        if ("RESUELTO".equals(seguimiento.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El hallazgo de la ejecución #" + ejecucionId + " ya está resuelto.");
        }
        return seguimiento;
    }

    /** La ejecución que lo resolvió debe existir, ser de la misma estación y de fecha posterior. */
    private EjecucionEntity validarEjecucionQueResolvio(EjecucionEntity hallazgo, Long resueltoEnEjecucionId) {
        EjecucionEntity resueltoEn = ejecucionRepository.findById(resueltoEnEjecucionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La ejecución #" + resueltoEnEjecucionId + " no existe."));
        if (!resueltoEn.getEstacion().getId().equals(hallazgo.getEstacion().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La ejecución que lo resolvió debe ser de la misma estación.");
        }
        if (!resueltoEn.getFecha().isAfter(hallazgo.getFecha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La ejecución que lo resolvió debe ser posterior al " + hallazgo.getFecha() + ".");
        }
        return resueltoEn;
    }
}
