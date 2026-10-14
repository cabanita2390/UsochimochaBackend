package com.app.usochicamochabackend.update.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.exception.BadRequestException;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.notifications.application.PreventiveAlertCalculationService;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.update.application.exception.InvalidOilChangeDataException;
import com.app.usochicamochabackend.update.infrastructure.entity.OilChangeEntity;
import com.app.usochicamochabackend.update.infrastructure.entity.OilType;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import com.app.usochicamochabackend.update.application.dto.PerformChangeHydraulicOilRequest;
import com.app.usochicamochabackend.update.application.dto.PerformChangeMotorOilRequest;
import com.app.usochicamochabackend.update.infrastructure.repository.BrandRepository;
import com.app.usochicamochabackend.update.infrastructure.repository.OilChangeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OilChangeServiceTest {

    @Mock private MachineRepository machineRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private InspectionRepository inspectionRepository;
    @Mock private OilChangeRepository oilChangeRepository;
    @Mock private SaveActionUseCase saveActionUseCase;
    @Mock private NotificationService notificationService;
    @Mock private PreventiveAlertCalculationService preventiveAlertCalculationService;

    private OilChangeService oilChangeService;

    @BeforeEach
    void setUp() {
        oilChangeService = new OilChangeService(
                machineRepository, brandRepository,
                new LecturaHorometroMaquina(inspectionRepository, oilChangeRepository),
                oilChangeRepository, saveActionUseCase, notificationService,
                preventiveAlertCalculationService, new OilChangeValidationService());

        UserPrincipal principal = new UserPrincipal(1L, "tecnico");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private MachineEntity maquina(Integer horometroActual) {
        return MachineEntity.builder().id(1L).name("Excavadora Test").status(true).horometroActual(horometroActual).build();
    }

    private PerformChangeMotorOilRequest requestMotor(Double currentHourMeter) {
        return new PerformChangeMotorOilRequest(1L, LocalDateTime.now(), null, 4.0, currentHourMeter, 250);
    }

    private PerformChangeHydraulicOilRequest requestHidraulico(Double currentHourMeter) {
        return new PerformChangeHydraulicOilRequest(1L, LocalDateTime.now(), null, 4.0, currentHourMeter, 250);
    }

    // ---- performMotorOilChange ----

    @Test
    void motor_ConHorometroMenorAlActual_NoDecrementaElHorometroDeLaMaquina() {
        MachineEntity machine = maquina(500);
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine));

        oilChangeService.performMotorOilChange(requestMotor(300.0));

        assertEquals(500, machine.getHorometroActual());
        verify(machineRepository, never()).save(any());
    }

    @Test
    void motor_ConHorometroMayorAlActual_ActualizaElHorometroDeLaMaquina() {
        MachineEntity machine = maquina(500);
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine));

        oilChangeService.performMotorOilChange(requestMotor(650.0));

        assertEquals(650, machine.getHorometroActual());
        verify(machineRepository).save(machine);
    }

    @Test
    void motor_ConHorometroMenorAUnaInspeccionPosterior_YaNoSeBloquea() {
        // Antes del fix: performMotorOilChange consultaba inspectionRepository.getLastInspection(...)
        // y lanzaba BadRequestException si currentHourMeter era menor a esa inspección — el
        // bloqueo real que impedía registrar tarde. Ahora ni siquiera debería consultarla.
        MachineEntity machine = maquina(1000);
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine));

        assertDoesNotThrow(() -> oilChangeService.performMotorOilChange(requestMotor(700.0)));

        verify(inspectionRepository, never()).getLastInspection(any());
    }

    @Test
    void motor_ConHorometroCero_LanzaBadRequestExceptionYNoGuardaNada() {
        assertThrows(BadRequestException.class, () -> oilChangeService.performMotorOilChange(requestMotor(0.0)));

        verify(oilChangeRepository, never()).save(any());
        verify(machineRepository, never()).findById(any());
    }

    @Test
    void motor_ConHorometroNulo_LanzaBadRequestException() {
        assertThrows(BadRequestException.class, () -> oilChangeService.performMotorOilChange(requestMotor(null)));
    }

    // ---- performChangeHydraulicOil ----

    @Test
    void hidraulico_ConHorometroMenorAlActual_NoDecrementaElHorometroDeLaMaquina() {
        MachineEntity machine = maquina(500);
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine));

        oilChangeService.performChangeHydraulicOil(requestHidraulico(300.0));

        assertEquals(500, machine.getHorometroActual());
        verify(machineRepository, never()).save(any());
    }

    @Test
    void hidraulico_ConHorometroMayorAlActual_ActualizaElHorometroDeLaMaquina() {
        MachineEntity machine = maquina(500);
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine));

        oilChangeService.performChangeHydraulicOil(requestHidraulico(650.0));

        assertEquals(650, machine.getHorometroActual());
        verify(machineRepository).save(machine);
    }

    @Test
    void hidraulico_ConHorometroMenorAUnaInspeccionPosterior_YaNoSeBloquea() {
        MachineEntity machine = maquina(1000);
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine));

        assertDoesNotThrow(() -> oilChangeService.performChangeHydraulicOil(requestHidraulico(700.0)));

        verify(inspectionRepository, never()).getLastInspection(any());
    }

    @Test
    void hidraulico_ConHorometroNegativo_LanzaBadRequestException() {
        assertThrows(BadRequestException.class, () -> oilChangeService.performChangeHydraulicOil(requestHidraulico(-5.0)));
    }

    // ---- obtenerHistorial ----

    @Test
    void obtenerHistorial_MapeaOilTypeYBrandIdParaPrecargarLaEdicion() {
        var brand = com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity.builder().id(2L).name("Mobil").build();
        var entity = com.app.usochicamochabackend.update.infrastructure.entity.OilChangeEntity.builder()
                .id(10L).oilType(com.app.usochicamochabackend.update.infrastructure.entity.OilType.MOTOR)
                .brand(brand).quantity(4.0).hourMeter(500.0).averageHoursChange(250).status(true)
                .build();
        when(oilChangeRepository.findByMachineIdAndOilTypeAndStatusOrderByDateStampDesc(
                1L, com.app.usochicamochabackend.update.infrastructure.entity.OilType.MOTOR, true))
                .thenReturn(List.of(entity));

        var historial = oilChangeService.obtenerHistorial(1L, "MOTOR");

        assertEquals(1, historial.size());
        assertEquals("MOTOR", historial.get(0).oilType());
        assertEquals(2L, historial.get(0).brandId());
        assertEquals("Mobil", historial.get(0).brandName());
    }

    // ---- actualizarCambioAceite ----

    @Test
    void actualizarCambioAceite_ConIdInexistente_LanzaResourceNotFound() {
        when(oilChangeRepository.findByIdAndStatus(99L, true)).thenReturn(Optional.empty());

        assertThrows(com.app.usochicamochabackend.exception.ResourceNotFoundException.class,
                () -> oilChangeService.actualizarCambioAceite(99L, requestMotor(650.0)));

        verify(oilChangeRepository, never()).save(any());
    }

    @Test
    void actualizarCambioAceite_ConHorometroMayorAlActual_ActualizaElHorometroDeLaMaquina() {
        MachineEntity machine = maquina(500);
        var entity = com.app.usochicamochabackend.update.infrastructure.entity.OilChangeEntity.builder()
                .id(10L).machine(machine).quantity(3.0).hourMeter(400.0).averageHoursChange(250).status(true)
                .build();
        when(oilChangeRepository.findByIdAndStatus(10L, true)).thenReturn(Optional.of(entity));

        oilChangeService.actualizarCambioAceite(10L, requestMotor(700.0));

        assertEquals(700, machine.getHorometroActual());
        assertEquals(700.0, entity.getHourMeter());
        verify(oilChangeRepository).save(entity);
        verify(preventiveAlertCalculationService).calculateAndEmitAlerts();
    }

    @Test
    void actualizarCambioAceite_ConHorometroInvalido_LanzaBadRequestException() {
        assertThrows(BadRequestException.class,
                () -> oilChangeService.actualizarCambioAceite(10L, requestMotor(0.0)));

        verify(oilChangeRepository, never()).findByIdAndStatus(any(), any());
    }

    // ---- eliminarCambioAceite ----

    @Test
    void eliminarCambioAceite_ConIdInexistente_LanzaResourceNotFound() {
        when(oilChangeRepository.findByIdAndStatus(99L, true)).thenReturn(Optional.empty());

        assertThrows(com.app.usochicamochabackend.exception.ResourceNotFoundException.class,
                () -> oilChangeService.eliminarCambioAceite(99L));
    }

    @Test
    void eliminarCambioAceite_MarcaStatusFalseYNoTocaElHorometroDeLaMaquina() {
        MachineEntity machine = maquina(500);
        var entity = com.app.usochicamochabackend.update.infrastructure.entity.OilChangeEntity.builder()
                .id(10L).machine(machine).status(true).build();
        when(oilChangeRepository.findByIdAndStatus(10L, true)).thenReturn(Optional.of(entity));

        oilChangeService.eliminarCambioAceite(10L);

        assertEquals(false, entity.getStatus());
        assertEquals(500, machine.getHorometroActual());
        verify(oilChangeRepository).save(entity);
        verify(machineRepository, never()).save(any());
        verify(preventiveAlertCalculationService).calculateAndEmitAlerts();
    }

    // ---- fecha del servicio ----

    @Test
    void motor_ConFechaAtrasada_SeGuardaConEsaFecha() {
        MachineEntity machine = maquina(500);
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine));
        LocalDateTime atrasada = LocalDateTime.now().minusDays(8).withHour(10).withMinute(30).withSecond(0).withNano(0);

        oilChangeService.performMotorOilChange(new PerformChangeMotorOilRequest(1L, atrasada, null, 4.0, 300.0, 250));

        verify(oilChangeRepository).save(argThat(e -> atrasada.equals(e.getDateStamp())));
    }

    @Test
    void motor_ConFechaFutura_LanzaInvalidOilChangeDataYNoGuarda() {
        var request = new PerformChangeMotorOilRequest(1L, LocalDateTime.now().plusDays(1), null, 4.0, 300.0, 250);

        assertThrows(InvalidOilChangeDataException.class, () -> oilChangeService.performMotorOilChange(request));

        verify(oilChangeRepository, never()).save(any());
    }

    @Test
    void hidraulico_SinFecha_UsaLaFechaActual() {
        when(machineRepository.findById(1L)).thenReturn(Optional.of(maquina(500)));

        oilChangeService.performChangeHydraulicOil(new PerformChangeHydraulicOilRequest(1L, null, null, 4.0, 600.0, 250));

        verify(oilChangeRepository).save(argThat(e -> e.getDateStamp() != null));
    }

    @Test
    void actualizarCambioAceite_ConFechaFutura_LanzaInvalidOilChangeData() {
        var request = new PerformChangeMotorOilRequest(1L, LocalDateTime.now().plusDays(3), null, 4.0, 300.0, 250);

        assertThrows(InvalidOilChangeDataException.class, () -> oilChangeService.actualizarCambioAceite(10L, request));

        verify(oilChangeRepository, never()).findByIdAndStatus(any(), any());
    }

    // ---- consolidado: horómetro actual ----

    private OilChangeEntity cambio(OilType tipo, double horometro, LocalDateTime fecha) {
        return OilChangeEntity.builder().id(tipo == OilType.MOTOR ? 1L : 2L).oilType(tipo)
                .hourMeter(horometro).averageHoursChange(250).dateStamp(fecha).quantity(4.0).status(true).build();
    }

    @Test
    void consolidado_CambioPosteriorConHorometroMayor_EsElHorometroActual() {
        MachineEntity machine = maquina(1600);
        LocalDateTime fechaInspeccion = LocalDateTime.now().minusDays(10);
        LocalDateTime fechaCambio = LocalDateTime.now().minusDays(2);
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(inspectionRepository.getLastInspection(1L)).thenReturn(
                InspectionEntity.builder().hourMeter(1500.0).dateStamp(fechaInspeccion).build());
        when(oilChangeRepository.getLastMotorOilChangeByMachineId(1L)).thenReturn(cambio(OilType.MOTOR, 1600.0, fechaCambio));

        var dto = oilChangeService.getConsolidateHydraulicAndMotorOilById(1L);

        assertEquals(1600.0, dto.currentData().currentHourMeter());
        assertEquals(fechaCambio, dto.currentData().lastUpdate());
        // Horas restantes nunca mayores al intervalo: 1600 + 250 - 1600
        assertEquals(250.0, dto.consolidateMotorOil().remainingHoursNextUpdateMouths());
    }

    @Test
    void consolidado_CambioAtrasadoConHorometroMenor_NoBajaElHorometroActual() {
        MachineEntity machine = maquina(1500);
        LocalDateTime fechaInspeccion = LocalDateTime.now().minusDays(1);
        when(machineRepository.findById(1L)).thenReturn(Optional.of(machine));
        when(inspectionRepository.getLastInspection(1L)).thenReturn(
                InspectionEntity.builder().hourMeter(1500.0).dateStamp(fechaInspeccion).build());
        when(oilChangeRepository.getLastMotorOilChangeByMachineId(1L))
                .thenReturn(cambio(OilType.MOTOR, 1400.0, LocalDateTime.now().minusDays(8)));

        var dto = oilChangeService.getConsolidateHydraulicAndMotorOilById(1L);

        assertEquals(1500.0, dto.currentData().currentHourMeter());
        assertEquals(fechaInspeccion, dto.currentData().lastUpdate());
        assertEquals(150.0, dto.consolidateMotorOil().remainingHoursNextUpdateMouths());
    }

    @Test
    void consolidado_MaquinaConCambioPeroSinInspeccion_SiAparece() {
        when(machineRepository.findById(1L)).thenReturn(Optional.of(maquina(800)));
        when(oilChangeRepository.getLastHydraulicOilChangeByMachineId(1L))
                .thenReturn(cambio(OilType.HYDRAULIC, 800.0, LocalDateTime.now().minusDays(3)));

        var dto = oilChangeService.getConsolidateHydraulicAndMotorOilById(1L);

        assertNotNull(dto);
        assertEquals(800.0, dto.currentData().currentHourMeter());
        assertNull(dto.consolidateMotorOil());
        assertNotNull(dto.consolidateHydraulicOil());
    }

    @Test
    void consolidado_MaquinaSinInspeccionNiCambios_NoAparece() {
        when(machineRepository.findById(1L)).thenReturn(Optional.of(maquina(0)));

        assertNull(oilChangeService.getConsolidateHydraulicAndMotorOilById(1L));
    }
}
