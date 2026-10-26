package com.app.usochicamochabackend.tire.application.service;

/** Resultado de comparar una presión medida contra el rango de su posición. */
public enum ResultadoPresionLlanta {
    OK,
    BAJA,
    ALTA,
    /** La posición no tenía rango configurado: queda la lectura, sin evaluación. */
    SIN_RANGO
}
