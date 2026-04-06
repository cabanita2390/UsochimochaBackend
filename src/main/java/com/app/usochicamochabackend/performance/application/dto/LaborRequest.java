package com.app.usochicamochabackend.performance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LaborRequest(BigDecimal price, Boolean sameMecanic, String contractor, String observations) {}