package com.app.usochicamochabackend.performance.application.dto;

import java.time.LocalDateTime;

public record LaborRequest(LocalDateTime date, String price, Boolean sameMecanic, String contractor, String observations) {
}
