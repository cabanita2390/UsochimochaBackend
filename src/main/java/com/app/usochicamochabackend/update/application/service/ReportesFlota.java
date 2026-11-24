package com.app.usochicamochabackend.update.application.service;

import com.app.usochicamochabackend.common.excel.reporte.Reporte;
import com.app.usochicamochabackend.common.excel.reporte.ReporteExcel;
import com.app.usochicamochabackend.context.application.port.GetMachineCurriculumUseCase;
import com.app.usochicamochabackend.context.application.port.GetVehicleCurriculumUseCase;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.machine.application.port.FindAllMachinesUseCase;
import com.app.usochicamochabackend.moto.application.port.MotoCRUDUseCase;
import com.app.usochicamochabackend.moto.application.port.MotoMonitoringUseCase;
import com.app.usochicamochabackend.order.application.port.GetAllOrdersUseCase;
import com.app.usochicamochabackend.order.application.port.GetAllVehicleOrdersUseCase;
import com.app.usochicamochabackend.review.application.port.GetAllInspectionsForExportUseCase;
import com.app.usochicamochabackend.update.application.port.GetConsolidateHydraulicAndMotorOilAllMachinesUseCase;
import com.app.usochicamochabackend.vehicle.application.dto.VehicleResponse;
import com.app.usochicamochabackend.vehicle.application.port.VehicleMonitoringUseCase;
import com.app.usochicamochabackend.vehicle.application.port.VehicleUseCase;
import com.app.usochicamochabackend.vehicleinspection.application.port.GetVehicleInspectionsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * Reportes de flota y maquinaria registrados en el motor único ({@code MotorReportes}). Aquí solo
 * se dice qué trae cada uno y de dónde salen sus datos; {@link ExcelGenerationService} dice cómo
 * se ven sus hojas.
 *
 * <ul>
 *   <li>Consolidados: foto de hoy, sin periodo.</li>
 *   <li>Hojas de vida: todo el historial de cada activo (duran años), sin periodo.</li>
 *   <li>Inspecciones y órdenes: registros del día a día, con periodo.</li>
 * </ul>
 */
@Configuration
public class ReportesFlota {

