package com.app.usochicamochabackend.vehicleinspection.application.dto;

import java.time.LocalDate;

public record VehicleDocumentRequest(
    Integer idVehiculo,
    String tipoDocumento,
    LocalDate fechaVencimiento,
    String imagenUrl
) {}
