package com.app.usochicamochabackend.fuel.application.service;

import com.app.usochicamochabackend.common.excel.ColumnaExcel;
import com.app.usochicamochabackend.common.excel.ColumnaExcel.Estado;
import com.app.usochicamochabackend.common.excel.LibroExcel;
import com.app.usochicamochabackend.common.excel.reporte.FiltroReporte;
import com.app.usochicamochabackend.common.excel.reporte.PeticionReporte;
import com.app.usochicamochabackend.common.excel.reporte.ReporteExcel;
import com.app.usochicamochabackend.fuel.application.dto.FuelBudgetProjectionRow;
import com.app.usochicamochabackend.fuel.application.dto.RefuelingRecordResponse;
import com.app.usochicamochabackend.fuel.application.port.GetFuelDashboardUseCase;
import com.app.usochicamochabackend.fuel.application.port.GetRefuelingReportUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * Exportación a Excel del reporte de "Tanqueo y Distribución" (detalle completo, no
 * la vista colapsada de 1 fila por activo), más la hoja de proyección presupuestal
 * calculada por {@link GetFuelDashboardUseCase#obtenerProyeccionPresupuestal}
 * (compartida con el Dashboard Financiero). Sustituto acordado con el usuario de la
 * "auditoría física y monetaria" del cronograma original: el sistema no audita por
 * sí mismo, pero entrega los datos reales para revisión manual por parte de
 * Asociación/Distrito. Con el estilo común de {@link LibroExcel}.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FuelExcelExportService implements ReporteExcel {

    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-CO"));

    private final GetRefuelingReportUseCase getRefuelingReportUseCase;
    private final GetFuelDashboardUseCase getFuelDashboardUseCase;

    // --- Reporte del motor único (GET /api/v1/reportes/tanqueos) ---

    @Override public String clave() { return "tanqueos"; }
    @Override public String titulo() { return "Tanqueo y distribución de combustible"; }
    @Override public String modulo() { return ReporteExcel.COMBUSTIBLE; }
    @Override public boolean usaPeriodo() { return true; }
    @Override public String periodoInicial() { return "mes"; }

    @Override
    public List<FiltroReporte> filtros() {
        return List.of(FiltroReporte.contexto("tipo", "Tipo de activo", true),
                FiltroReporte.contexto("area", "Área de costo", false));
    }

    @Override
    public void llenar(LibroExcel libro, PeticionReporte p) {
        String tipo = p.filtroObligatorio("tipo");
        String area = p.filtro("area");
        LocalDate fechaInicio = p.periodo().desde();
        LocalDate fechaFin = p.periodo().hasta();
        List<RefuelingRecordResponse> tanqueos = getRefuelingReportUseCase.obtenerReporte(tipo, area, fechaInicio, fechaFin);
        List<FuelBudgetProjectionRow> proyeccion = getFuelDashboardUseCase.obtenerProyeccionPresupuestal(fechaFin);

        List<ColumnaExcel<RefuelingRecordResponse>> cols = List.of(
                ColumnaExcel.fechaHora("Fecha", RefuelingRecordResponse::fechaRegistro),
                ColumnaExcel.texto("Tipo de activo", RefuelingRecordResponse::assetType),
                ColumnaExcel.texto("Activo", RefuelingRecordResponse::assetName),
                ColumnaExcel.texto("Lugar", RefuelingRecordResponse::lugar),
                ColumnaExcel.texto("Área de costo", RefuelingRecordResponse::areaCosto),
                ColumnaExcel.decimal("Cantidad (gal)", RefuelingRecordResponse::cantidadGalones).conTotal(),
                ColumnaExcel.moneda("Precio unitario", RefuelingRecordResponse::precioUnitario),
                ColumnaExcel.moneda("Total calculado", RefuelingRecordResponse::totalCalculado).conTotal(),
                ColumnaExcel.moneda("Total ingresado", RefuelingRecordResponse::totalIngresado).conTotal(),
                alerta("Discrepancia de valor", RefuelingRecordResponse::discrepanciaValor),
                alerta("Capacidad excedida", RefuelingRecordResponse::capacidadExcedida),
                alerta("Cantidad fuera de rango", RefuelingRecordResponse::cantidadFueraDeRango),
                alerta("Precio fuera de rango", RefuelingRecordResponse::precioFueraDeRango),
                alerta("Full inconsistente", RefuelingRecordResponse::fullInconsistente),
                ColumnaExcel.decimal("Cantidad reintegrada (gal)", RefuelingRecordResponse::cantidadReintegrada).conTotal());

        List<ColumnaExcel<FuelBudgetProjectionRow>> colsProyeccion = List.of(
                ColumnaExcel.texto("Mes", f -> f.mes() == null ? null : capitalizar(f.mes().format(MES))),
                ColumnaExcel.moneda("Gasto neto", FuelBudgetProjectionRow::gastoNeto),
                ColumnaExcel.texto("Tipo", (FuelBudgetProjectionRow f) -> f.proyectado() ? "Proyectado" : "Histórico")
                        .conEstado(f -> f.proyectado() ? Estado.ALERTA : null));

        {
            libro.filtro("Tipo de activo", tipo).filtro("Área de costo", area)
                    .cifra("Tanqueos", tanqueos.size())
                    .cifra("Galones", suma(tanqueos, RefuelingRecordResponse::cantidadGalones).doubleValue())
                    .cifra("Valor total", "$ " + String.format(Locale.forLanguageTag("es-CO"), "%,.0f",
                            suma(tanqueos, RefuelingRecordResponse::totalCalculado)))
                    .cifra("Tanqueos con alguna alerta", tanqueos.stream().filter(FuelExcelExportService::conAlerta).count())
                    .nota("Las alertas (discrepancia, capacidad, rangos, full) salen en rojo para revisión manual.");
            libro.tabla("Tanqueos", "Detalle de tanqueos", cols, tanqueos);
            libro.tabla("Proyección presupuestal", "Gasto neto mensual: histórico y proyectado", colsProyeccion, proyeccion);
            log.info("Excel de tanqueos generado: {} tanqueos, {} filas de proyección", tanqueos.size(), proyeccion.size());
        }
    }

    private static ColumnaExcel<RefuelingRecordResponse> alerta(String titulo, Function<RefuelingRecordResponse, Boolean> v) {
        return ColumnaExcel.texto(titulo, (RefuelingRecordResponse t) -> Boolean.TRUE.equals(v.apply(t)) ? "Sí" : "No")
                .conEstado(t -> Boolean.TRUE.equals(v.apply(t)) ? Estado.MAL : null);
    }

    private static boolean conAlerta(RefuelingRecordResponse t) {
        return Boolean.TRUE.equals(t.discrepanciaValor()) || Boolean.TRUE.equals(t.capacidadExcedida())
                || Boolean.TRUE.equals(t.cantidadFueraDeRango()) || Boolean.TRUE.equals(t.precioFueraDeRango())
                || Boolean.TRUE.equals(t.fullInconsistente());
    }

    private static BigDecimal suma(List<RefuelingRecordResponse> filas, Function<RefuelingRecordResponse, BigDecimal> v) {
        return filas.stream().map(v).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String capitalizar(String s) {
        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(Locale.forLanguageTag("es-CO")) + s.substring(1);
    }
}
