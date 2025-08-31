package com.app.usochicamochabackend.performance.application.dto;

import com.app.usochicamochabackend.order.application.dto.OrderDTO;

import java.time.LocalDateTime;
import java.util.List;

public record ExecuteDTO(
        OrderDTO order,
        LocalDateTime date,
        String description,
        String timeSpent,
        List<LaborResponse> labors,
        List<SparePartResponse> spareParts
) {}