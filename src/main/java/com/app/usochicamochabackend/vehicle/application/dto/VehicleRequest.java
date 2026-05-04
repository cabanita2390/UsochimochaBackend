package com.app.usochicamochabackend.vehicle.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record VehicleRequest(
        @NotBlank(message = "La placa es obligatoria")
        String placa,

        @NotNull(message = "La marca es obligatoria")
        Integer idMarca,

        @NotNull(message = "El tipo de vehículo es obligatorio")
        Integer idTipoVehiculo,

        @NotNull(message = "El kilometraje es obligatorio")
        @Min(value = 0, message = "El kilometraje no puede ser negativo")
        Integer kilometrajeActual,

        String belongsTo,

        /** Catálogo {@code cat_ubicaciones}; opcional (p. ej. Unidad Pantano). */
        Integer idUbicacionBase,

        Boolean activo
) {
}
