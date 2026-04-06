package com.app.usochicamochabackend.moto;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.moto.application.dto.InspeccionMotoRequest;
import com.app.usochicamochabackend.moto.application.service.MotoService;
import com.app.usochicamochabackend.moto.infrastructure.entity.TipoVehiculoEntity;
import com.app.usochicamochabackend.moto.infrastructure.entity.UbicacionEntity;
import com.app.usochicamochabackend.moto.infrastructure.entity.VehiculoEntity;
import com.app.usochicamochabackend.moto.infrastructure.repository.MotoInspeccionRepository;
import com.app.usochicamochabackend.moto.infrastructure.repository.TipoVehiculoRepository;
import com.app.usochicamochabackend.moto.infrastructure.repository.UbicacionRepository;
import com.app.usochicamochabackend.moto.infrastructure.repository.VehiculoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MotoE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private TipoVehiculoRepository tipoVehiculoRepository;

    @Autowired
    private MotoInspeccionRepository inspeccionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Integer testVehiculoId;
    private Integer testUbicacionId;

    @BeforeEach
    void setUp() {
        // 1. Create TipoVehiculo
        TipoVehiculoEntity tipo = TipoVehiculoEntity.builder()
                .nombreTipo("MOTOCICLETA")
                .activo(true)
                .build();
        tipoVehiculoRepository.save(tipo);

        // 2. Create Vehiculo
        VehiculoEntity moto = VehiculoEntity.builder()
                .placa("E2E-123")
                .tipoVehiculo(tipo)
                .kilometrajeActual(100)
                .activo(true)
                .build();
        vehiculoRepository.save(moto);
        testVehiculoId = moto.getId();

        // 3. Create Ubicacion
        UbicacionEntity ubicacion = UbicacionEntity.builder()
                .nombreUbicacion("Patio E2E")
                .activo(true)
                .build();
        ubicacionRepository.save(ubicacion);
        testUbicacionId = ubicacion.getId();
    }

    @Test
    void fullMotorcycleInspectionFlow_ShouldWork() throws Exception {
        // Setup Security Context with real UserPrincipal expected by MotoService
        UserPrincipal principal = new UserPrincipal(1L, "admin");
        var auth = new UsernamePasswordAuthenticationToken(
                principal, null, Set.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // STEP 1: Get Placas
        mockMvc.perform(get("/api/v1/moto/placas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.placa=='E2E-123')]").exists());

        // STEP 2: Get Ubicaciones
        mockMvc.perform(get("/api/v1/moto/ubicaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombreUbicacion=='Patio E2E')]").exists());

        // STEP 3: Save Inspection
        InspeccionMotoRequest request = new InspeccionMotoRequest(
                testVehiculoId,
                150,
                "EXCELENTE",
                "Prueba E2E terminada",
                "Vigente",
                "Vigente",
                "Vigente",
                "N/A",
                "Bueno",
                "Bueno",
                "Bueno",
                testUbicacionId
        );

        mockMvc.perform(post("/api/v1/moto/inspeccion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // STEP 4: Verify in Database
        var inspections = inspeccionRepository.findAll();
        assertThat(inspections).isNotEmpty();
        var lastInspection = inspections.get(inspections.size() - 1);
        
        assertThat(lastInspection.getVehiculo().getPlaca()).isEqualTo("E2E-123");
        assertThat(lastInspection.getKilometrajeReportado()).isEqualTo(150);
        assertThat(lastInspection.getObservacionesFinales()).isEqualTo("Prueba E2E terminada");
        assertThat(lastInspection.getAprobadoRuta()).isNull();
    }
}
