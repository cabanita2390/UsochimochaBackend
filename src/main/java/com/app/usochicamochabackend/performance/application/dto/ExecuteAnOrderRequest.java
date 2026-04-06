package com.app.usochicamochabackend.performance.application.dto;

import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;

import java.time.LocalDate;
import java.util.List;

public record ExecuteAnOrderRequest(
        Long orderId,
        String timeSpent,
        String description,
        LaborRequest labor,
        SparePartRequest sparePart
) {}