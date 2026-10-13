package com.app.usochicamochabackend.fuel.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.application.service.UserDetailsServiceImp;
import com.app.usochicamochabackend.auth.security.SecurityConfig;
import com.app.usochicamochabackend.auth.utils.JwtUtils;
import com.app.usochicamochabackend.fuel.application.port.ManageAssetFuelConfigUseCase;
import com.app.usochicamochabackend.fuel.application.port.ManageFuelMonthlyDiscountUseCase;
import com.app.usochicamochabackend.fuel.application.port.RegisterFuelReintegrationUseCase;
import com.app.usochicamochabackend.fuel.application.port.RegisterRefuelingRecordUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SUPERVISOR_OPERATIVO tiene CRUD completo en combustibles (igual que ADMIN): maneja mucha
 * información y debe poder corregir sus errores. Se prueba con el SecurityConfig real: es el
 * que decide el acceso (los @PreAuthorize no se aplican, no hay @EnableMethodSecurity).
 * El OPERARIO sigue sin poder borrar ni configurar.
 */
@WebMvcTest({RefuelingRecordController.class, AssetFuelConfigController.class,
        FuelMonthlyDiscountController.class, FuelReintegrationController.class})
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class FuelSupervisorPermisosTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private RegisterRefuelingRecordUseCase registerRefuelingRecordUseCase;
    @MockBean private ManageAssetFuelConfigUseCase manageAssetFuelConfigUseCase;
    @MockBean private ManageFuelMonthlyDiscountUseCase manageFuelMonthlyDiscountUseCase;
    @MockBean private RegisterFuelReintegrationUseCase registerFuelReintegrationUseCase;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private UserDetailsServiceImp userDetailsServiceImp;

    private static RequestPostProcessor as(String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                new UserPrincipal(5L, role.toLowerCase()), null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private static final String CONFIG_JSON =
            "{\"fuelTypeDefaultId\":1,\"consumoEstandar\":8.5,\"unidadConsumo\":\"KM_POR_GALON\",\"tanqueCapacidadGal\":20}";

    @Test
    @DisplayName("SUPERVISOR_OPERATIVO elimina un tanqueo (antes solo ADMIN)")
    void supervisor_EliminaTanqueo() throws Exception {
        mockMvc.perform(delete("/api/v1/fuel/refueling/{id}", 1L).with(csrf()).with(as("SUPERVISOR_OPERATIVO")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("SUPERVISOR_OPERATIVO consulta y configura el consumo estándar (antes solo ADMIN)")
    void supervisor_ConfiguraConsumoEstandar() throws Exception {
        mockMvc.perform(get("/api/v1/fuel/config").with(as("SUPERVISOR_OPERATIVO")))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/fuel/config/vehicle/{id}", 3).with(csrf()).with(as("SUPERVISOR_OPERATIVO"))
                        .contentType(MediaType.APPLICATION_JSON).content(CONFIG_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/v1/fuel/config/machine/{id}", 4L).with(csrf()).with(as("SUPERVISOR_OPERATIVO"))
                        .contentType(MediaType.APPLICATION_JSON).content(CONFIG_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SUPERVISOR_OPERATIVO registra y elimina descuentos mensuales y registra reintegros")
    void supervisor_DescuentosYReintegros() throws Exception {
        mockMvc.perform(post("/api/v1/fuel/monthly-discount").with(csrf()).with(as("SUPERVISOR_OPERATIVO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fechaInicio\":\"2026-10-01\",\"fechaFin\":\"2026-10-31\",\"monto\":1000}"))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/v1/fuel/monthly-discount/{id}", 2L).with(csrf()).with(as("SUPERVISOR_OPERATIVO")))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/fuel/reintegros").with(csrf()).with(as("SUPERVISOR_OPERATIVO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refuelingId\":1,\"cantidadReintegrada\":2,\"motivo\":\"sobrante\"}"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("OPERARIO sigue sin poder eliminar tanqueos ni configurar consumo estándar")
    void operario_NoEliminaNiConfigura() throws Exception {
        mockMvc.perform(delete("/api/v1/fuel/refueling/{id}", 1L).with(csrf()).with(as("OPERARIO")))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/v1/fuel/config/vehicle/{id}", 3).with(csrf()).with(as("OPERARIO"))
                        .contentType(MediaType.APPLICATION_JSON).content(CONFIG_JSON))
                .andExpect(status().isForbidden());
    }
}
