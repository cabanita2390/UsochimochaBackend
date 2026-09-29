package com.app.usochicamochabackend.substation.application.port;

import com.app.usochicamochabackend.substation.application.dto.CronogramaResponse;

/** Cronograma anual editable (borrador → publicación al móvil). */
public interface SubstationCronogramaUseCase {

    /** Grilla del año: citas en BORRADOR y PUBLICADA, estado de publicación y mes actual. disciplina es opcional. */
    CronogramaResponse obtenerCronograma(Integer anio, String disciplina);
}
