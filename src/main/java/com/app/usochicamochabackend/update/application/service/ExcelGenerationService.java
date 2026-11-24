package com.app.usochicamochabackend.update.application.service;

import com.app.usochicamochabackend.common.excel.ColumnaExcel;
import com.app.usochicamochabackend.common.excel.ColumnaExcel.Estado;
import com.app.usochicamochabackend.common.excel.LibroExcel;
import com.app.usochicamochabackend.common.excel.Periodo;
import com.app.usochicamochabackend.context.application.dto.MachineCurriculumDTO;
import com.app.usochicamochabackend.context.application.dto.VehicleCurriculumDTO;
import com.app.usochicamochabackend.moto.application.dto.MotoMonitoringDTO;
import com.app.usochicamochabackend.order.application.dto.OrderWithMachineDTO;
import com.app.usochicamochabackend.order.application.dto.OrderWithVehicleDTO;
import com.app.usochicamochabackend.order.application.dto.OrderWithoutInspectionResponse;
import com.app.usochicamochabackend.performance.application.dto.ResultDTO;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.update.application.dto.ConsolidateHydraulicAndMotorOilDTO;
import com.app.usochicamochabackend.vehicle.application.dto.VehicleMonitoringDTO;
import com.app.usochicamochabackend.vehicleinspection.application.dto.VehicleInspectionReportDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Descargas a Excel de flota y maquinaria (consolidados, hojas de vida, inspecciones y órdenes),
 * todas con el estilo común de {@link LibroExcel}: portada con periodo, filtros, cifras e índice,
 * y hojas con título, encabezado de color, filtros, totales y formato de impresión.
 *
 * <p>Los listados con fecha (inspecciones, órdenes, historial de las hojas de vida) se recortan al
 * {@link Periodo} pedido; los consolidados son una foto del estado actual ("corte" de hoy).
 */
@Slf4j
@Service
public class ExcelGenerationService {

    private static final Locale ES = Locale.forLanguageTag("es-CO");

    // ---------------------------------------------------------------------
    // Consolidado de aceites de maquinaria
    // ---------------------------------------------------------------------

    public void llenarConsolidadoAceites(LibroExcel libro, List<ConsolidateHydraulicAndMotorOilDTO> datos) throws IOException {
        log.info("Excel consolidado de maquinaria: {} máquinas", datos.size());
        List<ColumnaExcel<ConsolidateHydraulicAndMotorOilDTO>> cols = new ArrayList<>(List.of(
                ColumnaExcel.texto("Pertenece a", d -> d.machine().belongsTo()),
                ColumnaExcel.texto("Máquina", d -> d.machine().name()),
                ColumnaExcel.decimal("Horómetro actual", d -> d.currentData() != null ? d.currentData().currentHourMeter() : null),
                ColumnaExcel.fechaHora("Último reporte", d -> d.currentData() != null ? d.currentData().lastUpdate() : null)));
        cols.addAll(columnasAceite("Motor", d -> d.consolidateMotorOil() == null ? null : new Aceite(
                d.consolidateMotorOil().brand() != null ? d.consolidateMotorOil().brand().getName() : null,
                d.consolidateMotorOil().quantity(), d.consolidateMotorOil().averageChangeHours(),
                d.consolidateMotorOil().dateLastUpdate(), d.consolidateMotorOil().hourMeterLastUpdate(),
                d.consolidateMotorOil().hourMeterNextUpdate(), d.consolidateMotorOil().timeLastUpdateMouths(),
                d.consolidateMotorOil().remainingHoursNextUpdateMouths(), d.consolidateMotorOil().status())));
        cols.addAll(columnasAceite("Hidráulico", d -> d.consolidateHydraulicOil() == null ? null : new Aceite(
                d.consolidateHydraulicOil().brand() != null ? d.consolidateHydraulicOil().brand().getName() : null,
                d.consolidateHydraulicOil().quantity(), d.consolidateHydraulicOil().averageChangeHours(),
                d.consolidateHydraulicOil().dateLastUpdate(), d.consolidateHydraulicOil().hourMeterLastUpdate(),
                d.consolidateHydraulicOil().hourMeterNextUpdate(), d.consolidateHydraulicOil().timeLastUpdateMouths(),
                d.consolidateHydraulicOil().remainingHoursNextUpdateMouths(), d.consolidateHydraulicOil().status())));

            libro.filtro("Corte", LocalDate.now()).cifra("Máquinas", datos.size())
                    .cifra("Cambios de aceite de motor vencidos", contar(datos, d -> d.consolidateMotorOil() != null ? d.consolidateMotorOil().status() : null, Estado.MAL))
                    .cifra("Cambios de aceite hidráulico vencidos", contar(datos, d -> d.consolidateHydraulicOil() != null ? d.consolidateHydraulicOil().status() : null, Estado.MAL))
                    .nota("Foto del estado actual: no depende de un periodo.");
            libro.tabla("Consolidado", "Estado de los aceites por máquina", cols, datos);
    }

