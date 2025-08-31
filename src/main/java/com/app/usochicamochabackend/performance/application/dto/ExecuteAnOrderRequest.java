package com.app.usochicamochabackend.performance.application.dto;

import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;

import java.time.LocalDate;
import java.util.List;

public record ExecuteAnOrderRequest(
        LocalDate date,
        String timeSpent,
        List<LaborRequest> labor,
        List<SparePartRequest> sparePart
) {}