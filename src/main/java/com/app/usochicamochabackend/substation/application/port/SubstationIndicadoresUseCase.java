package com.app.usochicamochabackend.substation.application.port;

import com.app.usochicamochabackend.substation.application.dto.CumplimientoResponse;
import com.app.usochicamochabackend.substation.application.dto.IndicadorEstacionResponse;
import com.app.usochicamochabackend.substation.application.dto.ResumenActividadResponse;

import java.util.List;

public interface SubstationIndicadoresUseCase {

    /** Cumplimiento de todas las estaciones para un mes+año+disciplina (una fila por cita del cronograma). */
    List<CumplimientoResponse> cumplimientoPorMes(Integer anio, Integer mes, String disciplina);

    /** Cumplimiento de una estación a lo largo del año, mes a mes. */
    List<CumplimientoResponse> cumplimientoPorEstacion(Long estacionId, Integer anio, String disciplina);

    /** Dashboard: una fila por estación activa, con lo publicado y lo ejecutado del año y la disciplina. */
    List<IndicadorEstacionResponse> indicadoresPorEstacion(Integer anio, String disciplina);

    /** Resumen por actividad del año (actividades activas de la disciplina), todas las estaciones. */
    List<ResumenActividadResponse> resumenPorActividad(String disciplina, Integer anio);
}
