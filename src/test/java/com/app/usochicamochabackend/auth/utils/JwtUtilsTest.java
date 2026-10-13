package com.app.usochicamochabackend.auth.utils;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secretPassword", "test-secret-key-for-testing-purposes-only-minimum-32-chars");
        ReflectionTestUtils.setField(jwtUtils, "userGenerator", "test-issuer");
    }

    private Authentication buildAuthentication(Long id, String username, String role) {
        UserPrincipal principal = new UserPrincipal(id, username);
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role)));
    }

    @Test
    @DisplayName("createToken: genera token no vacío con subject correcto")
    void createToken_GeneraTokenValido() {
        Authentication auth = buildAuthentication(1L, "admin", "ROLE_ADMIN");
        String token = jwtUtils.createToken(auth);

        assertNotNull(token);
        assertFalse(token.isBlank());

        DecodedJWT decoded = jwtUtils.verifyToken(token);
        assertEquals("admin", decoded.getSubject());
        assertEquals("ROLE_ADMIN", decoded.getClaim("role").asString());
        assertEquals(1L, decoded.getClaim("userId").asLong());
    }

    @Test
    @DisplayName("createRefreshToken: expira en ~7 días")
    void createRefreshToken_ExpiraEnSieteDias() {
        Authentication auth = buildAuthentication(2L, "operario", "ROLE_OPERARIO");
        String refreshToken = jwtUtils.createRefreshToken(auth);

        assertNotNull(refreshToken);
        DecodedJWT decoded = jwtUtils.verifyToken(refreshToken);
        assertEquals("operario", decoded.getSubject());

        long diffMs = decoded.getExpiresAt().getTime() - decoded.getIssuedAt().getTime();
        long sevenDaysMs = 7L * 24 * 60 * 60 * 1000;
        assertTrue(diffMs >= sevenDaysMs - 1000 && diffMs <= sevenDaysMs + 1000);
    }

    @Test
    @DisplayName("verifyToken: token con firma inválida lanza excepción")
    void verifyToken_TokenManipulado_LanzaExcepcion() {
        Authentication auth = buildAuthentication(1L, "user", "ROLE_USER");
        String token = jwtUtils.createToken(auth);
        String manipulated = token.substring(0, token.lastIndexOf('.') + 1) + "fakeSignature";

        assertThrows(Exception.class, () -> jwtUtils.verifyToken(manipulated));
    }

    @Test
    @DisplayName("extractUsername: devuelve el username del token")
    void extractUsername_DebeRetornarUsername() {
        Authentication auth = buildAuthentication(1L, "supervisor", "ROLE_SUPERVISOR");
        String token = jwtUtils.createToken(auth);
        DecodedJWT decoded = jwtUtils.verifyToken(token);

        assertEquals("supervisor", jwtUtils.extractUsername(decoded));
    }

    @Test
    @DisplayName("extractSpecificClaim: devuelve userId del token")
    void extractSpecificClaim_DevuelveUserId() {
        Authentication auth = buildAuthentication(42L, "driver", "ROLE_USER");
        String token = jwtUtils.createToken(auth);
        DecodedJWT decoded = jwtUtils.verifyToken(token);

        assertEquals(42L, jwtUtils.extractSpecificClaim(decoded, "userId").asLong());
    }

    @Test
    @DisplayName("esRefreshToken: distingue el refresh (7 días) del access (15 min)")
    void esRefreshToken_DistingueLosDosTokens() {
        Authentication auth = buildAuthentication(2L, "operario", "ROLE_OPERARIO");

        assertTrue(jwtUtils.esRefreshToken(jwtUtils.verifyToken(jwtUtils.createRefreshToken(auth))));
        assertFalse(jwtUtils.esRefreshToken(jwtUtils.verifyToken(jwtUtils.createToken(auth))));
    }

    @Test
    @DisplayName("esRefreshToken: reconoce por su vida los tokens emitidos antes del claim tipo")
    void esRefreshToken_TokensSinTipo() {
        com.auth0.jwt.algorithms.Algorithm alg =
                com.auth0.jwt.algorithms.Algorithm.HMAC256("test-secret-key-for-testing-purposes-only-minimum-32-chars");
        long ahora = System.currentTimeMillis();
        String refreshViejo = com.auth0.jwt.JWT.create().withIssuer("test-issuer").withSubject("op")
                .withIssuedAt(new java.util.Date(ahora))
                .withExpiresAt(new java.util.Date(ahora + 7L * 24 * 60 * 60 * 1000)).sign(alg);
        String accessViejo = com.auth0.jwt.JWT.create().withIssuer("test-issuer").withSubject("op")
                .withIssuedAt(new java.util.Date(ahora))
                .withExpiresAt(new java.util.Date(ahora + 15L * 60 * 1000)).sign(alg);

        assertTrue(jwtUtils.esRefreshToken(jwtUtils.verifyToken(refreshViejo)));
        assertFalse(jwtUtils.esRefreshToken(jwtUtils.verifyToken(accessViejo)));
    }
}