    /** Lo común del aceite de motor e hidráulico en el consolidado de maquinaria. */
    private record Aceite(String marca, Double cantidad, Integer promedioHoras, LocalDate fechaUltimo,
            Double horometroUltimo, Double horometroProximo, Double mesesDesdeUltimo, Double horasRestantes, String estado) {
    }

    private static <T> List<ColumnaExcel<T>> columnasAceite(String cual, Function<T, Aceite> a) {
        Function<Function<Aceite, Object>, Function<T, Object>> de = f -> t -> {
            Aceite x = a.apply(t);
            return x == null ? null : f.apply(x);
        };
        return List.of(
                ColumnaExcel.texto(cual + " · tipo / marca", de.apply(Aceite::marca)),
                ColumnaExcel.decimal(cual + " · cantidad (gl)", de.apply(Aceite::cantidad)),
                ColumnaExcel.numero(cual + " · promedio cambio (h)", de.apply(Aceite::promedioHoras)),
                ColumnaExcel.fecha(cual + " · último cambio", de.apply(Aceite::fechaUltimo)),
                ColumnaExcel.decimal(cual + " · horómetro último", de.apply(Aceite::horometroUltimo)),
                ColumnaExcel.decimal(cual + " · horómetro próximo", de.apply(Aceite::horometroProximo)),
                ColumnaExcel.decimal(cual + " · meses desde el cambio", de.apply(Aceite::mesesDesdeUltimo)),
                ColumnaExcel.decimal(cual + " · horas restantes", de.apply(Aceite::horasRestantes)),
                ColumnaExcel.texto(cual + " · estado", de.apply(Aceite::estado))
                        .conEstado(t -> estadoDe(a.apply(t) == null ? null : a.apply(t).estado())));
    }

    // ---------------------------------------------------------------------
    // Consolidados de vehículos y motos
    // ---------------------------------------------------------------------

    public void llenarConsolidadoVehiculos(LibroExcel libro, List<VehicleMonitoringDTO> datos) throws IOException {
        log.info("Excel consolidado de vehículos: {} registros", datos.size());
        List<ColumnaExcel<VehicleMonitoringDTO>> cols = new ArrayList<>(List.of(
                ColumnaExcel.texto("Pertenece a", VehicleMonitoringDTO::area),
                ColumnaExcel.texto("Placa", VehicleMonitoringDTO::placa),
                ColumnaExcel.numero("Km actual", VehicleMonitoringDTO::kmActual),
                ColumnaExcel.numero("Días sin reporte", (VehicleMonitoringDTO d) -> d.diasUltimoReporte())
                        .conEstado(d -> diasSinReporte(d.diasUltimoReporte())),
                ColumnaExcel.fechaHora("Último reporte", VehicleMonitoringDTO::fechaUltimoReporte)));
        cols.addAll(columnasAceiteKm(d -> d.maintenance() == null ? null : new AceiteKm(d.maintenance().brandName(),
                d.maintenance().quantity(), d.maintenance().intervalKm(), d.maintenance().fechaUltimoCambio(),
                d.maintenance().kmCambio(), d.maintenance().kmProximoCambio(), d.maintenance().diasDesdeUltimoCambio(),
                d.maintenance().kmParaProximo(), d.maintenance().filtroAire(), d.maintenance().estado())));
        consolidado(libro, "Vehículos", cols, datos,
                d -> d.maintenance() != null ? d.maintenance().estado() : null);
    }

