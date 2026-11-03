package com.app.usochicamochabackend.substation.application.dto;

/** Operario que se puede asignar a una orden de trabajo. */
public record OperarioResponse(Long id, String nombre, String username) {
}
