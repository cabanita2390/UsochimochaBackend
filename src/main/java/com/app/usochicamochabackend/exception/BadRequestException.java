package com.app.usochicamochabackend.exception;

/** Datos de entrada inválidos: GlobalExceptionHandler la responde como 400 con su mensaje. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
