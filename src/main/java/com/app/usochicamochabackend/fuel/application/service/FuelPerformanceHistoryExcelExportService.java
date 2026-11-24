package com.app.usochicamochabackend.fuel.application.service;

import com.app.usochicamochabackend.common.excel.ColumnaExcel;
import com.app.usochicamochabackend.common.excel.ColumnaExcel.Estado;
import com.app.usochicamochabackend.common.excel.LibroExcel;
import com.app.usochicamochabackend.common.excel.Periodo;
import com.app.usochicamochabackend.common.excel.RespuestaExcel;
import com.app.usochicamochabackend.common.excel.reporte.FiltroReporte;
import com.app.usochicamochabackend.common.excel.reporte.MotorReportes;
import com.app.usochicamochabackend.common.excel.reporte.PeticionReporte;
import com.app.usochicamochabackend.common.excel.reporte.ReporteExcel;
import com.app.usochicamochabackend.fuel.application.dto.FuelPerformanceResponse;
import com.app.usochicamochabackend.fuel.application.port.ExportFuelPerformanceHistoryUseCase;
import com.app.usochicamochabackend.fuel.application.port.GetFuelPerformanceUseCase;
import com.app.usochicamochabackend.fuel.infrastructure.entity.AssetFuelConfigEntity;
import com.app.usochicamochabackend.fuel.infrastructure.entity.FuelTypesEntity;
import com.app.usochicamochabackend.fuel.infrastructure.repository.AssetFuelConfigRepository;
import com.app.usochicamochabackend.fuel.infrastructure.repository.FuelTypesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Exportación a Excel del historial de rendimiento de UN activo puntual — pantalla
 * "Historial de rendimiento" del front (FuelPerformanceHistory.svelte). Una hoja por
 * mes, con las mismas columnas que la tabla "Historial detallado" en pantalla (ver
 * createFuelPerformanceColumns en el front).
 *
 * Reutiliza {@link GetFuelPerformanceUseCase#obtenerRendimiento} — la misma fuente
 * que ya consume esa pantalla — y filtra al activo pedido. La config del activo
 * (tanque/unidad de consumo) es constante para todas las filas de un mismo activo,
 * así que se resuelve una sola vez en vez de por fila (a diferencia del reporte de
 * tanqueos, donde cada fila puede ser de un activo distinto).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FuelPerformanceHistoryExcelExportService implements ExportFuelPerformanceHistoryUseCase, ReporteExcel {

    private static final String MAQUINARIA = "MAQUINARIA";
    private static final String VEHICULO = "VEHICULO";
    private static final String MOTOCICLETA = "MOTOCICLETA";

    // Mismo criterio de "todo el histórico" que FECHA_INICIO_HISTORICO en
    // FuelPerformanceHistory.svelte: el backend de Rendimiento asume el mes actual
    // si las fechas vienen vacías, así que acá se fuerza un rango amplio explícito.
    private static final LocalDate FECHA_INICIO_HISTORICO = LocalDate.of(2000, 1, 1);

    private static final DateTimeFormatter MES_TITULO = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-CO"));
    // Nombre de hoja: yyyy-MM (sin caracteres inválidos para Excel, ordena
    // cronológicamente solo con orden alfabético de las pestañas).
    private static final DateTimeFormatter MES_SHEET_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private static final Map<String, String> UNIDAD_CONSUMO_LABELS = Map.of(
            "KM_POR_GALON", "Km/Gl",
            "HORA_POR_GALON", "H/Gl",
            "KM_POR_M3", "Km/M3",
            "HORA_POR_M3", "H/M3");

    private final GetFuelPerformanceUseCase getFuelPerformanceUseCase;
    private final AssetFuelConfigRepository assetFuelConfigRepository;
    private final FuelTypesRepository fuelTypesRepository;

    // --- Reporte del motor único (GET /api/v1/reportes/rendimiento-historial) ---

    @Override public String clave() { return "rendimiento-historial"; }
    @Override public String titulo() { return "Historial de rendimiento de un activo"; }
    @Override public String modulo() { return ReporteExcel.COMBUSTIBLE; }
    @Override public boolean usaPeriodo() { return true; }
    @Override public String periodoInicial() { return "todo"; }

    @Override
    public List<FiltroReporte> filtros() {
        return List.of(FiltroReporte.contexto("tipo", "Tipo de activo", true),
                FiltroReporte.contexto("activoId", "Activo", true));
    }

    /** "rendimiento_historial_vehiculo_12" (el motor le agrega el periodo). */
    @Override
    public String nombreArchivo(PeticionReporte p) {
        return "rendimiento_historial_" + p.filtroObligatorio("tipo").toLowerCase(Locale.ROOT) + "_" + p.filtroObligatorio("activoId");
    }

    /** El endpoint anterior: mismo motor. */
    @Override
    public byte[] exportarHistorialActivo(String tipo, Long activoId, LocalDate desde, LocalDate hasta) {
        Map<String, String> filtros = new java.util.HashMap<>();
        filtros.put("tipo", tipo);
        filtros.put("activoId", activoId == null ? null : activoId.toString());
        try {
            return MotorReportes.generar(this, PeticionReporte.de(desde, hasta, filtros, RespuestaExcel.usuarioActual()))
                    .contenido();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel de historial de rendimiento.", e);
        }
    }

    @Override
    public void llenar(LibroExcel libro, PeticionReporte p) {
        String tipo = p.filtroObligatorio("tipo");
        Long activoId = p.filtroLong("activoId");
        if (activoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el activo.");
        }
        Periodo periodo = p.periodo();
        LocalDate desde = periodo.desde();
        LocalDate hasta = periodo.hasta();
        if (!MAQUINARIA.equals(tipo) && !VEHICULO.equals(tipo) && !MOTOCICLETA.equals(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipo debe ser MAQUINARIA, VEHICULO o MOTOCICLETA.");
        }
        boolean esMaquina = MAQUINARIA.equals(tipo);

        List<FuelPerformanceResponse> filas = getFuelPerformanceUseCase
                .obtenerRendimiento(tipo, desde != null ? desde : FECHA_INICIO_HISTORICO, hasta != null ? hasta : LocalDate.now()).stream()
                .filter(r -> esMaquina ? activoId.equals(r.machineId()) : activoId.intValue() == safeInt(r.vehicleId()))
                .filter(r -> periodo.contiene(r.fechaRegistro()))
                .sorted(Comparator.comparing(FuelPerformanceResponse::fechaRegistro))
                .toList();
        if (filas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay historial de rendimiento para este activo.");
        }

        Optional<AssetFuelConfigEntity> config = esMaquina
                ? assetFuelConfigRepository.findByMachineId(activoId)
                : assetFuelConfigRepository.findByVehicleId(activoId.intValue());
        BigDecimal tanqueCapacidadGal = config.map(AssetFuelConfigEntity::getTanqueCapacidadGal).orElse(null);
        String unidadConsumo = config.map(AssetFuelConfigEntity::getUnidadConsumo).orElse(null);
        String unidadFisica = config.map(AssetFuelConfigEntity::getFuelTypeDefaultId)
                .flatMap(fuelTypesRepository::findById)
                .map(FuelTypesEntity::getUnidadMedida)
                .orElse("GALON");

        Set<Long> fuelTypeIds = filas.stream().map(FuelPerformanceResponse::fuelTypeId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> nombreFuelTypeById = fuelTypesRepository.findAllById(fuelTypeIds).stream()
                .collect(Collectors.toMap(FuelTypesEntity::getId, FuelTypesEntity::getNombre));

        Map<YearMonth, List<FuelPerformanceResponse>> porMes = filas.stream()
                .collect(Collectors.groupingBy(f -> YearMonth.from(f.fechaRegistro()), TreeMap::new, Collectors.toList()));

        String activo = filas.get(0).identificacionActivo();
        String medicion = esMaquina ? "Horómetro" : "Kilometraje";
        String ejec = esMaquina ? "Horas" : "Km";
        String unidadFisicaLabel = "M3".equals(unidadFisica) ? "m³" : "gal";
        List<ColumnaExcel<FuelPerformanceResponse>> cols = List.of(
                ColumnaExcel.fechaHora("Fecha", FuelPerformanceResponse::fechaRegistro),
                ColumnaExcel.texto("Producto", f -> nombreFuelTypeById.get(f.fuelTypeId())),
                ColumnaExcel.decimal("Consumo estándar (A) " + unidadConsumoLabel(unidadConsumo), FuelPerformanceResponse::consumoEstandar),
                ColumnaExcel.decimal("Tamaño del tanque (gal)", f -> tanqueCapacidadGal),
                ColumnaExcel.decimal("Total tanqueado (F, " + unidadFisicaLabel + ")", FuelPerformanceResponse::galonesReal).conTotal(),
                ColumnaExcel.decimal(medicion + " anterior (B)", FuelPerformanceResponse::horometroAnterior),
                ColumnaExcel.decimal(medicion + " actual (C)", FuelPerformanceResponse::horometroActual),
                ColumnaExcel.decimal(ejec + " esperadas (H = F×A)", (FuelPerformanceResponse f) -> esperado(f)).conTotal(),
                ColumnaExcel.decimal(ejec + " ejecutadas (D = C−B)", (FuelPerformanceResponse f) -> nullToZero(f.ejecutado())).conTotal(),
                ColumnaExcel.decimal("Diferencia (D − H)", (FuelPerformanceResponse f) -> nullToZero(f.ejecutado()).subtract(esperado(f)))
                        .conEstado(f -> nullToZero(f.ejecutado()).subtract(esperado(f)).signum() < 0 ? Estado.ALERTA : null),
                ColumnaExcel.porcentaje("% eficiencia (D ÷ H)", this::eficiencia),
                ColumnaExcel.texto("¿Tanque lleno?", f -> siNo(f.esFull())),
                ColumnaExcel.texto("Alerta", (FuelPerformanceResponse f) -> siNo(f.alerta())
                        + (Boolean.FALSE.equals(f.usaRangoAprendido()) ? " *" : ""))
                        .conEstado(f -> Boolean.TRUE.equals(f.alerta()) ? Estado.MAL : null));

        {
            libro.titulo("Historial de rendimiento · " + activo).filtro("Activo", activo).filtro("Tipo", tipo)
                    .cifra("Tanqueos", filas.size())
                    .cifra("Meses", porMes.size())
                    .cifra("Combustible (" + unidadFisicaLabel + ")", filas.stream().map(FuelPerformanceResponse::galonesReal)
                            .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue())
                    .cifra("Tanqueos con alerta", filas.stream().filter(f -> Boolean.TRUE.equals(f.alerta())).count())
                    .nota("* La alerta usa el rango por defecto: el activo todavía no tiene un rango aprendido.");
            for (Map.Entry<YearMonth, List<FuelPerformanceResponse>> entry : porMes.entrySet()) {
                String mes = entry.getKey().format(MES_SHEET_FORMATTER);
                libro.tabla(mes, activo + " · " + capitalizar(entry.getKey().format(MES_TITULO)), cols, entry.getValue());
            }
            log.info("Excel de historial de rendimiento generado: activo {} ({}), {} meses, {} filas",
                    activoId, tipo, porMes.size(), filas.size());
        }
    }

    private BigDecimal esperado(FuelPerformanceResponse f) {
        return nullToZero(f.galonesReal()).multiply(nullToZero(f.consumoEstandar()));
    }

    private BigDecimal eficiencia(FuelPerformanceResponse f) {
        BigDecimal esp = esperado(f);
        return esp.signum() == 0 ? null
                : nullToZero(f.ejecutado()).divide(esp, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private static String capitalizar(String s) {
        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(Locale.forLanguageTag("es-CO")) + s.substring(1);
    }

    private int safeInt(Integer value) {
        return value != null ? value : -1;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String unidadConsumoLabel(String codigo) {
        if (codigo == null) return "—";
        return UNIDAD_CONSUMO_LABELS.getOrDefault(codigo, codigo);
    }

    // Sí/No/N/A — mismo criterio de 3 estados que yn() en el front (a diferencia de
    // siONo en FuelExcelExportService, que solo distingue Sí/No para un DTO donde el
    // campo nunca es null).
    private String siNo(Boolean value) {
        if (Boolean.TRUE.equals(value)) return "SÍ";
        if (Boolean.FALSE.equals(value)) return "NO";
        return "N/A";
    }
}
