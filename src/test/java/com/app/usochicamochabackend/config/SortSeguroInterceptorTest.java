package com.app.usochicamochabackend.config;

import com.app.usochicamochabackend.exception.BadRequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortSeguroInterceptorTest {

    private final SortSeguroInterceptor interceptor = new SortSeguroInterceptor();

    private boolean pasa(String... sort) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/substation/ejecuciones");
        request.addParameter("sort", sort);
        return interceptor.preHandle(request, new MockHttpServletResponse(), new Object());
    }

    @ParameterizedTest
    @ValueSource(strings = {"usuario.password", "usuario.password,asc", "fecha,usuario.password,desc", "password",
            "usuario.PASSWORD", " usuario . password ", "orden.creadoPor.password,desc"})
    void rechazaOrdenarPorLaContrasena(String sort) {
        assertThrows(BadRequestException.class, () -> pasa(sort));
    }

    @ParameterizedTest
    @ValueSource(strings = {"fecha,desc", "id,desc", "estacion.nombre,asc", "actividad.nombre,desc", "usuario.username,asc",
            "descripcionLibre"})
    void dejaPasarLosOrdenamientosDeLaWeb(String sort) {
        assertThat(pasa(sort)).isTrue();
    }

    @org.junit.jupiter.api.Test
    void variosParametrosSort_revisaTodos() {
        assertThat(pasa("fecha,desc", "id,desc")).isTrue();
        assertThrows(BadRequestException.class, () -> pasa("fecha,desc", "usuario.password,asc"));
    }

    @org.junit.jupiter.api.Test
    void sinSort_pasa() {
        assertThat(interceptor.preHandle(new MockHttpServletRequest("GET", "/x"), new MockHttpServletResponse(), new Object())).isTrue();
    }
}
