package com.app.usochicamochabackend.substation.application.port;

import com.app.usochicamochabackend.substation.application.dto.ProgramacionResponse;

import java.util.List;

public interface SubstationCatalogUseCase {

    /** Citas del cronograma para una estación+mes+disciplina — lo que el técnico ve al elegir actividad. */
    List<ProgramacionResponse> listarProgramacion(Long estacionId, Integer anio, Integer mes, String disciplina);
}
