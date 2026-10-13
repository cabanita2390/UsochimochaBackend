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

/** Auditoría QA 2026-10-13: /uploads/** era de acceso público. */
class UploadsAccessFilterTest {

    private JwtUtils jwtUtils;
    private UserRepositoryJpa userRepository;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secretPassword", "test-secret-key-for-testing-purposes-only-minimum-32-chars");
        ReflectionTestUtils.setField(jwtUtils, "userGenerator", "test-issuer");
        userRepository = mock(UserRepositoryJpa.class);
        when(userRepository.existsActiveById(7L)).thenReturn(true);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(new UserPrincipal(7L, "op"), null,
                List.of(new SimpleGrantedAuthority("ROLE_OPERARIO")));
    }

    /** Devuelve el estado y si la petición siguió hasta el recurso (chain invocada). */
    private Object[] pedir(String modo, String uri, String k) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        if (k != null) request.setParameter("k", k);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        new UploadsAccessFilter(jwtUtils, userRepository, modo).doFilter(request, response, chain);
        return new Object[]{response.getStatus(), chain.getRequest() != null};
    }

    @Test
    @DisplayName("modo exigir: sin credencial → 401 y no se sirve el archivo")
    void exigir_sinCredencial_401() throws Exception {
        Object[] r = pedir("exigir", "/uploads/subestaciones/ejecuciones/1/a.jpg", null);
        assertEquals(401, r[0]);
        assertEquals(false, r[1]);
    }

    @Test
    @DisplayName("modo exigir: con ?k= de archivos válido → se sirve")
    void exigir_conTokenDeArchivos_pasa() throws Exception {
        Object[] r = pedir("exigir", "/uploads/subestaciones/ejecuciones/1/a.jpg", jwtUtils.createArchivosToken(auth()));
        assertEquals(200, r[0]);
        assertEquals(true, r[1]);
    }

    @Test
    @DisplayName("modo exigir: ?k= de un usuario desactivado o un refresh token → 401")
    void exigir_tokenInvalido_401() throws Exception {
        when(userRepository.existsActiveById(7L)).thenReturn(false);
        assertEquals(401, pedir("exigir", "/uploads/x/y/z.jpg", jwtUtils.createArchivosToken(auth()))[0]);
        when(userRepository.existsActiveById(7L)).thenReturn(true);
        assertEquals(401, pedir("exigir", "/uploads/x/y/z.jpg", jwtUtils.createRefreshToken(auth()))[0]);
        assertEquals(401, pedir("exigir", "/uploads/x/y/z.jpg", "basura")[0]);
    }

    @Test
    @DisplayName("modo exigir: autenticado por el header Authorization → se sirve")
    void exigir_autenticadoPorHeader_pasa() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(auth());
        assertEquals(true, pedir("exigir", "/uploads/x/y/z.jpg", null)[1]);
    }

    @Test
    @DisplayName("modo registrar (por defecto): sin credencial se sigue sirviendo, no rompe nada")
    void registrar_sinCredencial_pasa() throws Exception {
        Object[] r = pedir(null, "/uploads/subestaciones/ejecuciones/1/a.jpg", null);
        assertEquals(200, r[0]);
        assertEquals(true, r[1]);
    }

    @Test
    @DisplayName("no toca rutas fuera de /uploads")
    void otrasRutas_noSeFiltran() throws Exception {
        assertEquals(true, pedir("exigir", "/api/v1/substation/estaciones", null)[1]);
    }
}