    public void llenarConsolidadoMotos(LibroExcel libro, List<MotoMonitoringDTO> datos) throws IOException {
        log.info("Excel consolidado de motos: {} registros", datos.size());
        List<ColumnaExcel<MotoMonitoringDTO>> cols = new ArrayList<>(List.of(
                ColumnaExcel.texto("Pertenece a", MotoMonitoringDTO::departamento),
                ColumnaExcel.texto("Placa", MotoMonitoringDTO::placa),
                ColumnaExcel.numero("Km actual", MotoMonitoringDTO::kmActual),
                ColumnaExcel.numero("Días sin reporte", (MotoMonitoringDTO d) -> d.diasUltimoReporte())
                        .conEstado(d -> diasSinReporte(d.diasUltimoReporte())),
                ColumnaExcel.fechaHora("Último reporte", MotoMonitoringDTO::fechaUltimoReporte)));
        cols.addAll(columnasAceiteKm(d -> d.oil() == null ? null : new AceiteKm(d.oil().brandName(), d.oil().quantity(),
                d.oil().intervalKm(), d.oil().fechaUltimoCambio(), d.oil().kmCambio(), d.oil().kmProximoCambio(),
                d.oil().diasDesdeUltimoCambio(), d.oil().kmParaProximo(), d.oil().filtroAire(), d.oil().estado())));
        consolidado(libro, "Motos", cols, datos, d -> d.oil() != null ? d.oil().estado() : null);
    }

    private record AceiteKm(String marca, Object cantidad, Object intervalo, LocalDate fechaUltimo, Object kmCambio,
            Object kmProximo, Object diasDesde, Object kmRestantes, Boolean filtroAire, String estado) {
    }

    private static <T> List<ColumnaExcel<T>> columnasAceiteKm(Function<T, AceiteKm> a) {
        Function<Function<AceiteKm, Object>, Function<T, Object>> de = f -> t -> {
            AceiteKm x = a.apply(t);
            return x == null ? null : f.apply(x);
        };
        return List.of(
                ColumnaExcel.texto("Aceite · marca", de.apply(AceiteKm::marca)),
                ColumnaExcel.decimal("Cantidad", de.apply(AceiteKm::cantidad)),
                ColumnaExcel.numero("Intervalo (km)", de.apply(AceiteKm::intervalo)),
                ColumnaExcel.fecha("Último cambio", de.apply(AceiteKm::fechaUltimo)),
                ColumnaExcel.numero("Km último cambio", de.apply(AceiteKm::kmCambio)),
                ColumnaExcel.numero("Próximo cambio (km)", de.apply(AceiteKm::kmProximo)),
                ColumnaExcel.numero("Días desde el cambio", de.apply(AceiteKm::diasDesde)),
                ColumnaExcel.numero("Km restantes", de.apply(AceiteKm::kmRestantes)),
                ColumnaExcel.texto("Filtro de aire", de.apply(x -> siNo(x.filtroAire()))),
                ColumnaExcel.texto("Estado", de.apply(AceiteKm::estado))
                        .conEstado(t -> estadoDe(a.apply(t) == null ? null : a.apply(t).estado())));
    }

    private <T> void consolidado(LibroExcel libro, String que, List<ColumnaExcel<T>> cols, List<T> datos,
            Function<T, String> estado) throws IOException {
            libro.filtro("Corte", LocalDate.now()).cifra(que, datos.size())
                    .cifra("Cambio de aceite vencido", contar(datos, estado, Estado.MAL))
                    .cifra("Cambio de aceite próximo", contar(datos, estado, Estado.ALERTA))
                    .nota("Foto del estado actual: no depende de un periodo.");
            libro.tabla("Consolidado", "Estado del aceite por " + que.toLowerCase(ES), cols, datos);
    }

    // ---------------------------------------------------------------------
    // Hojas de vida (una hoja por máquina o vehículo)
    // ---------------------------------------------------------------------

