package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.substation.application.dto.CronogramaResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationCronogramaUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.ProgramacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.PublicacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ProgramacionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubstationCronogramaService implements SubstationCronogramaUseCase {

    private final ProgramacionRepository programacionRepository;
    private final PublicacionRepository publicacionRepository;
    private final CalendarioMantenimiento calendario;

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
