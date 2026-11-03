package com.app.usochicamochabackend.moto.application.dto;

import com.app.usochicamochabackend.tire.application.dto.LecturaPresionLlantaRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.List;

@Schema(
        name = "InspeccionMotoRequest",
        description = "Inspección diaria simplificada de moto. Persiste cabecera, detalle mecánico y detalle de documentos. "
                        + "El usuario responsable se toma del JWT. Si `kilometrajeReportado` > 0 se actualiza el odómetro del vehículo.")
public record InspeccionMotoRequest(
        // Cabecera Principal y Detalles Vehiculo
        @Schema(description = "FK a `vehiculos.id_vehiculo` (debe ser tipo moto)", example = "4", requiredMode = RequiredMode.REQUIRED)
        Integer idVehiculo,
        @Schema(description = "Odómetro reportado en campo; actualiza `vehiculos.kilometraje_actual` si es positivo", example = "15200")
        Integer kilometrajeReportado,
        @Schema(description = "Estado general de la moto (texto libre o valor acordado, p. ej. EXCELENTE)", example = "BUENO")
        String estadoVehiculo,
        @Schema(description = "Observaciones finales del inspector") String observacionesFinales,

        // Detalle Documentos (valores de chequeo visual / estado)
        @Schema(description = "Estado declarado del SOAT", example = "Vigente")
        String checkSoat,
        @Schema(description = "Estado declarado de tecnomecánica", example = "Vigente")
        String checkTecno,
        @Schema(description = "Extintor u observación (ej. N/A si no aplica)", example = "N/A")
        String checkExtintor,

        // Detalle Mecánico Moto (Bueno / Regular / Malo)
        @Schema(description = "Nivel de aceite (Bueno / Regular / Malo)", example = "Bueno")
        String checkNivelAceite,
        @Schema(description = "Estado de llantas", example = "Bueno")
        String checkEstadoLlantas,
        @Schema(description = "Estado de luces", example = "Bueno")
        String checkEstadoLuces,

        // Ubicación (Migrado a General)
        @Schema(description = "Id en `cat_ubicaciones` donde se reporta la inspección", example = "2")
        Integer idUbicacion,

        // Presión de llantas (opcional; según el perfil de llantas de la moto)
        @Schema(description = "Presión medida por llanta (PSI). No bloquea la inspección si está fuera de rango.")
        List<LecturaPresionLlantaRequest> presionesLlantas,

        // Identificador de la inspección en el teléfono: un reintento con el mismo valor devuelve la ya
        // registrada en vez de crear otra. Opcional (versiones viejas del móvil no lo envían).
        @Schema(description = "UUID de la inspección en el móvil; reenviarla con el mismo valor no la duplica", example = "1b4e28ba-2fa1-11d2-883f-0016d3cca427")
        String uuidCliente) {

    /** Sin uuidCliente: clientes anteriores a V52. */
    public InspeccionMotoRequest(Integer idVehiculo, Integer kilometrajeReportado, String estadoVehiculo, String observacionesFinales, String checkSoat, String checkTecno, String checkExtintor, String checkNivelAceite, String checkEstadoLlantas, String checkEstadoLuces, Integer idUbicacion, List<LecturaPresionLlantaRequest> presionesLlantas) {
        this(idVehiculo, kilometrajeReportado, estadoVehiculo, observacionesFinales, checkSoat, checkTecno, checkExtintor, checkNivelAceite, checkEstadoLlantas, checkEstadoLuces, idUbicacion, presionesLlantas, null);
    }
}
