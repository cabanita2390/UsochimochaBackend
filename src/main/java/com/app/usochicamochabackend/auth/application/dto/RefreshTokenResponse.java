package com.app.usochicamochabackend.auth.application.dto;

/** archivosToken: ver JwtUtils.createArchivosToken (los clientes lo usan en las URL de /uploads). */
public record RefreshTokenResponse(String accessToken, String archivosToken) {
}
