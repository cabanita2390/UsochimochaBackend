package com.app.usochicamochabackend.vehicleinspection.application.dto;

/**
 * Respuesta del endpoint de validación de kilometraje.
 *
 * @param alerta  {@code true} si el kilometraje ingresado es menor al
 *                registrado;
 *                {@code false} si es correcto.
 * @param mensaje Descripción legible del resultado de la validación.
 */
public record KilometrajeValidacionResponse(boolean alerta, String mensaje) {
}
