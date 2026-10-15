package com.app.usochicamochabackend.auth.utils;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtUtils {

    /**
     * Distingue el access token (15 min) del refresh token (7 días). Antes los dos eran idénticos
     * y el filtro aceptaba el refresh como Bearer: un usuario desactivado seguía escribiendo
     * 7 días con él, y un access token servía para pedir otro.
     */
    public static final String CLAIM_TIPO = "tipo";
    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";
    /** Solo sirve para ver archivos de /uploads (fotos, documentos): no da acceso a la API. */
    public static final String TIPO_ARCHIVOS = "archivos";
    private static final long VIDA_TOKEN_ARCHIVOS_MS = 12L * 60 * 60 * 1000;
    /** Tokens emitidos antes del claim "tipo": se reconocen por su vida (access 15 min, refresh 7 días). */
    private static final long VIDA_MAXIMA_ACCESS_MS = 60L * 60 * 1000;

    @Value("${jwt.secret.password}")
    private String secretPassword;

    @Value("${jwt.issuer.generator}")
    private String userGenerator;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secretPassword);
    }

    public String createToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String username = userPrincipal.username();
        Long id = userPrincipal.id();
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");


        return JWT.create()
                .withClaim("userId", id)
                .withSubject(username)
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("role", role)
                .withClaim(CLAIM_TIPO, TIPO_ACCESS)
                .withIssuer(userGenerator)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000*60*15)))
                .sign(getAlgorithm());
    }

    public String createRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String username = userPrincipal.username();
        Long id = userPrincipal.id();
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        return JWT.create()
                .withClaim("userId", id)
                .withSubject(username)
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("role", role)
                .withClaim(CLAIM_TIPO, TIPO_REFRESH)
                .withIssuer(userGenerator)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000*60*60*24*7)))
                .sign(getAlgorithm());
    }

    public DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(getAlgorithm())
                .withIssuer(userGenerator)
                .build();

        return verifier.verify(token);
    }

    /**
     * Token para ver archivos de /uploads. Las URL de imágenes (img src, window.open) no pueden
     * llevar el header Authorization: los clientes lo agregan como parámetro ?k=. Vive más que el
     * access token para que una página abierta no pierda sus fotos a los 15 minutos.
     */
    public String createArchivosToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return JWT.create()
                .withClaim("userId", userPrincipal.id())
                .withSubject(userPrincipal.username())
                .withJWTId(UUID.randomUUID().toString())
                .withClaim(CLAIM_TIPO, TIPO_ARCHIVOS)
                .withIssuer(userGenerator)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + VIDA_TOKEN_ARCHIVOS_MS))
                .sign(getAlgorithm());
    }

    /** Solo el access token da acceso a la API (no el refresh ni el de archivos). */
    public boolean esTokenDeAcceso(DecodedJWT decodedJWT) {
        String tipo = decodedJWT.getClaim(CLAIM_TIPO).asString();
        return tipo == null ? !esRefreshToken(decodedJWT) : TIPO_ACCESS.equals(tipo);
    }

    /** Sirve para ver archivos: el token de archivos o un access token. */
    public boolean sirveParaArchivos(DecodedJWT decodedJWT) {
        return TIPO_ARCHIVOS.equals(decodedJWT.getClaim(CLAIM_TIPO).asString()) || esTokenDeAcceso(decodedJWT);
    }

    public boolean esRefreshToken(DecodedJWT decodedJWT) {
        String tipo = decodedJWT.getClaim(CLAIM_TIPO).asString();
        if (tipo != null) {
            return TIPO_REFRESH.equals(tipo);
        }
        Date emitido = decodedJWT.getIssuedAt();
        Date expira = decodedJWT.getExpiresAt();
        return emitido != null && expira != null
                && expira.getTime() - emitido.getTime() > VIDA_MAXIMA_ACCESS_MS;
    }

    public Map<String, Claim> extractAllClaims(DecodedJWT decodedJWT){
        return decodedJWT.getClaims();
    }

    public String extractUsername(DecodedJWT decodedJWT) {
        return decodedJWT.getSubject();
    }

    public Claim extractSpecificClaim(DecodedJWT decodedJWT, String claimName){
        return decodedJWT.getClaim(claimName);
    }
}

