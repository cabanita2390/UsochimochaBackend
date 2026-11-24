package com.app.usochicamochabackend.fuel.application.service;

import com.app.usochicamochabackend.common.excel.ColumnaExcel;
import com.app.usochicamochabackend.common.excel.LibroExcel;
import com.app.usochicamochabackend.common.excel.Periodo;
import com.app.usochicamochabackend.common.excel.reporte.PeticionReporte;
import com.app.usochicamochabackend.common.excel.reporte.ReporteExcel;
import com.app.usochicamochabackend.fuel.infrastructure.entity.AssetFuelConfigEntity;
import com.app.usochicamochabackend.fuel.infrastructure.entity.RefuelingRecordsEntity;
import com.app.usochicamochabackend.fuel.infrastructure.repository.AssetFuelConfigRepository;
import com.app.usochicamochabackend.fuel.infrastructure.repository.RefuelingRecordsRepository;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Exportación a Excel del rendimiento mensual de TODOS los activos configurados
 * (pantalla Rendimiento — botón "Exportar Excel" en FuelPerformance.svelte, distinto
 * del export de historial de UN activo que ya existía en
 * {@link FuelPerformanceHistoryExcelExportService}).
 *
 * Una hoja por mes calendario dentro del rango pedido — incluyendo meses sin ningún
 * tanqueo, para que la descarga nunca "salte" un mes — con UNA sola tabla por activo:
 * nombre + consumo estándar configurado (asset_fuel_config, fijo mes a mes) junto al
 * rendimiento real de ese mes (horómetro/km primero y último tanqueo, consumo total,
 * recorrido y eficiencia real). Antes eran dos tablas lado a lado emparejadas solo por
 * posición de fila (sin nombre en la de rendimiento) — se rompía en cuanto un activo
 * no tenía tanqueos ese mes, porque esa tabla se acortaba y la de configuración no.
 * Ahora TODOS los activos configurados aparecen siempre, con "-" en las columnas de
 * rendimiento cuando no hay tanqueos ese mes, así el archivo nunca deja de "cuadrar".
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FuelMonthlyPerformanceExcelExportService implements ReporteExcel {

    private static final DateTimeFormatter MES_TITULO_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-CO"));
    private static final DateTimeFormatter MES_SHEET_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int NUM_COLUMNAS = 7;

    private static final Map<String, String> UNIDAD_CONSUMO_LABELS = Map.of(
            "KM_POR_GALON", "Km/Gl",
            "HORA_POR_GALON", "H/Gl",
            "KM_POR_M3", "Km/M3",
            "HORA_POR_M3", "H/M3");

    private final RefuelingRecordsRepository refuelingRecordsRepository;
    private final AssetFuelConfigRepository assetFuelConfigRepository;
    private final VehicleRepository vehicleRepository;
    private final MachineRepository machineRepository;

    private record InfoActivo(String tipo, String nombre) {}

    private record FilaActivoMes(String nombre, String consumoEstandarLabel, BigDecimal primero, BigDecimal ultimo,
            BigDecimal consumo, BigDecimal recorrido, BigDecimal eficiencia) {}

    // --- Reporte del motor único (GET /api/v1/reportes/rendimiento-mensual) ---

    @Override public String clave() { return "rendimiento-mensual"; }
    @Override public String titulo() { return "Rendimiento mensual de combustible"; }
    @Override public String modulo() { return ReporteExcel.COMBUSTIBLE; }
    @Override public boolean usaPeriodo() { return true; }
    @Override public String periodoInicial() { return "mes"; }

    /** Sin fechas: el año en curso hasta hoy (una hoja por mes, no puede ser "todo"). */
    @Override
    public Periodo periodo(PeticionReporte p) {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = p.periodo().desde();
        LocalDate hasta = p.periodo().hasta();
        return new Periodo(desde != null ? desde : (hasta != null ? hasta : hoy).withDayOfYear(1),
                hasta != null ? hasta : hoy);
    }

    @Override
    public void llenar(LibroExcel libro, PeticionReporte p) {
        Periodo periodo = periodo(p);
        LocalDate inicio = periodo.desde();
        LocalDate fin = periodo.hasta();
        Timestamp inicioTs = Timestamp.valueOf(LocalDateTime.of(inicio, LocalTime.MIN));
        Timestamp finTs = Timestamp.valueOf(LocalDateTime.of(fin, LocalTime.MAX));

        List<RefuelingRecordsEntity> tanqueos = refuelingRecordsRepository.findByFechaRegistroBetweenAndStatusTrue(inicioTs, finTs);
        // Todos los activos con consumo estándar configurado, sin importar tipo — el
        // export mensual no depende de qué pestaña (Maquinaria/Vehículo/Motocicleta)
        // esté activa en pantalla, siempre trae los 3 tipos juntos.
        List<AssetFuelConfigEntity> configs = assetFuelConfigRepository.findAll();

        Map<String, InfoActivo> infoPorClave = new LinkedHashMap<>();
        for (AssetFuelConfigEntity c : configs) {
            infoPorClave.put(clave(c.getVehicleId(), c.getMachineId()), resolverInfo(c));
        }
        List<AssetFuelConfigEntity> configsOrdenados = configs.stream()
                .sorted(Comparator
                        .comparing((AssetFuelConfigEntity c) -> infoPorClave.get(clave(c.getVehicleId(), c.getMachineId())).tipo())
                        .thenComparing(c -> infoPorClave.get(clave(c.getVehicleId(), c.getMachineId())).nombre()))
                .toList();

        Map<YearMonth, List<RefuelingRecordsEntity>> tanqueosPorMes = tanqueos.stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getFechaRegistro().toLocalDateTime())));

        List<ColumnaExcel<FilaActivoMes>> cols = List.of(
                ColumnaExcel.texto("Maquinaria / vehículo", FilaActivoMes::nombre),
                ColumnaExcel.texto("Consumo estándar", FilaActivoMes::consumoEstandarLabel),
                ColumnaExcel.decimal("Horas / km primer tanqueo del mes", FilaActivoMes::primero),
                ColumnaExcel.decimal("Horas / km último tanqueo del mes", FilaActivoMes::ultimo),
                ColumnaExcel.decimal("Consumo de combustible (gal)", FilaActivoMes::consumo).conTotal(),
                ColumnaExcel.decimal("Recorrido del mes", FilaActivoMes::recorrido).conTotal(),
                ColumnaExcel.decimal("Rendimiento real (recorrido ÷ galón)", FilaActivoMes::eficiencia));
        YearMonth mesInicio = YearMonth.from(inicio);
        YearMonth mesFin = YearMonth.from(fin);
        {
            libro.cifra("Activos configurados", configsOrdenados.size())
                    .cifra("Meses", mesInicio.until(mesFin, java.time.temporal.ChronoUnit.MONTHS) + 1)
                    .cifra("Tanqueos", tanqueos.size())
                    .cifra("Galones", tanqueos.stream().map(RefuelingRecordsEntity::getCantidadGalones).filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue())
                    .nota("Todos los activos configurados salen en todos los meses; sin tanqueos ese mes, las lecturas quedan vacías.");
            for (YearMonth mes = mesInicio; !mes.isAfter(mesFin); mes = mes.plusMonths(1)) {
                List<FilaActivoMes> filas = calcularFilasDelMes(tanqueosPorMes.getOrDefault(mes, List.of()),
                        configsOrdenados, infoPorClave);
                String titulo = mes.format(MES_TITULO_FORMATTER);
                libro.tabla(mes.format(MES_SHEET_FORMATTER), "Rendimiento de " + titulo.substring(0, 1).toUpperCase() + titulo.substring(1),
                        cols, filas);
            }
            log.info("Excel de rendimiento mensual generado: {} a {}, {} activos", mesInicio, mesFin, configsOrdenados.size());
        }
    }

    private List<FilaActivoMes> calcularFilasDelMes(List<RefuelingRecordsEntity> tanqueosDelMes,
            List<AssetFuelConfigEntity> configsOrdenados, Map<String, InfoActivo> infoPorClave) {
        Map<String, List<RefuelingRecordsEntity>> porActivo = tanqueosDelMes.stream()
                .collect(Collectors.groupingBy(t -> clave(t.getVehicleId(), t.getMachineId())));

        List<FilaActivoMes> filas = new ArrayList<>();
        for (AssetFuelConfigEntity cfg : configsOrdenados) {
            String key = clave(cfg.getVehicleId(), cfg.getMachineId());
            List<RefuelingRecordsEntity> delActivo = porActivo.getOrDefault(key, List.of()).stream()
                    .sorted(Comparator.comparing(RefuelingRecordsEntity::getFechaRegistro))
                    .toList();

            BigDecimal consumo = delActivo.stream().map(RefuelingRecordsEntity::getCantidadGalones)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Un horómetro/km en 0 se trata como "sin lectura registrada" (tanqueo
            // cargado solo por el consumo, sin anotar la lectura real) — no cuenta
            // para primero/último ni para el recorrido, pero el consumo sí suma.
            List<BigDecimal> lecturas = delActivo.stream()
                    .map(RefuelingRecordsEntity::getHorometroKm)
                    .filter(h -> h != null && h.signum() > 0)
                    .toList();
            BigDecimal primero = lecturas.isEmpty() ? null : lecturas.get(0);
            BigDecimal ultimo = lecturas.isEmpty() ? null : lecturas.get(lecturas.size() - 1);
            BigDecimal recorrido = (primero != null && ultimo != null) ? ultimo.subtract(primero) : null;
            BigDecimal eficiencia = (recorrido != null && consumo.compareTo(BigDecimal.ZERO) != 0)
                    ? recorrido.divide(consumo, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            filas.add(new FilaActivoMes(infoPorClave.get(key).nombre(), formatoConsumoEstandar(cfg),
                    primero, ultimo, consumo, recorrido, eficiencia));
        }
        return filas;
    }

    private String clave(Integer vehicleId, Long machineId) {
        return machineId != null ? "M-" + machineId : "V-" + vehicleId;
    }

    private InfoActivo resolverInfo(AssetFuelConfigEntity cfg) {
        if (cfg.getMachineId() != null) {
            String nombre = machineRepository.findById(cfg.getMachineId())
                    .map(MachineEntity::getName).orElse("Máquina #" + cfg.getMachineId());
            return new InfoActivo("Máquina", nombre);
        }
        return vehicleRepository.findById(cfg.getVehicleId())
                .map(v -> {
                    boolean esMoto = v.getTipoVehiculo() != null && "MOTOCICLETA".equalsIgnoreCase(v.getTipoVehiculo().getNombreTipo());
                    String tipo = esMoto ? "Motocicleta" : "Vehículo";
                    String marca = v.getMarca() != null ? v.getMarca().getDescripcion() : null;
                    String nombre = marca != null ? v.getPlaca() + " — " + marca : v.getPlaca();
                    return new InfoActivo(tipo, nombre);
                })
                .orElse(new InfoActivo("Vehículo", "Vehículo #" + cfg.getVehicleId()));
    }

    private String formatoConsumoEstandar(AssetFuelConfigEntity cfg) {
        String unidad = UNIDAD_CONSUMO_LABELS.getOrDefault(cfg.getUnidadConsumo(), cfg.getUnidadConsumo());
        return fmt2(cfg.getConsumoEstandar()) + " " + unidad;
    }

    private String fmt2(BigDecimal value) {
        java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(new Locale("es", "CO"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(value != null ? value : BigDecimal.ZERO);
    }
}
