package com.app.usochicamochabackend.update.application.service;

import com.app.usochicamochabackend.common.excel.Periodo;
import com.app.usochicamochabackend.common.excel.reporte.MotorReportes;
import com.app.usochicamochabackend.common.excel.reporte.PeticionReporte;
import com.app.usochicamochabackend.common.excel.reporte.ReporteExcel;
import com.app.usochicamochabackend.order.application.port.GetAllVehicleOrdersUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Cada reporte de órdenes trae lo mismo que su pestaña en la web. */
class ReportesFlotaTest {

    private final ReportesFlota reportes = new ReportesFlota();
    private final ExcelGenerationService excel = new ExcelGenerationService();

    private static PeticionReporte peticion() {
        return new PeticionReporte(new Periodo(null, null), Map.of(), null, "admin");
    }

    @Test
    void ordenesDeVehiculosSinLasMotos() throws Exception {
        GetAllVehicleOrdersUseCase ordenes = mock(GetAllVehicleOrdersUseCase.class);
        when(ordenes.getAllVehicleOrders(any(Pageable.class), eq(false))).thenReturn(Page.empty());
        ReporteExcel r = reportes.reporteOrdenesVehiculos(excel, ordenes);

        MotorReportes.Archivo a = MotorReportes.generar(r, peticion());

        verify(ordenes).getAllVehicleOrders(any(Pageable.class), eq(false));
        verify(ordenes, never()).getAllVehicleOrders(any(Pageable.class));
        assertEquals("Órdenes de trabajo · Vehículos", r.titulo());
        assertEquals("ordenes_vehiculos.xlsx", a.nombre());
        assertTrue(r.usaPeriodo());
    }

    @Test
    void ordenesDeMotosSoloLasMotos() throws Exception {
        GetAllVehicleOrdersUseCase ordenes = mock(GetAllVehicleOrdersUseCase.class);
        when(ordenes.getAllVehicleOrders(any(Pageable.class), eq(true))).thenReturn(Page.empty());
        ReporteExcel r = reportes.reporteOrdenesMotos(excel, ordenes);

        MotorReportes.Archivo a = MotorReportes.generar(r, peticion());

        verify(ordenes).getAllVehicleOrders(any(Pageable.class), eq(true));
        assertEquals("ordenes_motos.xlsx", a.nombre());
    }

    @Test
    void lasFotosDeHoySinPeriodoLlevanLaFechaDeCorte() throws Exception {
        var monitoreo = mock(com.app.usochicamochabackend.moto.application.port.MotoMonitoringUseCase.class);
        when(monitoreo.getConsolidatedMonitoring()).thenReturn(java.util.List.of());
        ReporteExcel r = reportes.reporteConsolidadoMotos(excel, monitoreo);

        // Aunque llegue un periodo, un consolidado no lo usa.
        MotorReportes.Archivo a = MotorReportes.generar(r, new PeticionReporte(
                new Periodo(java.time.LocalDate.of(2026, 1, 1), java.time.LocalDate.of(2026, 1, 31)), Map.of(), null, "admin"));

        assertFalse(r.usaPeriodo());
        assertEquals("consolidado_motos_" + java.time.LocalDate.now() + ".xlsx", a.nombre());
    }
}
