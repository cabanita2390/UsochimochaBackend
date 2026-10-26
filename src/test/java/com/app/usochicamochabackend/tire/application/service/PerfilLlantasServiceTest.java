package com.app.usochicamochabackend.tire.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.catalog.infrastructure.entity.TipoVehiculoEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.tire.application.dto.*;
import com.app.usochicamochabackend.tire.infrastructure.entity.AssetLlantaConfigEntity;
import com.app.usochicamochabackend.tire.infrastructure.entity.LlantaPerfilEntity;
import com.app.usochicamochabackend.tire.infrastructure.entity.LlantaPerfilPosicionEntity;
import com.app.usochicamochabackend.tire.infrastructure.repository.AssetLlantaConfigRepository;
import com.app.usochicamochabackend.tire.infrastructure.repository.LlantaPerfilRepository;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilLlantasServiceTest {

    @Mock private LlantaPerfilRepository perfilRepository;
    @Mock private AssetLlantaConfigRepository assetConfigRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private MachineRepository machineRepository;
    @Mock private SaveActionUseCase saveActionUseCase;

    @InjectMocks
    private PerfilLlantasService service;

    private static PosicionLlantaRequest pos(String codigo, String min, String max) {
        return new PosicionLlantaRequest(codigo, "Llanta " + codigo, null, new BigDecimal(min), null, new BigDecimal(max));
    }

    private static int status(Runnable r) {
        return assertThrows(ResponseStatusException.class, r::run).getStatusCode().value();
    }

    @Test
    void crearPerfil_NormalizaCodigosYAsignaOrden() {
        when(perfilRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PerfilLlantasResponse r = service.crearPerfil(new PerfilLlantasRequest(" Moto 2 llantas ", "motocicleta", null,
                List.of(pos("del", "28", "33"), pos("tra", "30", "36"))));

        assertEquals("Moto 2 llantas", r.nombre());
        assertEquals("MOTOCICLETA", r.aplicaA());
        assertEquals(List.of("DEL", "TRA"), r.posiciones().stream().map(PosicionLlantaResponse::codigo).toList());
        assertEquals(List.of(1, 2), r.posiciones().stream().map(PosicionLlantaResponse::orden).toList());
        verify(saveActionUseCase).save(anyString());
    }

    @Test
    void crearPerfil_RechazaDatosInvalidos() {
        assertEquals(400, status(() -> service.crearPerfil(new PerfilLlantasRequest("X", "CAMION", null, List.of(pos("A", "1", "2"))))));
        assertEquals(400, status(() -> service.crearPerfil(new PerfilLlantasRequest("X", "VEHICULO", null, List.of()))));
        assertEquals(400, status(() -> service.crearPerfil(new PerfilLlantasRequest("X", "VEHICULO", null,
                List.of(pos("A", "1", "2"), pos("a", "1", "2"))))));
        assertEquals(400, status(() -> service.crearPerfil(new PerfilLlantasRequest("X", "VEHICULO", null, List.of(pos("A", "40", "30"))))));
        assertEquals(400, status(() -> service.crearPerfil(new PerfilLlantasRequest("X", "VEHICULO", null, List.of(pos("EJE 1", "30", "40"))))));
        assertEquals(400, status(() -> service.crearPerfil(new PerfilLlantasRequest("X", "VEHICULO", null,
                List.of(new PosicionLlantaRequest("A", "A", null, new BigDecimal("30"), new BigDecimal("50"), new BigDecimal("40")))))));
        verifyNoInteractions(saveActionUseCase);
    }

    @Test
    void crearPerfil_NombreRepetidoEsConflicto() {
        when(perfilRepository.existsByNombreIgnoreCaseAndStatusTrue("Tractor")).thenReturn(true);
        assertEquals(409, status(() -> service.crearPerfil(new PerfilLlantasRequest("Tractor", "MAQUINARIA", null, List.of(pos("A", "1", "2"))))));
    }

    @Test
    void actualizarPerfil_CasaPorCodigoYDesactivaLasQueSalen() {
        LlantaPerfilEntity perfil = LlantaPerfilEntity.builder().id(3L).nombre("Camioneta").aplicaA("VEHICULO")
                .posiciones(new ArrayList<>()).build();
        LlantaPerfilPosicionEntity del = LlantaPerfilPosicionEntity.builder().id(10L).perfil(perfil).codigo("DEL").etiqueta("Delantera")
                .orden(1).psiMin(new BigDecimal("30")).psiMax(new BigDecimal("35")).build();
        LlantaPerfilPosicionEntity rep = LlantaPerfilPosicionEntity.builder().id(11L).perfil(perfil).codigo("REP").etiqueta("Repuesto")
                .orden(2).psiMin(new BigDecimal("30")).psiMax(new BigDecimal("35")).build();
        perfil.getPosiciones().addAll(List.of(del, rep));
        when(perfilRepository.findByIdAndStatusTrue(3L)).thenReturn(Optional.of(perfil));
        when(perfilRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PerfilLlantasResponse r = service.actualizarPerfil(3L, new PerfilLlantasRequest("Camioneta", "VEHICULO", null,
                List.of(pos("DEL", "32", "36"), pos("TRA", "35", "40"))));

        assertEquals(List.of("DEL", "TRA"), r.posiciones().stream().map(PosicionLlantaResponse::codigo).toList());
        assertEquals(10L, del.getId());
        assertEquals(0, new BigDecimal("32").compareTo(del.getPsiMin()));
        assertFalse(rep.getStatus());
        assertEquals(3, perfil.getPosiciones().size());
    }

    @Test
    void actualizarPerfil_NoCambiaElTipoSiYaEstaAsignado() {
        LlantaPerfilEntity perfil = LlantaPerfilEntity.builder().id(3L).nombre("Camioneta").aplicaA("VEHICULO").build();
        when(perfilRepository.findByIdAndStatusTrue(3L)).thenReturn(Optional.of(perfil));
        when(assetConfigRepository.existsByPerfilIdAndStatusTrue(3L)).thenReturn(true);

        assertEquals(409, status(() -> service.actualizarPerfil(3L, new PerfilLlantasRequest("Camioneta", "MAQUINARIA", null,
                List.of(pos("A", "1", "2"))))));
    }

    @Test
    void desactivarPerfil_AsignadoEsConflicto() {
        when(perfilRepository.findByIdAndStatusTrue(3L)).thenReturn(Optional.of(LlantaPerfilEntity.builder().id(3L).build()));
        when(assetConfigRepository.existsByPerfilIdAndStatusTrue(3L)).thenReturn(true);

        assertEquals(HttpStatus.CONFLICT.value(), status(() -> service.desactivarPerfil(3L)));
        verify(perfilRepository, never()).save(any());
    }

    @Test
    void asignarVehiculo_ValidaQueElPerfilSeaDelTipoDelActivo() {
        VehicleEntity moto = VehicleEntity.builder().idVehiculo(4).placa("ABC12D")
                .tipoVehiculo(TipoVehiculoEntity.builder().nombreTipo("MOTOCICLETA").build()).build();
        when(vehicleRepository.findById(4)).thenReturn(Optional.of(moto));
        when(assetConfigRepository.findByVehicleId(4)).thenReturn(Optional.empty());
        when(perfilRepository.findByIdAndStatusTrue(3L)).thenReturn(Optional.of(
                LlantaPerfilEntity.builder().id(3L).nombre("Volqueta").aplicaA("VEHICULO").build()));

        assertEquals(400, status(() -> service.asignarVehiculo(4, new AsignacionLlantasRequest(3L))));
        verify(assetConfigRepository, never()).save(any());
    }

    @Test
    void asignarVehiculo_ReactivaYQuitaSinBorrar() {
        VehicleEntity volqueta = VehicleEntity.builder().idVehiculo(8).placa("XYZ123")
                .tipoVehiculo(TipoVehiculoEntity.builder().nombreTipo("VOLQUETA").build()).build();
        LlantaPerfilEntity perfil = LlantaPerfilEntity.builder().id(3L).nombre("Volqueta").aplicaA("VEHICULO")
                .posiciones(new ArrayList<>()).build();
        AssetLlantaConfigEntity existente = AssetLlantaConfigEntity.builder().id(20L).vehicleId(8).status(false).build();
        when(vehicleRepository.findById(8)).thenReturn(Optional.of(volqueta));
        when(assetConfigRepository.findByVehicleId(8)).thenReturn(Optional.of(existente));
        when(perfilRepository.findByIdAndStatusTrue(3L)).thenReturn(Optional.of(perfil));
        when(assetConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ConfigLlantasActivoResponse r = service.asignarVehiculo(8, new AsignacionLlantasRequest(3L));
        assertEquals(8, r.vehicleId());
        assertTrue(existente.getStatus());

        assertNull(service.asignarVehiculo(8, new AsignacionLlantasRequest(null)));
        assertFalse(existente.getStatus());
        verify(assetConfigRepository, never()).delete(any());
    }
}
