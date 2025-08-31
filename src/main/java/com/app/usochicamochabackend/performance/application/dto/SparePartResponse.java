package com.app.usochicamochabackend.performance.application.dto;

import java.math.BigDecimal;

public record SparePartResponse(String ref, String name, Integer quantity, BigDecimal price) {}