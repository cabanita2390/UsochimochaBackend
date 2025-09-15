package com.app.usochicamochabackend.performance.application.dto;

import com.app.usochicamochabackend.order.application.dto.OrderResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ExecuteDTO(
        OrderResponse order,
        LocalDateTime date,
        String description,
        String timeSpent,
        LaborResponse labor,
        SparePartResponse sparePart
) {}