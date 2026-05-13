package com.app.usochicamochabackend.vehicle.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "VehicleResponse",
        description = "Vista de vehículo con marca y tipo resueltos a texto. Usado en `/api/v1/vehicle` y CRUD `/api/v1/moto`.")
public record VehicleResponse(
        @Schema(description = "PK `vehiculos.id_vehiculo`", example = "15")
        Integer id,
        @Schema(description = "Placa", example = "JKL456")
        String placa,
        @Schema(description = "Descripción de marca (`cat_marcas_modelos`)", example = "Toyota")
        String marca,
        @Schema(description = "FK `cat_marcas_modelos.id_marca`", example = "3")
        Integer idMarca,
        @Schema(description = "FK `cat_tipos_vehiculo.id_tipo_vehiculo`", example = "1")
        Integer idTipoVehiculo,
        @Schema(description = "Nombre del tipo (`cat_tipos_vehiculo`)", example = "AUTOMOVIL")
        String tipoVehiculo,
        @Schema(description = "Kilometraje actual registrado", example = "89000")
        Integer kilometrajeActual,
        @Schema(description = "Área / pertenencia organizacional", example = "Operaciones Chicamocha")
        String belongsTo,
        @Schema(description = "Id de ubicación base (`cat_ubicaciones`)", example = "2")
        Integer idUbicacionBase,
        @Schema(description = "Nombre legible de la ubicación base", example = "Campo Málaga")
        String ubicacionBase
) {
}
