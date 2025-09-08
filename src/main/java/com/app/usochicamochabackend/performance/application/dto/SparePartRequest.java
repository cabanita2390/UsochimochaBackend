package com.app.usochicamochabackend.performance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SparePartRequest(String ref, String name, Integer quantity, BigDecimal price) {}