    public void llenarHojasDeVidaMaquinaria(LibroExcel libro, List<MachineCurriculumDTO> hojas, Periodo periodo) throws IOException {
        log.info("Excel de hojas de vida de {} máquinas", hojas.size());
            int trabajos = 0;
            BigDecimal valor = BigDecimal.ZERO;
            for (MachineCurriculumDTO h : hojas) {
                List<FilaHistorial> filas = h.inspections().stream()
                        .filter(i -> periodo.contiene(i.dateStamp()))
                        .flatMap(i -> i.results().stream().map(r -> new FilaHistorial(i.dateStamp() != null ? i.dateStamp() : r.date(),
                                i.hourMeter(), r)))
                        .toList();
                LibroExcel.Hoja hoja = libro.hoja(h.machine().name(), "Hoja de vida · " + h.machine().name(),
                        "Ficha y trabajos realizados");
                hoja.ficha("Datos de la máquina", List.of(
                        par("Equipo", texto(h.machine().name())),
                        par("N.º interno", texto(h.machine().numInterIdentification())),
                        par("Modelo", texto(h.machine().model())),
                        par("N.º de motor", texto(h.machine().numEngine())),
                        par("Vigencia SOAT", h.machine().soat()),
                        par("Vigencia RUNT", h.machine().runt()),
                        par("Trabajos en el periodo", filas.size()),
                        par("Valor total en el periodo", pesos(sumar(filas)))));
                hoja.tabla("Trabajos realizados", columnasHistorial("Horómetro"), filas);
                hoja.terminar(13);
                trabajos += filas.size();
                valor = valor.add(sumar(filas));
            }
            libro.cifra("Máquinas", hojas.size()).cifra("Trabajos realizados", trabajos).cifra("Valor total", pesos(valor));
    }

    public void llenarHojasDeVidaVehiculos(LibroExcel libro, List<VehicleCurriculumDTO> hojas, Periodo periodo,
            String que) throws IOException {
        log.info("Excel de hojas de vida de {} {}", hojas.size(), que);
            int trabajos = 0;
            BigDecimal valor = BigDecimal.ZERO;
            for (VehicleCurriculumDTO h : hojas) {
                List<FilaHistorial> filas = h.results().stream()
                        .filter(r -> periodo.contiene(r.date()))
                        .map(r -> new FilaHistorial(r.date(), r.hourMeter(), r))
                        .toList();
                String placa = h.vehicle().placa() != null ? h.vehicle().placa() : "Sin placa";
                LibroExcel.Hoja hoja = libro.hoja(placa, "Hoja de vida · " + placa, "Ficha y trabajos realizados");
                hoja.ficha("Datos del vehículo", List.of(
                        par("Placa", placa),
                        par("Marca", texto(h.vehicle().marca())),
                        par("Tipo", texto(h.vehicle().tipoVehiculo())),
                        par("Km actual", h.vehicle().kilometrajeActual() == null ? "—"
                                : String.format(ES, "%,d", h.vehicle().kilometrajeActual().longValue())),
                        par("Área", texto(h.vehicle().belongsTo())),
                        par("Ubicación", texto(h.vehicle().ubicacionBase())),
                        par("Trabajos en el periodo", filas.size()),
                        par("Valor total en el periodo", pesos(sumar(filas)))));
                hoja.tabla("Trabajos realizados", columnasHistorial("Kilómetro"), filas);
                hoja.terminar(13);
                trabajos += filas.size();
                valor = valor.add(sumar(filas));
            }
            libro.cifra(que, hojas.size()).cifra("Trabajos realizados", trabajos).cifra("Valor total", pesos(valor));
    }

    /** Un trabajo de la hoja de vida: el resultado de una orden, con la fecha y el horómetro/km. */
    private record FilaHistorial(java.time.LocalDateTime fecha, Double medida, ResultDTO r) {
    }