    @Bean
    ReporteExcel reporteConsolidadoAceites(ExcelGenerationService excel,
            GetConsolidateHydraulicAndMotorOilAllMachinesUseCase consolidado) {
        return Reporte.builder("consolidado-aceites-maquinaria", "Consolidado de aceites · Maquinaria", ReporteExcel.FLOTA)
                .roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarConsolidadoAceites(libro,
                        consolidado.getConsolidateHydraulicAndMotorOilAllMachines()))
                .build();
    }

    @Bean
    ReporteExcel reporteConsolidadoVehiculos(ExcelGenerationService excel, VehicleMonitoringUseCase monitoreo) {
        return Reporte.builder("consolidado-vehiculos", "Consolidado de vehículos", ReporteExcel.FLOTA)
                .roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarConsolidadoVehiculos(libro, monitoreo.getConsolidatedMonitoring()))
                .build();
    }

    @Bean
    ReporteExcel reporteConsolidadoMotos(ExcelGenerationService excel, MotoMonitoringUseCase monitoreo) {
        return Reporte.builder("consolidado-motos", "Consolidado de motos", ReporteExcel.FLOTA)
                .roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarConsolidadoMotos(libro, monitoreo.getConsolidatedMonitoring()))
                .build();
    }

    @Bean
    ReporteExcel reporteHojasDeVidaMaquinaria(ExcelGenerationService excel, FindAllMachinesUseCase maquinas,
            GetMachineCurriculumUseCase hojaDeVida) {
        return Reporte.builder("hojas-de-vida-maquinaria", "Hojas de vida · Maquinaria", ReporteExcel.FLOTA)
                .roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarHojasDeVidaMaquinaria(libro,
                        conHoja(maquinas.findAllMachines(), m -> hojaDeVida.getMachineCurriculum(m.id())), p.periodo()))
                .build();
    }

    @Bean
    ReporteExcel reporteHojasDeVidaVehiculos(ExcelGenerationService excel, VehicleUseCase vehiculos,
            GetVehicleCurriculumUseCase hojaDeVida) {
        return Reporte.builder("hojas-de-vida-vehiculos", "Hojas de vida · Vehículos", ReporteExcel.FLOTA)
                .roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarHojasDeVidaVehiculos(libro,
                        conHoja(vehiculos.findAllVehicles().stream().filter(v -> !esMoto(v)).toList(),
                                v -> hojaDeVida.getVehicleCurriculum(v.id())), p.periodo(), "Vehículos"))
                .build();
    }

    @Bean
    ReporteExcel reporteHojasDeVidaMotos(ExcelGenerationService excel, MotoCRUDUseCase motos,
            GetVehicleCurriculumUseCase hojaDeVida) {
        return Reporte.builder("hojas-de-vida-motos", "Hojas de vida · Motos", ReporteExcel.FLOTA)
                .roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarHojasDeVidaVehiculos(libro,
                        conHoja(motos.findAllMotos(), v -> hojaDeVida.getVehicleCurriculum(v.id())), p.periodo(), "Motos"))
                .build();
    }

    @Bean
    ReporteExcel reporteInspeccionesMaquinaria(ExcelGenerationService excel, GetAllInspectionsForExportUseCase inspecciones) {
        return Reporte.builder("inspecciones-maquinaria", "Inspecciones pre-operativas · Maquinaria", ReporteExcel.FLOTA)
                .conPeriodo().roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarInspeccionesMaquinaria(libro,
                        inspecciones.getAllInspectionsForExport(), p.periodo()))
                .build();
    }

    @Bean
    ReporteExcel reporteInspeccionesVehiculos(ExcelGenerationService excel, GetVehicleInspectionsUseCase inspecciones) {
        return Reporte.builder("inspecciones-vehiculos", "Inspecciones pre-operativas · Vehículos", ReporteExcel.FLOTA)
                .conPeriodo().roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarInspeccionesVehiculos(libro,
                        inspecciones.getAllVehicleInspections(), p.periodo(), "Vehículos"))
                .build();
    }

    @Bean
    ReporteExcel reporteInspeccionesMotos(ExcelGenerationService excel, GetVehicleInspectionsUseCase inspecciones) {
        return Reporte.builder("inspecciones-motos", "Inspecciones pre-operativas · Motos", ReporteExcel.FLOTA)
                .conPeriodo().roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarInspeccionesVehiculos(libro,
                        inspecciones.getMotoInspectionsHistory(), p.periodo(), "Motos"))
                .build();
    }

    @Bean
    ReporteExcel reporteOrdenesMaquinaria(ExcelGenerationService excel, GetAllOrdersUseCase ordenes) {
        return Reporte.builder("ordenes-maquinaria", "Órdenes de trabajo · Maquinaria", ReporteExcel.FLOTA)
                .conPeriodo().roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarOrdenesMaquinaria(libro,
                        ordenes.getAllOrders(Pageable.unpaged()).getContent(), p.periodo()))
                .build();
    }

    @Bean
    ReporteExcel reporteOrdenesVehiculos(ExcelGenerationService excel, GetAllVehicleOrdersUseCase ordenes) {
        // Lo mismo que la pestaña Vehículos de Órdenes: sin las motos (tienen su propia pestaña y reporte).
        return Reporte.builder("ordenes-vehiculos", "Órdenes de trabajo · Vehículos", ReporteExcel.FLOTA)
                .conPeriodo().roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarOrdenesVehiculos(libro,
                        ordenes.getAllVehicleOrders(Pageable.unpaged(), false).getContent(), p.periodo(), "Vehículos"))
                .build();
    }

    @Bean
    ReporteExcel reporteOrdenesMotos(ExcelGenerationService excel, GetAllVehicleOrdersUseCase ordenes) {
        return Reporte.builder("ordenes-motos", "Órdenes de trabajo · Motos", ReporteExcel.FLOTA)
                .conPeriodo().roles(ReporteExcel.ROLES_TODOS)
                .llenando((libro, p) -> excel.llenarOrdenesVehiculos(libro,
                        ordenes.getAllVehicleOrders(Pageable.unpaged(), true).getContent(), p.periodo(), "Motos"))
                .build();
    }

    /** Las hojas de vida de los activos que tienen una (los que no, se saltan). */
    private static <A, H> List<H> conHoja(List<A> activos, Function<A, H> hoja) {
        return activos.stream().map(a -> {
            try {
                return hoja.apply(a);
            } catch (ResourceNotFoundException e) {
                return null;
            }
        }).filter(Objects::nonNull).toList();
    }

    private static boolean esMoto(VehicleResponse v) {
        return String.valueOf(v.tipoVehiculo()).toLowerCase(Locale.ROOT).contains("moto");
    }
}
