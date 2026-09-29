package com.app.usochicamochabackend.substation.application.port;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.substation.application.dto.AsignacionResultado;
import com.app.usochicamochabackend.substation.application.dto.AsignarCitasRequest;
import com.app.usochicamochabackend.substation.application.dto.CopiarAnioRequest;
import com.app.usochicamochabackend.substation.application.dto.CronogramaResponse;

/** Cronograma anual editable (borrador → publicación al móvil). */
public interface SubstationCronogramaUseCase {

    /** Grilla del año: citas en BORRADOR y PUBLICADA, estado de publicación y mes actual. disciplina es opcional. */
    CronogramaResponse obtenerCronograma(Integer anio, String disciplina);

    /** Crea citas en BORRADOR. Omite (sin fallar) duplicados, meses cerrados y estaciones inactivas. */
    AsignacionResultado asignar(AsignarCitasRequest request, UserPrincipal usuario);

    /** BORRADOR → se da de baja; PUBLICADA → pendiente de retiro. 409 si tiene ejecución o el mes está cerrado. */
    void quitar(Long citaId, UserPrincipal usuario);

    /** Deshace "quitar" sobre una PUBLICADA pendiente de retiro. */
    void restaurar(Long citaId, UserPrincipal usuario);

    /** Copia las citas PUBLICADA del año origen al destino como BORRADOR. 409 si el destino ya tiene publicadas. */
    AsignacionResultado copiarAnio(CopiarAnioRequest request, UserPrincipal usuario);

    /** Descarta el borrador del año: altas dadas de baja y retiros pendientes anulados. No toca citas con ejecución. */
    void descartarBorrador(Integer anio, UserPrincipal usuario);
}
