package com.app.usochicamochabackend.tire.application.service;

import com.app.usochicamochabackend.tire.application.dto.LecturaPresionLlantaRequest;
import com.app.usochicamochabackend.tire.application.dto.LecturaPresionLlantaResponse;
import com.app.usochicamochabackend.tire.infrastructure.entity.AssetLlantaConfigEntity;
import com.app.usochicamochabackend.tire.infrastructure.entity.InspPresionLlantaEntity;
import com.app.usochicamochabackend.tire.infrastructure.entity.LlantaPerfilEntity;
import com.app.usochicamochabackend.tire.infrastructure.entity.LlantaPerfilPosicionEntity;
import com.app.usochicamochabackend.tire.infrastructure.repository.AssetLlantaConfigRepository;
import com.app.usochicamochabackend.tire.infrastructure.repository.InspPresionLlantaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresionLlantasServiceTest {

    @Mock private AssetLlantaConfigRepository assetConfigRepository;
    @Mock private InspPresionLlantaRepository lecturaRepository;

    @InjectMocks
    private PresionLlantasService service;

    private AssetLlantaConfigEntity configMoto;

    @BeforeEach
    void setUp() {
        LlantaPerfilEntity perfil = LlantaPerfilEntity.builder().id(1L).nombre("Moto 2 llantas")
                .aplicaA("MOTOCICLETA").posiciones(new ArrayList<>()).build();
        perfil.getPosiciones().add(posicion(perfil, "DEL", "Delantera", 1, "28", "33"));
        perfil.getPosiciones().add(posicion(perfil, "TRA", "Trasera", 2, "30", "36"));
        LlantaPerfilPosicionEntity retirada = posicion(perfil, "SIDECAR", "Sidecar", 3, "20", "25");
        retirada.setStatus(false);
        perfil.getPosiciones().add(retirada);
        configMoto = AssetLlantaConfigEntity.builder().id(5L).vehicleId(4).perfil(perfil).build();
    }

    private static LlantaPerfilPosicionEntity posicion(LlantaPerfilEntity perfil, String codigo, String etiqueta,
                                                      int orden, String min, String max) {
        return LlantaPerfilPosicionEntity.builder().perfil(perfil).codigo(codigo).etiqueta(etiqueta).orden(orden)
                .psiMin(new BigDecimal(min)).psiMax(new BigDecimal(max)).build();
    }

    private static LecturaPresionLlantaRequest lectura(String codigo, String psi) {
        return new LecturaPresionLlantaRequest(codigo, psi == null ? null : new BigDecimal(psi), null, null, null);
    }

    @SuppressWarnings("unchecked")
    private List<InspPresionLlantaEntity> guardadas() {
        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(lecturaRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    void registrar_EvaluaContraElPerfilYCopiaElRango() {
        when(assetConfigRepository.findByVehicleId(4)).thenReturn(Optional.of(configMoto));
        when(lecturaRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<LecturaPresionLlantaResponse> r = service.registrarParaInspeccionVehiculo(4, 99L,
                List.of(lectura("del", "22"), lectura("TRA", "32.04")));

        assertEquals(2, r.size());
        assertEquals("BAJA", r.get(0).resultado());
        assertEquals("Delantera", r.get(0).etiquetaPosicion());
        assertEquals(0, new BigDecimal("28").compareTo(r.get(0).psiMin()));
        assertEquals("OK", r.get(1).resultado());
        assertEquals(new BigDecimal("32.0"), r.get(1).psiMedido());
        assertEquals("BAJA", service.resumen(r));
        guardadas().forEach(e -> assertEquals(99L, e.getIdInspeccionVehiculo()));
    }

    @Test
    void registrar_OmiteLecturasInvalidasYRepetidasSinFallar() {
        when(assetConfigRepository.findByVehicleId(4)).thenReturn(Optional.of(configMoto));
        when(lecturaRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<LecturaPresionLlantaResponse> r = service.registrarParaInspeccionVehiculo(4, 99L, List.of(
                lectura("DEL", null), lectura("DEL", "-3"), lectura("DEL", "999"),
                lectura(" ", "30"), lectura("TRA", "40"), lectura("TRA", "31")));

        assertEquals(1, r.size());
        assertEquals("TRA", r.get(0).codigoPosicion());
        assertEquals("ALTA", r.get(0).resultado());
    }

    @Test
    void registrar_PosicionFueraDelPerfilUsaElRangoDelMovilOQuedaSinRango() {
        when(assetConfigRepository.findByVehicleId(4)).thenReturn(Optional.of(configMoto));
        when(lecturaRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<LecturaPresionLlantaResponse> r = service.registrarParaInspeccionVehiculo(4, 99L, List.of(
                new LecturaPresionLlantaRequest("SIDECAR", new BigDecimal("18"), "Sidecar",
                        new BigDecimal("20"), new BigDecimal("25")),
                new LecturaPresionLlantaRequest("EXTRA", new BigDecimal("18"), null, null, null)));

        assertEquals("BAJA", r.get(0).resultado());
        assertEquals("Sidecar", r.get(0).etiquetaPosicion());
        assertEquals("SIN_RANGO", r.get(1).resultado());
        assertEquals("EXTRA", r.get(1).etiquetaPosicion());
    }

    @Test
    void registrar_SinPerfilAsignadoConservaLasLecturas() {
        when(assetConfigRepository.findByMachineId(7L)).thenReturn(Optional.empty());
        when(lecturaRepository.existsByIdInspeccionMaquina(50L)).thenReturn(false);
        when(lecturaRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<LecturaPresionLlantaResponse> r = service.registrarParaInspeccionMaquina(7L, 50L, List.of(lectura("DEL_IZQ", "20")));

        assertEquals("SIN_RANGO", r.get(0).resultado());
        assertEquals(50L, guardadas().get(0).getIdInspeccionMaquina());
    }

    @Test
    void registrar_PerfilQuitadoNoSeUsaParaEvaluar() {
        configMoto.setStatus(false);
        when(assetConfigRepository.findByVehicleId(4)).thenReturn(Optional.of(configMoto));
        when(lecturaRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<LecturaPresionLlantaResponse> r = service.registrarParaInspeccionVehiculo(4, 99L, List.of(lectura("DEL", "22")));

        assertEquals("SIN_RANGO", r.get(0).resultado());
    }

    @Test
    void registrar_SinLecturasNoConsultaNiGuarda() {
        assertTrue(service.registrarParaInspeccionVehiculo(4, 99L, null).isEmpty());
        assertTrue(service.registrarParaInspeccionVehiculo(4, 99L, List.of()).isEmpty());
        verifyNoInteractions(lecturaRepository);
    }

    @Test
    void registrar_ReintentoDeMaquinaNoDuplica() {
        when(lecturaRepository.existsByIdInspeccionMaquina(50L)).thenReturn(true);

        assertTrue(service.registrarParaInspeccionMaquina(7L, 50L, List.of(lectura("DEL", "20"))).isEmpty());
        verify(lecturaRepository, never()).saveAll(any());
    }

    @Test
    void lecturasPorInspeccion_AgrupaPorInspeccion() {
        when(lecturaRepository.findByIdInspeccionVehiculoInOrderByOrdenAsc(anyList())).thenReturn(List.of(
                InspPresionLlantaEntity.builder().idInspeccionVehiculo(1L).codigoPosicion("DEL").etiquetaPosicion("Delantera")
                        .psiMedido(new BigDecimal("30")).resultado("OK").build(),
                InspPresionLlantaEntity.builder().idInspeccionVehiculo(2L).codigoPosicion("DEL").etiquetaPosicion("Delantera")
                        .psiMedido(new BigDecimal("20")).resultado("BAJA").build()));

        Map<Long, List<LecturaPresionLlantaResponse>> m = service.lecturasPorInspeccionVehiculo(List.of(1L, 2L, 3L));

        assertEquals("OK", service.resumen(m.get(1L)));
        assertEquals("BAJA", service.resumen(m.get(2L)));
        assertNull(service.resumen(m.get(3L)));
        assertTrue(service.lecturasPorInspeccionVehiculo(List.of()).isEmpty());
    }
}
