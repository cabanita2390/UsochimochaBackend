package com.app.usochicamochabackend.auth.security.filter;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.auth.utils.JwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Auditoría QA 2026-10-13: un usuario desactivado seguía escribiendo 7 días usando su refresh
 * token como Bearer (el filtro no distinguía tipos ni miraba el estado del usuario).
 */
class JwtTokenValidatorTest {

    private JwtUtils jwtUtils;
    private UserRepositoryJpa userRepository;
    private JwtTokenValidator filtro;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secretPassword", "test-secret-key-for-testing-purposes-only-minimum-32-chars");
        ReflectionTestUtils.setField(jwtUtils, "userGenerator", "test-issuer");
        userRepository = mock(UserRepositoryJpa.class);
        filtro = new JwtTokenValidator(jwtUtils, userRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UsernamePasswordAuthenticationToken auth(long id) {
        return new UsernamePasswordAuthenticationToken(new UserPrincipal(id, "op"), null,
                List.of(new SimpleGrantedAuthority("ROLE_OPERARIO")));
    }

    private MockHttpServletResponse ejecutar(String token) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/substation/ejecuciones");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filtro.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    @DisplayName("access token de usuario activo: pasa y autentica")
    void accessTokenActivo_Pasa() throws Exception {
        when(userRepository.existsActiveById(7L)).thenReturn(true);

        MockHttpServletResponse response = ejecutar(jwtUtils.createToken(auth(7L)));

        assertEquals(200, response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("refresh token usado como Bearer: 401")
    void refreshTokenComoBearer_401() throws Exception {
        when(userRepository.existsActiveById(7L)).thenReturn(true);

        MockHttpServletResponse response = ejecutar(jwtUtils.createRefreshToken(auth(7L)));

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("usuario desactivado con access token vigente: 401 de inmediato")
    void usuarioDesactivado_401() throws Exception {
        when(userRepository.existsActiveById(7L)).thenReturn(false);

        MockHttpServletResponse response = ejecutar(jwtUtils.createToken(auth(7L)));

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