    private static List<ColumnaExcel<FilaHistorial>> columnasHistorial(String medida) {
        return List.of(
                ColumnaExcel.fechaHora("Fecha", FilaHistorial::fecha),
                ColumnaExcel.decimal(medida, FilaHistorial::medida),
                ColumnaExcel.texto("Descripción", f -> f.r().description()),
                ColumnaExcel.texto("REF", f -> f.r().sparePart() != null ? f.r().sparePart().ref() : null),
                ColumnaExcel.texto("Repuesto", f -> f.r().sparePart() != null ? f.r().sparePart().name() : null),
                ColumnaExcel.texto("Cantidad", f -> f.r().sparePart() != null ? f.r().sparePart().quantity() : null),
                ColumnaExcel.moneda("Valor repuesto", (FilaHistorial f) -> f.r().sparePart() != null ? f.r().sparePart().price() : null).conTotal(),
                ColumnaExcel.texto("Mecánico de planta", f -> f.r().labor() != null && f.r().labor().user() != null
                        ? f.r().labor().user().getFullName() : null),
                ColumnaExcel.texto("Contratista", f -> f.r().labor() != null ? f.r().labor().contractor() : null),
                ColumnaExcel.texto("Tiempo empleado", f -> f.r().timeSpent()),
                ColumnaExcel.moneda("Valor mano de obra", (FilaHistorial f) -> f.r().labor() != null ? f.r().labor().price() : null).conTotal(),
                ColumnaExcel.moneda("Valor total", (FilaHistorial f) -> f.r().totalPrice()).conTotal(),
                ColumnaExcel.texto("Descripción mano de obra", f -> f.r().labor() != null ? f.r().labor().observations() : null));
    }

