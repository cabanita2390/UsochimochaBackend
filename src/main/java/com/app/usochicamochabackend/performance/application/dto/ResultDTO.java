package com.app.usochicamochabackend.performance.application.dto;

import com.app.usochicamochabackend.order.application.dto.OrderResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ResultDTO(
        Long id,
        LocalDateTime date,
        String description,
        Double hourMeter,
        String timeSpent,
        LaborResponse labor,
        SparePartResponse sparePart,
        BigDecimal totalPrice
) {}
