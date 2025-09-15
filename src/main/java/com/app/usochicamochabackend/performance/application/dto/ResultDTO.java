package com.app.usochicamochabackend.performance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ResultDTO(
        Long id,
        LocalDateTime date,
        String description,
        String timeSpent,
        LaborResponse labor,
        SparePartResponse sparePart,
        BigDecimal totalPrice
) {}
