package com.app.usochicamochabackend.vehicleinspection.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.moto.application.dto.InspeccionMotoRequest;
import com.app.usochicamochabackend.moto.application.service.MotoService;
import com.app.usochicamochabackend.moto.web.MotoController;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionRequest;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehiculoInspectionResponse;
import com.app.usochicamochabackend.vehicleinspection.application.port.CreateVehiculoInspectionUseCase;
import com.app.usochicamochabackend.vehicleinspection.application.service.VehiculoInspectionService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Dos envíos simultáneos de la misma inspección preoperativa (mismo uuidCliente) desde el móvil. */
class InspeccionReintentoTest {

    private static final String UUID_MOVIL = "1b4e28ba-2fa1-11d2-883f-0016d3cca427";

    @Test
    void vehiculo_elEnvioQueChocaConElIndiceUnico_respondeConLaYaRegistrada() throws Exception {
        CreateVehiculoInspectionUseCase useCase = mock(CreateVehiculoInspectionUseCase.class);
        VehiculoInspectionController controller = new VehiculoInspectionController(useCase, null, null, null);
        VehiculoInspectionRequest request = mock(VehiculoInspectionRequest.class);
        when(request.uuidCliente()).thenReturn(UUID_MOVIL);
        when(useCase.create(any(), any())).thenThrow(new DataIntegrityViolationException("uuid_cliente"));
        when(useCase.buscarPorUuidCliente(UUID_MOVIL)).thenReturn(Optional.of(new VehiculoInspectionResponse(70L, "ya estaba")));

        var r = controller.createInspection(request,
                new UsernamePasswordAuthenticationToken(new UserPrincipal(1L, "operario"), null, List.of()));

        assertEquals(201, r.getStatusCode().value());
        assertEquals(70L, r.getBody().idInspeccion());
    }

    @Test
    void moto_elEnvioQueChocaConElIndiceUnico_respondeConLaYaRegistrada() {
        MotoService motoService = mock(MotoService.class);
        MotoController controller = new MotoController(motoService, null, null, null, null);
        InspeccionMotoRequest request = mock(InspeccionMotoRequest.class);
        when(request.uuidCliente()).thenReturn(UUID_MOVIL);
        when(motoService.saveInspeccion(any())).thenThrow(new DataIntegrityViolationException("uuid_cliente"));
        when(motoService.inspeccionYaRegistrada(UUID_MOVIL)).thenReturn(Optional.of(71L));

        var r = controller.saveInspeccion(request);

        assertEquals(201, r.getStatusCode().value());
        assertEquals(71L, r.getBody());
    }

    @Test
    void uuidCliente_seLimpiaYSeIgnoraSiNoCabe() {
        assertEquals(UUID_MOVIL, VehiculoInspectionService.uuidCliente("  " + UUID_MOVIL + " "));
        assertNull(VehiculoInspectionService.uuidCliente(" "));
        assertNull(VehiculoInspectionService.uuidCliente("x".repeat(37)));
    }
}
