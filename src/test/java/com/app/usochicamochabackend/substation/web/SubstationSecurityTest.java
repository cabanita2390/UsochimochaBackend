package com.app.usochicamochabackend.substation.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.security.SecurityConfig;
import com.app.usochicamochabackend.auth.utils.JwtUtils;
import com.app.usochicamochabackend.auth.application.service.UserDetailsServiceImp;
import com.app.usochicamochabackend.substation.application.port.SubstationCatalogAdminUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationCatalogUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationHallazgoUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationCronogramaUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationIndicadoresUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * Reglas de SecurityConfig para /api/v1/substation (SUB-01). Varias rutas de escritura
 * (cronograma, hallazgos, catálogos) todavía no existen en el controller: para esas, "permitido"
 * significa que la seguridad deja pasar la petición (llega al 404/400/500 de la app) y
 * "denegado" que responde 403 antes de llegar al controller.
 */
@WebMvcTest({SubstationController.class, SubstationCatalogAdminController.class, SubstationHallazgoController.class,
        SubstationCronogramaController.class})
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class SubstationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubstationCatalogUseCase catalogUseCase;

    @MockBean
    private SubstationCatalogAdminUseCase catalogAdminUseCase;

    @MockBean
    private SubstationHallazgoUseCase hallazgoUseCase;

    @MockBean
    private SubstationCronogramaUseCase cronogramaUseCase;

    @MockBean
    private SubstationEjecucionUseCase ejecucionUseCase;

    @MockBean
    private SubstationIndicadoresUseCase indicadoresUseCase;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImp userDetailsServiceImp;

    @ParameterizedTest(name = "{0} {1} como {2} → permitido={3}")
    @DisplayName("Permisos por endpoint de Subestaciones")
    @CsvSource({
            // Lecturas: móvil y web
            "GET,    /api/v1/substation/estaciones,                 OPERARIO,             true",
            "GET,    /api/v1/substation/indicadores/cumplimiento,   OPERARIO,             true",
            "GET,    /api/v1/substation/cronograma?anio=2026,       SUPERVISOR_OPERATIVO, true",
            "GET,    /api/v1/substation/cronograma?anio=2026,       OPERARIO,             true",
            // Captura y edición desde el móvil
            "POST,   /api/v1/substation/ejecuciones,                OPERARIO,             true",
            "POST,   /api/v1/substation/ejecuciones/1/evidencia,    OPERARIO,             true",
            "PUT,    /api/v1/substation/ejecuciones/1,              OPERARIO,             true",
            "PUT,    /api/v1/substation/ejecuciones/1,              SUPERVISOR_OPERATIVO, true",
            // Seguimiento de hallazgos: web, sin OPERARIO
            "PUT,    /api/v1/substation/hallazgos/1/resolver,       SUPERVISOR_OPERATIVO, true",
            "PUT,    /api/v1/substation/hallazgos/1/en-proceso,     ADMIN,                true",
            "PUT,    /api/v1/substation/hallazgos/1/resolver,       OPERARIO,             false",
            // Cronograma: solo ADMIN
            "POST,   /api/v1/substation/cronograma/citas,           ADMIN,                true",
            "POST,   /api/v1/substation/cronograma/citas,           SUPERVISOR_OPERATIVO, false",
            "POST,   /api/v1/substation/cronograma/citas,           OPERARIO,             false",
            "POST,   /api/v1/substation/cronograma/publicar,        SUPERVISOR_OPERATIVO, false",
            "DELETE, /api/v1/substation/cronograma/citas/1,         SUPERVISOR_OPERATIVO, false",
            "DELETE, /api/v1/substation/cronograma/citas/1,         ADMIN,                true",
            "POST,   /api/v1/substation/cronograma/citas/1/restaurar, ADMIN,              true",
            "POST,   /api/v1/substation/cronograma/citas/1/restaurar, SUPERVISOR_OPERATIVO, false",
            "POST,   /api/v1/substation/cronograma/copiar,          ADMIN,                true",
            "POST,   /api/v1/substation/cronograma/copiar,          OPERARIO,             false",
            "DELETE, /api/v1/substation/cronograma/borrador?anio=2026, ADMIN,             true",
            "DELETE, /api/v1/substation/cronograma/borrador?anio=2026, SUPERVISOR_OPERATIVO, false",
            // Catálogos: solo ADMIN
            "POST,   /api/v1/substation/estaciones,                 ADMIN,                true",
            "POST,   /api/v1/substation/estaciones,                 SUPERVISOR_OPERATIVO, false",
            "PUT,    /api/v1/substation/actividades/1,              SUPERVISOR_OPERATIVO, false",
            "PATCH,  /api/v1/substation/estaciones/1/estado,        OPERARIO,             false",
            "PATCH,  /api/v1/substation/estaciones/1/estado,        ADMIN,                true",
    })
    void permisosPorEndpoint(String metodo, String ruta, String rol, boolean permitido) throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                new UserPrincipal(1L, "usuario-" + rol.toLowerCase()),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol)));

        int status = mockMvc.perform(request(HttpMethod.valueOf(metodo), ruta)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn().getResponse().getStatus();

        if (permitido) {
            assertThat(status).as("%s %s como %s no debe dar 401/403", metodo, ruta, rol).isNotIn(401, 403);
        } else {
            assertThat(status).as("%s %s como %s debe dar 403", metodo, ruta, rol).isEqualTo(403);
        }
    }
}
