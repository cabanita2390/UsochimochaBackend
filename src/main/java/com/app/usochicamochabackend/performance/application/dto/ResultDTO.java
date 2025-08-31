package com.app.usochicamochabackend.performance.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ResultDTO(
        Long id,
        LocalDateTime date,
        String description,
        String timeSpent,
        List<LaborResponse> labors,
        List<SparePartResponse> spareParts
) {}
