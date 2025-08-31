package com.app.usochicamochabackend.performance.application.dto;

import java.time.LocalDateTime;

public record LaborRequest(String price, Boolean sameMecanic, String contractor, String observations) {
}
