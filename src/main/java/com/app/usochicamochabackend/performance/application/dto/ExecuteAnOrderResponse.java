package com.app.usochicamochabackend.performance.application.dto;

import java.time.LocalDate;
import java.util.List;

public record ExecuteAnOrderResponse(
        LocalDate date,
        String timeSpent,
        List<LaborResponse> labors,
        List<SparePartResponse> spareParts
) {}