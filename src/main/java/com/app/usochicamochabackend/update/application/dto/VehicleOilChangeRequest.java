package com.app.usochicamochabackend.update.application.dto;

import java.time.LocalDateTime;

public record VehicleOilChangeRequest(
    String placa,
    LocalDateTime dateStamp,
    String oilType,
    Long brandId,
    Double quantity,
    Integer kmAtChange,
    Integer intervalKm,
    Boolean airFilterChanged
) {}
