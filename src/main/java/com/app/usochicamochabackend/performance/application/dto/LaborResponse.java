package com.app.usochicamochabackend.performance.application.dto;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LaborResponse(Long id, LocalDateTime date, BigDecimal price, Boolean sameMecanic, UserEntity user, String contractor, String observations) {}