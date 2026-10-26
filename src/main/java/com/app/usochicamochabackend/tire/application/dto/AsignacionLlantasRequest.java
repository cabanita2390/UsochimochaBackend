package com.app.usochicamochabackend.tire.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AsignacionLlantasRequest", description = "Perfil a asignar al activo; null quita la asignación.")
public record AsignacionLlantasRequest(Long perfilId) {
}
