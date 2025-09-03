package com.app.usochicamochabackend.machine.application.dto;

import java.time.LocalDate;

public record MachineRequest(String name, String belongsTo, String model, LocalDate soat, String brand, LocalDate runt, String numEngine, String numInterIdentification) {
}