    private static BigDecimal sumar(List<FilaHistorial> filas) {
        return filas.stream().map(f -> f.r().totalPrice()).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ---------------------------------------------------------------------
    // Inspecciones
    // ---------------------------------------------------------------------

    public void llenarInspeccionesMaquinaria(LibroExcel libro, List<InspectionEntity> todas, Periodo periodo) throws IOException {
        List<InspectionEntity> inspecciones = todas.stream().filter(i -> periodo.contiene(i.getDateStamp())).toList();
        log.info("Excel de inspecciones de maquinaria: {} de {}", inspecciones.size(), todas.size());
        List<ColumnaExcel<InspectionEntity>> cols = List.of(
                ColumnaExcel.texto("Mes", i -> i.getDateStamp() == null ? null
                        : capitalizar(i.getDateStamp().getMonth().getDisplayName(TextStyle.FULL, ES))),
                ColumnaExcel.fechaHora("Fecha y hora", InspectionEntity::getDateStamp),
                ColumnaExcel.texto("Máquina", i -> i.getMachine() != null ? i.getMachine().getName() : null),
                ColumnaExcel.decimal("Horómetro", InspectionEntity::getHourMeter),
                revision("Fugas en el sistema", InspectionEntity::getLeakStatus),
                revision("Sistema de frenos", InspectionEntity::getBrakeStatus),
                revision("Correas y poleas", InspectionEntity::getBeltsPulleysStatus),
                revision("Llantas y/o carriles", InspectionEntity::getTireLanesStatus),
                revision("Sistema de encendido", InspectionEntity::getCarIgnitionStatus),
                revision("Sistema eléctrico", InspectionEntity::getElectricalStatus),
                revision("Sistema mecánico", InspectionEntity::getMechanicalStatus),
                revision("Nivel de temperatura", InspectionEntity::getTemperatureStatus),
                revision("Nivel de aceite", InspectionEntity::getOilStatus),
                revision("Nivel de hidráulico", InspectionEntity::getHydraulicStatus),
                revision("Nivel de refrigerante", InspectionEntity::getCoolantStatus),
                revision("Estado estructural", InspectionEntity::getStructuralStatus),
                ColumnaExcel.texto("Vigencia extintor", InspectionEntity::getExpirationDateFireExtinguisher),
                ColumnaExcel.texto("Observaciones", InspectionEntity::getObservations),
                ColumnaExcel.texto("Engrasado", InspectionEntity::getGreasingAction),
                ColumnaExcel.texto("Observaciones engrasado", InspectionEntity::getGreasingObservations),
                ColumnaExcel.texto("Responsable", i -> i.getUser() != null ? i.getUser().getFullName() : null));
            libro.cifra("Inspecciones", inspecciones.size())
                    .cifra("Máquinas inspeccionadas", inspecciones.stream().map(i -> i.getMachine() != null ? i.getMachine().getId() : null)
                            .filter(Objects::nonNull).distinct().count())
                    .nota("Las revisiones con novedad salen en amarillo o rojo.");
            libro.tabla("Inspecciones", "Inspecciones pre-operativas de maquinaria", cols, inspecciones);
    }

    public void llenarInspeccionesVehiculos(LibroExcel libro, List<VehicleInspectionReportDTO> todas, Periodo periodo,
            String que) throws IOException {
        List<VehicleInspectionReportDTO> inspecciones = todas.stream().filter(i -> periodo.contiene(i.fechaRegistro())).toList();
        log.info("Excel de inspecciones de {}: {} de {}", que, inspecciones.size(), todas.size());
        List<ColumnaExcel<VehicleInspectionReportDTO>> cols = List.of(
                ColumnaExcel.fechaHora("Fecha y hora", VehicleInspectionReportDTO::fechaRegistro),
                ColumnaExcel.texto("Placa", VehicleInspectionReportDTO::placa),
                ColumnaExcel.texto("Marca", VehicleInspectionReportDTO::marca),
                ColumnaExcel.texto("Tipo", VehicleInspectionReportDTO::tipoVehiculo),
                ColumnaExcel.texto("Área", VehicleInspectionReportDTO::areaOrganizacional),
                ColumnaExcel.texto("Ubicación", VehicleInspectionReportDTO::ubicacion),
                ColumnaExcel.texto("Responsable", VehicleInspectionReportDTO::responsable),
                ColumnaExcel.numero("Kilometraje", VehicleInspectionReportDTO::kilometraje),
                ColumnaExcel.texto("Aprobado para ruta", (VehicleInspectionReportDTO d) -> siNo(d.aprobadoRuta()))
                        .conEstado(d -> d.aprobadoRuta() == null ? null : d.aprobadoRuta() ? Estado.OK : Estado.MAL),
                revision("Nivel de aceite", VehicleInspectionReportDTO::nivelAceite),
                revision("Nivel de refrigerante", VehicleInspectionReportDTO::nivelRefrigerante),
                revision("Nivel de frenos", VehicleInspectionReportDTO::nivelFrenos),
                revision("Estado de llantas", VehicleInspectionReportDTO::estadoLlantas),
                revision("Luces", VehicleInspectionReportDTO::lucesGeneral),
                revision("Estado visual", VehicleInspectionReportDTO::estadoVisual),
                revision("Limpieza", VehicleInspectionReportDTO::limpiezaGeneral),
                revision("SOAT", VehicleInspectionReportDTO::checkSoat),
                revision("Tecnomecánica", VehicleInspectionReportDTO::checkTecno),
                revision("Extintor", VehicleInspectionReportDTO::checkExtintor),
                siNoCol("Botiquín", VehicleInspectionReportDTO::tieneBotiquin),
                siNoCol("Señalización", VehicleInspectionReportDTO::tieneSeñalizacion),
                siNoCol("Líneas de emergencia", VehicleInspectionReportDTO::tieneLineasEmergencia),
                ColumnaExcel.texto("Llanta de repuesto", VehicleInspectionReportDTO::tieneLlantaRepuesto),
                ColumnaExcel.texto("Gato hidráulico", VehicleInspectionReportDTO::tieneGatoHidraulico),
                siNoCol("Salud física", VehicleInspectionReportDTO::saludFisica),
                siNoCol("Salud mental", VehicleInspectionReportDTO::saludMental),
                siNoCol("Sobriedad", VehicleInspectionReportDTO::sobrio),
                // Aquí lo malo es el "Sí": consume medicamentos que afectan los sentidos.
                siNoCol("Medicamentos", VehicleInspectionReportDTO::medicamentos, false),
                siNoCol("Condición para conducir", VehicleInspectionReportDTO::condicionParaConducir),
                siNoCol("Consciente de su responsabilidad", VehicleInspectionReportDTO::conscienteResponsabilidad),
                ColumnaExcel.texto("Mecánico de planta", VehicleInspectionReportDTO::mechanicName),
                ColumnaExcel.texto("Contratista", VehicleInspectionReportDTO::contractorName),
                ColumnaExcel.texto("Tiempo empleado", VehicleInspectionReportDTO::timeSpent),
                ColumnaExcel.texto("Observaciones", VehicleInspectionReportDTO::observacionesFinales));
            libro.cifra("Inspecciones", inspecciones.size())
                    .cifra("No aprobadas para ruta", inspecciones.stream().filter(i -> Boolean.FALSE.equals(i.aprobadoRuta())).count())
                    .cifra(que + " inspeccionados", inspecciones.stream().map(VehicleInspectionReportDTO::placa)
                            .filter(Objects::nonNull).distinct().count());
            libro.tabla("Inspecciones", "Inspecciones pre-operativas de " + que.toLowerCase(ES), cols, inspecciones);
    }

    // ---------------------------------------------------------------------
    // Órdenes de trabajo
    // ---------------------------------------------------------------------

    public void llenarOrdenesMaquinaria(LibroExcel libro, List<OrderWithMachineDTO> todas, Periodo periodo) throws IOException {
        List<OrderWithMachineDTO> ordenes = todas.stream().filter(o -> o.order() != null && periodo.contiene(o.order().date())).toList();
        log.info("Excel de órdenes de maquinaria: {} de {}", ordenes.size(), todas.size());
        List<ColumnaExcel<OrderWithMachineDTO>> cols = new ArrayList<>(columnasOrden(OrderWithMachineDTO::order));
        cols.add(4, ColumnaExcel.texto("Máquina", o -> o.machine() != null ? o.machine().name() : null));
        cols.add(5, ColumnaExcel.texto("Área", o -> o.machine() != null ? o.machine().belongsTo() : null));
        ordenes(libro, "Órdenes de trabajo · Maquinaria", cols, ordenes, OrderWithMachineDTO::order);
    }

    public void llenarOrdenesVehiculos(LibroExcel libro, List<OrderWithVehicleDTO> todas, Periodo periodo, String que) throws IOException {
        List<OrderWithVehicleDTO> ordenes = todas.stream().filter(o -> o.order() != null && periodo.contiene(o.order().date())).toList();
        log.info("Excel de órdenes de {}: {} de {}", que, ordenes.size(), todas.size());
        List<ColumnaExcel<OrderWithVehicleDTO>> cols = new ArrayList<>(columnasOrden(OrderWithVehicleDTO::order));
        cols.add(4, ColumnaExcel.texto("Placa", o -> o.vehicle() != null ? o.vehicle().placa() : null));
        cols.add(5, ColumnaExcel.texto("Marca", o -> o.vehicle() != null ? o.vehicle().marca() : null));
        cols.add(6, ColumnaExcel.texto("Tipo de vehículo", o -> o.vehicle() != null ? o.vehicle().tipoVehiculo() : null));
        cols.add(7, ColumnaExcel.fechaHora("Fecha de la inspección", o -> o.vehicle() != null ? o.vehicle().fechaInspeccion() : null));
        ordenes(libro, "Órdenes de trabajo · " + que, cols, ordenes, OrderWithVehicleDTO::order);
    }

    private static <T> List<ColumnaExcel<T>> columnasOrden(Function<T, OrderWithoutInspectionResponse> o) {
        return List.of(
                ColumnaExcel.texto("Consecutivo", t -> o.apply(t).consecutive()),
                ColumnaExcel.fechaHora("Fecha", t -> o.apply(t).date()),
                ColumnaExcel.texto("Estado", (T t) -> o.apply(t).status()).conEstado(t -> estadoDe(o.apply(t).status())),
                ColumnaExcel.texto("Descripción", t -> o.apply(t).description()),
                ColumnaExcel.texto("Asignada por", t -> o.apply(t).assignerUser() != null ? o.apply(t).assignerUser().fullName() : null),
                ColumnaExcel.texto("Proveedor de repuestos", t -> o.apply(t).suppliers()),
                ColumnaExcel.texto("Tiempo de ejecución", t -> tiempoEjecucion(o.apply(t))));
    }

    private <T> void ordenes(LibroExcel libro, String titulo, List<ColumnaExcel<T>> cols, List<T> ordenes,
            Function<T, OrderWithoutInspectionResponse> o) throws IOException {
            libro.cifra("Órdenes", ordenes.size())
                    .cifra("Cerradas", ordenes.stream().filter(t -> estadoDe(o.apply(t).status()) == Estado.OK).count())
                    .cifra("Abiertas o en curso", ordenes.stream().filter(t -> estadoDe(o.apply(t).status()) != Estado.OK).count());
            libro.tabla("Órdenes", titulo, cols, ordenes);
    }

    // ---------------------------------------------------------------------
    // Auxiliares
    // ---------------------------------------------------------------------

    /** "2 h 30 min" desde las horas y minutos de la orden, o el texto que ya traiga. */
    static String tiempoEjecucion(OrderWithoutInspectionResponse o) {
        if (o == null) {
            return null;
        }
        if (o.timeSpent() != null && !o.timeSpent().isBlank()) {
            return o.timeSpent();
        }
        if (o.hoursSpent() == null && o.minutesSpent() == null) {
            return null;
        }
        int h = o.hoursSpent() != null ? o.hoursSpent() : 0;
        int m = o.minutesSpent() != null ? o.minutesSpent() : 0;
        return h > 0 ? (m > 0 ? h + " h " + m + " min" : h + " h") : m + " min";
    }

    /** Una revisión de la inspección, con color según lo que diga (bueno / regular / malo). */
    private static <T> ColumnaExcel<T> revision(String titulo, Function<T, String> valor) {
        return ColumnaExcel.texto(titulo, (T t) -> valor.apply(t)).conEstado(t -> estadoDe(valor.apply(t)));
    }

    /** Sí/No donde lo bueno es el "Sí": el "No" sale en rojo. */
    private static <T> ColumnaExcel<T> siNoCol(String titulo, Function<T, Boolean> valor) {
        return siNoCol(titulo, valor, true);
    }

    /** Sí/No con la respuesta buena de la pregunta: la otra sale en rojo. */
    static <T> ColumnaExcel<T> siNoCol(String titulo, Function<T, Boolean> valor, boolean siEsBueno) {
        return ColumnaExcel.texto(titulo, (T t) -> siNo(valor.apply(t)))
                .conEstado(t -> valor.apply(t) == null || valor.apply(t) == siEsBueno ? null : Estado.MAL);
    }

    /**
     * Color según el texto que guarda el sistema: vencido, malo, no aprobado → rojo; próximo, regular
     * → amarillo; bueno, al día, cerrada → verde. Lo que no se reconoce queda sin color.
     */
    static Estado estadoDe(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String t = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(ES).trim();
        if (t.contains("venc") || t.equals("malo") || t.equals("mal") || t.contains("critic") || t.contains("no cumple")
                || t.contains("deficiente") || t.equals("bajo") || t.contains("fuga") && !t.contains("sin")) {
            return Estado.MAL;
        }
        if (t.contains("prox") || t.contains("regular") || t.contains("pendiente") || t.contains("proceso")
                || t.contains("asignad") || t.contains("revis")) {
            return Estado.ALERTA;
        }
        if (t.equals("bueno") || t.equals("buena") || t.equals("ok") || t.contains("al dia") || t.contains("vigente")
                || t.contains("cerrad") || t.contains("completad") || t.contains("finaliz") || t.equals("cumple")
                || t.equals("normal") || t.equals("adecuado") || t.startsWith("sin ")) {
            return Estado.OK;
        }
        return null;
    }

    private static Estado diasSinReporte(Number dias) {
        if (dias == null) {
            return null;
        }
        return dias.longValue() > 30 ? Estado.MAL : dias.longValue() > 7 ? Estado.ALERTA : null;
    }

    private static <T> long contar(List<T> datos, Function<T, String> estado, Estado buscado) {
        return datos.stream().filter(d -> estadoDe(estado.apply(d)) == buscado).count();
    }

    /** Un dato de la ficha; acepta valores vacíos (Map.entry no: lanza NullPointerException). */
    private static Map.Entry<String, Object> par(String etiqueta, Object valor) {
        return new java.util.AbstractMap.SimpleEntry<>(etiqueta, valor);
    }

    private static String siNo(Boolean b) {
        return b == null ? null : b ? "Sí" : "No";
    }

    private static String texto(Object v) {
        return v == null ? "—" : v.toString();
    }

    private static String pesos(BigDecimal v) {
        return v == null ? "—" : "$ " + String.format(ES, "%,.0f", v);
    }

    private static String capitalizar(String s) {
        return s == null || s.isEmpty() ? s : s.substring(0, 1).toUpperCase(ES) + s.substring(1);
    }
}
