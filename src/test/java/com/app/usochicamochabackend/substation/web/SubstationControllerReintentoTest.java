package com.app.usochicamochabackend.substation.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.substation.application.dto.EjecucionRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Dos envíos simultáneos del mismo registro desde el móvil (mismo uuidCliente). */
@ExtendWith(MockitoExtension.class)
class SubstationControllerReintentoTest {

    @Mock
    private SubstationEjecucionUseCase ejecucionUseCase;

    @InjectMocks
    private SubstationController controller;

    private final UUID uuid = UUID.randomUUID();
    private final EjecucionRequest request = new EjecucionRequest(LocalDate.now(), 10, 1, 1L, "CIVIL",
            "PREVENTIVO", "MANTENIMIENTO", 1L, null, null, "CONFORME", "ok", null, uuid);
    private final UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(new UserPrincipal(1L, "operario"), null, List.of());

    @Test
    void elEnvioQueChocaConElIndiceUnico_respondeConElRegistroYaGuardado() throws Exception {
        EjecucionResponse guardada = mock(EjecucionResponse.class);
        when(guardada.id()).thenReturn(70L);
        when(ejecucionUseCase.registrarEjecucion(any(), any())).thenThrow(new DataIntegrityViolationException("uuid_cliente"));
        when(ejecucionUseCase.buscarPorUuidCliente(uuid)).thenReturn(Optional.of(guardada));

        var r = controller.registrarEjecucion(request, auth);

        assertEquals(201, r.getStatusCode().value());
        assertSame(guardada, r.getBody());
    }

    @Test
    void otraViolacionDeDatos_sigueFallando() {
        when(ejecucionUseCase.registrarEjecucion(any(), any())).thenThrow(new DataIntegrityViolationException("otra"));
        when(ejecucionUseCase.buscarPorUuidCliente(uuid)).thenReturn(Optional.empty());

        assertThrows(DataIntegrityViolationException.class, () -> controller.registrarEjecucion(request, auth));
    }
}
