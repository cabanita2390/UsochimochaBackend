package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.common.excel.ColumnaExcel;
import com.app.usochicamochabackend.common.excel.ColumnaExcel.Estado;
import com.app.usochicamochabackend.common.excel.LibroExcel;
import com.app.usochicamochabackend.common.excel.Periodo;
import com.app.usochicamochabackend.common.excel.reporte.ColumnaReporte;
import com.app.usochicamochabackend.common.excel.reporte.FiltroReporte;
import com.app.usochicamochabackend.common.excel.reporte.PeticionReporte;
import com.app.usochicamochabackend.substation.application.dto.EjecucionResponse;
import com.app.usochicamochabackend.substation.application.dto.ElementoMarcadoResponse;
import com.app.usochicamochabackend.substation.application.dto.HojaExportacionResponse;
import com.app.usochicamochabackend.substation.application.dto.IndicadorEstacionResponse;
import com.app.usochicamochabackend.substation.application.dto.ItemRefResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenResumenResponse;
import com.app.usochicamochabackend.substation.application.dto.ResumenActividadResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationIndicadoresUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationOrdenUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.DisciplinaRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EstacionRepository;
import com.app.usochicamochabackend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Descarga a Excel de Estaciones (plan de Electromecánico, sección 3.6): el usuario elige la hoja,
 * los filtros y las columnas. Usa las mismas consultas que la web, para que el archivo y la
 * pantalla den lo mismo. Nunca salen IDs internos, uuid ni historial de ediciones.
 */
@Service
@RequiredArgsConstructor
public class ExportacionEstacionesService {

    private static final List<String> RESULTADOS_HALLAZGO = List.of("CON_HALLAZGOS", "REQUIERE_INTERVENCION");
    private static final int PAGINA = Paginas.TAMANO_MAXIMO;
    /** Cortes de criticidad del Excel de indicadores: Alto ≥ 12 % de las intervenciones, Medio ≥ 6 %. */
    static final double CRITICIDAD_ALTA = 12.0;
    static final double CRITICIDAD_MEDIA = 6.0;
    private static final Map<String, String> ETIQUETAS = Map.ofEntries(
            Map.entry("PREVENTIVO", "Preventivo"), Map.entry("CORRECTIVO", "Correctivo"),
            Map.entry("PREDICTIVO", "Predictivo"), Map.entry("NO_PROGRAMADO", "No programado"),
            Map.entry("INSPECCION", "Inspección"), Map.entry("MANTENIMIENTO", "Mantenimiento"),
            Map.entry("OTRO", "Otro"), Map.entry("CONFORME", "Conforme"),
            Map.entry("CON_HALLAZGOS", "Con hallazgos"), Map.entry("REQUIERE_INTERVENCION", "Requiere intervención"),
            Map.entry("ABIERTO", "Abierto"), Map.entry("EN_PROCESO", "En corrección"), Map.entry("RESUELTO", "Resuelto"),
            Map.entry("ASIGNADA", "Asignada"), Map.entry("POR_VERIFICAR", "Por verificar"),
            Map.entry("CERRADA", "Cerrada"), Map.entry("ANULADA", "Anulada"),
            Map.entry("ALTA", "Alta"), Map.entry("MEDIA", "Media"), Map.entry("BAJA", "Baja"));
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SubstationEjecucionUseCase ejecucionUseCase;
    private final SubstationIndicadoresUseCase indicadoresUseCase;
    private final SubstationOrdenUseCase ordenUseCase;
    private final DisciplinaRepository disciplinaRepository;
    private final EstacionRepository estacionRepository;
    private final CalendarioMantenimiento calendario;

    /** Filtros de la descarga; los que no aplican a la hoja se ignoran. */
    public record Filtros(String disciplina, Integer anio, Integer mes, Long estacionId) {
    }

    /** Una columna: clave estable (la usa la web), cómo se ve y si sale marcada por defecto. */
    private record Col<T>(String clave, boolean porDefecto, ColumnaExcel<T> excel) {
    }

    /** Una hoja descargable: sus columnas, los filtros que usa y cómo trae sus filas. */
    private record Hoja<T>(String clave, String titulo, List<String> filtros, List<Col<T>> columnas,
            Function<Filtros, List<T>> filas) {
    }

    // ---------------------------------------------------------------------
    // Catálogo de hojas y columnas
    // ---------------------------------------------------------------------

    private Map<String, Hoja<?>> hojas() {
        Map<String, String> nombres = nombresDisciplina();
        Function<String, String> disc = c -> c == null ? "" : nombres.getOrDefault(c, c);
        Map<String, Hoja<?>> m = new LinkedHashMap<>();
        m.put("EJECUCIONES", new Hoja<EjecucionResponse>("EJECUCIONES", "Ejecuciones",
                List.of("disciplina", "anio", "mes", "estacionId"), List.of(
                col("fecha", true, ColumnaExcel.fecha("Fecha", EjecucionResponse::fecha)),
                col("estacion", true, ColumnaExcel.texto("Estación", EjecucionResponse::estacionNombre)),
                col("disciplina", true, ColumnaExcel.texto("Disciplina", e -> disc.apply(e.disciplina()))),
                col("origen", true, ColumnaExcel.texto("Origen", ExportacionEstacionesService::origen)),
                col("actividad", true, ColumnaExcel.texto("Actividad", ExportacionEstacionesService::actividad)),
                col("tipoMantenimiento", true, ColumnaExcel.texto("Tipo de mantenimiento", e -> etiqueta(e.tipoMantenimiento()))),
                col("tipoActividad", true, ColumnaExcel.texto("Tipo de actividad", e -> etiqueta(e.tipoActividad()))),
                col("resultado", true, ColumnaExcel.texto("Resultado", (EjecucionResponse e) -> etiqueta(e.resultado()))
                        .conEstado(e -> estadoResultado(e.resultado()))),
                col("operario", true, ColumnaExcel.texto("Operario", EjecucionResponse::responsable)),
                col("conjuntos", true, ColumnaExcel.texto("Conjuntos", ExportacionEstacionesService::conjuntos)),
                col("elementos", true, ColumnaExcel.texto("Elementos", ExportacionEstacionesService::elementos)),
                col("observaciones", true, ColumnaExcel.texto("Observaciones", EjecucionResponse::observaciones)),
                col("fotos", true, ColumnaExcel.numero("Fotos", e -> e.evidencias() == null ? 0 : e.evidencias().size())),
                col("mesSemana", false, ColumnaExcel.texto("Mes / semana", e -> e.mesEjecucion() + " / " + e.semanaEjecucion())),
                col("seguimiento", false, ColumnaExcel.texto("Seguimiento del hallazgo",
                        e -> e.seguimiento() == null ? "" : etiqueta(e.seguimiento().estado())))),
                f -> ejecuciones(f, null)));
        m.put("HALLAZGOS", new Hoja<EjecucionResponse>("HALLAZGOS", "Hallazgos",
                List.of("disciplina", "anio", "mes", "estacionId"), List.of(
                col("fecha", true, ColumnaExcel.fecha("Fecha", EjecucionResponse::fecha)),
                col("estacion", true, ColumnaExcel.texto("Estación", EjecucionResponse::estacionNombre)),
                col("disciplina", true, ColumnaExcel.texto("Disciplina", e -> disc.apply(e.disciplina()))),
                col("actividad", true, ColumnaExcel.texto("Actividad", ExportacionEstacionesService::actividad)),
                col("resultado", true, ColumnaExcel.texto("Resultado", (EjecucionResponse e) -> etiqueta(e.resultado()))
                        .conEstado(e -> estadoResultado(e.resultado()))),
                col("descripcion", true, ColumnaExcel.texto("Descripción", EjecucionResponse::observaciones)),
                col("seguimiento", true, ColumnaExcel.texto("Estado del seguimiento",
                        (EjecucionResponse e) -> e.seguimiento() == null ? "" : etiqueta(e.seguimiento().estado()))
                        .conEstado(ExportacionEstacionesService::estadoSeguimiento)),
                col("orden", true, ColumnaExcel.texto("Orden de trabajo",
                        e -> e.seguimiento() != null && e.seguimiento().orden() != null ? e.seguimiento().orden().consecutivo() : "")),
                col("diasAbierto", true, ColumnaExcel.numero("Días abierto", ExportacionEstacionesService::diasAbierto)),
                col("operario", false, ColumnaExcel.texto("Operario", EjecucionResponse::responsable)),
                col("cierre", false, ColumnaExcel.texto("Cómo se resolvió",
                        e -> e.seguimiento() == null ? "" : e.seguimiento().observacionesCierre()))),
                f -> ejecuciones(f, RESULTADOS_HALLAZGO)));
        m.put("ORDENES", new Hoja<OrdenResumenResponse>("ORDENES", "Órdenes de trabajo",
                List.of("disciplina", "anio", "mes", "estacionId"), List.of(
                col("consecutivo", true, ColumnaExcel.texto("Orden", OrdenResumenResponse::consecutivo)),
                col("estado", true, ColumnaExcel.texto("Estado", (OrdenResumenResponse o) -> etiqueta(o.estado()))
                        .conEstado(o -> switch (o.estado()) {
                            case "CERRADA" -> Estado.OK;
                            case "ASIGNADA", "POR_VERIFICAR" -> Boolean.TRUE.equals(o.vencida()) ? Estado.MAL : Estado.ALERTA;
                            default -> null;
                        })),
                col("estacion", true, ColumnaExcel.texto("Estación", OrdenResumenResponse::estacionNombre)),
                col("disciplina", true, ColumnaExcel.texto("Disciplina", o -> disc.apply(o.disciplina()))),
                col("hallazgo", true, ColumnaExcel.texto("Hallazgo de origen", o -> o.hallazgoFecha() == null ? ""
                        : o.hallazgoFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " · " + etiqueta(o.hallazgoResultado()))),
                col("actividad", true, ColumnaExcel.texto("Actividad", OrdenResumenResponse::actividadNombre)),
                col("asignado", true, ColumnaExcel.texto("Asignado a",
                        o -> Boolean.TRUE.equals(o.paraCualquiera()) ? "Cualquier operario" : o.asignadoANombre())),
                col("creada", true, ColumnaExcel.fecha("Creada", OrdenResumenResponse::creadoEn)),
                col("diasAbierta", true, ColumnaExcel.numero("Días en curso", OrdenResumenResponse::diasAbierta)),
                col("prioridad", false, ColumnaExcel.texto("Prioridad", o -> etiqueta(o.prioridad()))),
                col("fechaLimite", false, ColumnaExcel.fecha("Fecha límite", OrdenResumenResponse::fechaLimite)),
                col("instrucciones", false, ColumnaExcel.texto("Instrucciones", OrdenResumenResponse::instrucciones)),
                col("creadoPor", false, ColumnaExcel.texto("Creada por", OrdenResumenResponse::creadoPor))),
                this::ordenes));
        m.put("AVANCE", new Hoja<FilaAvance>("AVANCE", "Avance por estación",
                List.of("disciplina", "anio", "estacionId"), List.of(
                col("estacion", true, ColumnaExcel.texto("Estación", (FilaAvance a) -> a.i().estacionNombre())),
                col("programado", true, ColumnaExcel.numero("Programadas", (FilaAvance a) -> a.i().programado())),
                col("cumple", true, ColumnaExcel.numero("Cumplidas", (FilaAvance a) -> a.i().cumple())),
                col("porcentaje", true, ColumnaExcel.porcentaje("% cumplimiento", (FilaAvance a) -> a.i().porcentajeCumplimiento())
                        .conEstado(a -> estadoPorcentaje(a.i().porcentajeCumplimiento()))),
                col("pendientes", true, ColumnaExcel.numero("Pendientes", (FilaAvance a) -> a.i().noCumple())),
                col("atrasadas", true, ColumnaExcel.numero("Atrasadas", (FilaAvance a) -> atrasadas(a.i()))),
                col("mantenimiento", true, ColumnaExcel.numero("Ejecutado mantenimiento", (FilaAvance a) -> a.i().ejecutadoMantenimiento())),
                col("inspeccion", true, ColumnaExcel.numero("Ejecutado inspección", (FilaAvance a) -> a.i().ejecutadoInspeccion())),
                col("total", true, ColumnaExcel.numero("Ejecutado total", (FilaAvance a) -> a.i().ejecutadoTotal())),
                col("criticidad", true, ColumnaExcel.texto("Criticidad", FilaAvance::criticidad)
                        .conEstado(a -> switch (a.criticidad()) {
                            case "Alto" -> Estado.MAL;
                            case "Medio" -> Estado.ALERTA;
                            default -> Estado.OK;
                        })),
                col("porcentajeIntervenciones", false, ColumnaExcel.porcentaje("% de las intervenciones", FilaAvance::participacion)),
                col("imprevistos", false, ColumnaExcel.numero("Imprevistos", (FilaAvance a) -> a.i().ejecutadoNoProgramado())),
                col("conHallazgos", false, ColumnaExcel.numero("Con hallazgos", (FilaAvance a) -> a.i().conHallazgos())),
                col("hallazgosAbiertos", false, ColumnaExcel.numero("Hallazgos abiertos", (FilaAvance a) -> a.i().hallazgosAbiertos()))),
                this::avance));
        m.put("ACTIVIDADES", new Hoja<ResumenActividadResponse>("ACTIVIDADES", "Resumen por actividad",
                List.of("disciplina", "anio"), List.of(
                col("actividad", true, ColumnaExcel.texto("Actividad", ResumenActividadResponse::actividadNombre)),
                col("disciplina", true, ColumnaExcel.texto("Disciplina", r -> disc.apply(r.disciplina()))),
                col("programado", true, ColumnaExcel.numero("Programado", ResumenActividadResponse::programadoAnual)),
                col("ejecutado", true, ColumnaExcel.numero("Ejecutado", ResumenActividadResponse::ejecutadoAnual)),
                col("noProgramado", true, ColumnaExcel.numero("No programado", ResumenActividadResponse::ejecutadoNoProgramado)),
                col("mantenimiento", true, ColumnaExcel.numero("Mantenimiento", ResumenActividadResponse::mantenimiento)),
                col("inspeccion", true, ColumnaExcel.numero("Inspección", ResumenActividadResponse::inspeccion)),
                col("total", true, ColumnaExcel.numero("Total", ResumenActividadResponse::ejecutadoTotal)),
                col("porcentaje", true, ColumnaExcel.porcentaje("% cumplimiento", ResumenActividadResponse::porcentajeCumplimiento)
                        .conEstado(r -> estadoPorcentaje(r.porcentajeCumplimiento()))),
                col("estaciones", false, ColumnaExcel.numero("Estaciones", ResumenActividadResponse::estaciones))),
                f -> indicadoresUseCase.resumenPorActividad(f.disciplina(), f.anio())));
        return m;
    }

    private static <T> Col<T> col(String clave, boolean porDefecto, ColumnaExcel<T> excel) {
        return new Col<>(clave, porDefecto, excel);
    }

    /** Lo que la web muestra en el panel de descarga: hojas, filtros y columnas. */
    public List<HojaExportacionResponse> listarHojas() {
        return hojas().values().stream()
                .map(h -> new HojaExportacionResponse(h.clave(), h.titulo(), h.filtros(),
                        h.columnas().stream()
                                .map(c -> new HojaExportacionResponse.Columna(c.clave(), c.excel().titulo(), c.porDefecto()))
                                .toList()))
                .toList();
    }

    // ---------------------------------------------------------------------
    // Generación
    // ---------------------------------------------------------------------

    /** Las hojas, en el orden del panel: clave → título. Fijas, para registrarlas en el motor al arrancar. */
    public static final List<Map.Entry<String, String>> HOJAS = List.of(
            Map.entry("EJECUCIONES", "Ejecuciones"),
            Map.entry("HALLAZGOS", "Hallazgos"),
            Map.entry("ORDENES", "Órdenes de trabajo"),
            Map.entry("AVANCE", "Avance por estación"),
            Map.entry("ACTIVIDADES", "Resumen por actividad"));

    private static final Map<String, FiltroReporte> FILTROS = Map.of(
            "disciplina", FiltroReporte.elegible("disciplina", "Disciplina", "DISCIPLINA"),
            "anio", FiltroReporte.elegible("anio", "Año", "ANIO"),
            "mes", FiltroReporte.elegible("mes", "Mes", "MES"),
            "estacionId", FiltroReporte.elegible("estacionId", "Estación", "ESTACION"));

    private static final Map<String, List<String>> FILTROS_POR_HOJA = Map.of(
            "EJECUCIONES", List.of("disciplina", "anio", "mes", "estacionId"),
            "HALLAZGOS", List.of("disciplina", "anio", "mes", "estacionId"),
            "ORDENES", List.of("disciplina", "anio", "mes", "estacionId"),
            "AVANCE", List.of("disciplina", "anio", "estacionId"),
            "ACTIVIDADES", List.of("disciplina", "anio"));

    /** Los filtros que usa la hoja (sin tocar la base: se piden al registrar el motor). */
    public List<FiltroReporte> filtrosDe(String clave) {
        return FILTROS_POR_HOJA.getOrDefault(clave, List.of()).stream().map(FILTROS::get).toList();
    }

    /** Las columnas elegibles de la hoja y cuáles salen marcadas. */
    public List<ColumnaReporte> columnasDe(String clave) {
        return hoja(clave).columnas().stream()
                .map(c -> new ColumnaReporte(c.clave(), c.excel().titulo(), c.porDefecto()))
                .toList();
    }

    /** Lo que se pidió, ya validado y con el año actual si no vino. */
    private Filtros filtrosPedidos(String clave, PeticionReporte p) {
        List<String> usa = FILTROS_POR_HOJA.getOrDefault(clave, List.of());
        Integer mes = usa.contains("mes") ? p.filtroEntero("mes") : null;
        if (mes != null && (mes < 1 || mes > 12)) {
            throw new BadRequestException("mes inválido: " + mes);
        }
        Integer anio = p.filtroEntero("anio");
        return new Filtros(p.filtro("disciplina"), anio != null ? anio : calendario.anioActual(), mes,
                usa.contains("estacionId") ? p.filtroLong("estacionId") : null);
    }

    /** El año (o el mes) pedido: es el periodo de la portada y del nombre del archivo. */
    public Periodo periodoDe(String clave, PeticionReporte p) {
        Filtros f = filtrosPedidos(clave, p);
        LocalDate desde = f.mes() == null ? LocalDate.of(f.anio(), 1, 1) : LocalDate.of(f.anio(), f.mes(), 1);
        LocalDate hasta = f.mes() == null ? LocalDate.of(f.anio(), 12, 31) : desde.withDayOfMonth(desde.lengthOfMonth());
        return new Periodo(desde, hasta);
    }

    /** "estaciones_ejecuciones_electrico" (el motor le agrega el periodo). */
    public String nombreArchivoDe(String clave, PeticionReporte p) {
        String disciplina = p.filtro("disciplina");
        return "estaciones_" + clave.toLowerCase() + (disciplina == null ? "" : "_" + disciplina.toLowerCase());
    }

    /** Llena el libro con la hoja pedida. */
    @Transactional(readOnly = true)
    public void llenar(String clave, LibroExcel libro, PeticionReporte p) {
        llenar(hoja(clave), libro, filtrosPedidos(clave, p), p.columnas());
    }

    private Hoja<?> hoja(String clave) {
        Hoja<?> hoja = hojas().get(clave == null ? "" : clave.toUpperCase());
        if (hoja == null) {
            throw new BadRequestException("Hoja desconocida: " + clave + ". Opciones: " + String.join(", ", FILTROS_POR_HOJA.keySet()));
        }
        return hoja;
    }

    private <T> void llenar(Hoja<T> hoja, LibroExcel libro, Filtros f, List<String> claves) {
        List<Col<T>> elegidas = claves == null || claves.isEmpty()
                ? hoja.columnas().stream().filter(Col::porDefecto).toList()
                // En el orden de la hoja, no en el que llegaron; las claves desconocidas se ignoran.
                : hoja.columnas().stream().filter(c -> claves.contains(c.clave())).toList();
        if (elegidas.isEmpty()) {
            throw new BadRequestException("Elija al menos una columna de " + hoja.titulo());
        }
        List<T> filas = hoja.filas().apply(f);
        String disciplina = f.disciplina() == null ? null : nombresDisciplina().getOrDefault(f.disciplina(), f.disciplina());
        libro.filtro("Disciplina", disciplina == null ? "Todas las disciplinas" : disciplina);
        if (hoja.filtros().contains("estacionId")) {
            libro.filtro("Estación", f.estacionId() == null ? "Todas las estaciones"
                    : estacionRepository.findById(f.estacionId()).map(e -> e.getNombre()).orElse("#" + f.estacionId()));
        }
        libro.cifra("Registros", filas.size());
        libro.tabla(hoja.titulo(), hoja.titulo() + " · " + (disciplina == null ? "todas las disciplinas" : disciplina),
                elegidas.stream().map(Col::excel).toList(), filas);
    }

    // ---------------------------------------------------------------------
    // Filas (las mismas consultas de la web)
    // ---------------------------------------------------------------------

    private List<EjecucionResponse> ejecuciones(Filtros f, List<String> resultados) {
        LocalDate desde = f.mes() == null ? LocalDate.of(f.anio(), 1, 1) : LocalDate.of(f.anio(), f.mes(), 1);
        LocalDate hasta = f.mes() == null ? LocalDate.of(f.anio(), 12, 31) : desde.withDayOfMonth(desde.lengthOfMonth());
        return todasLasPaginas(p -> ejecucionUseCase.listarEjecuciones(f.estacionId(), desde, hasta, null, resultados,
                null, null, null, null, f.disciplina(), null, null,
                PageRequest.of(p, PAGINA, Sort.by(Sort.Order.desc("fecha")))));
    }

    private List<OrdenResumenResponse> ordenes(Filtros f) {
        return todasLasPaginas(p -> ordenUseCase.listar(null, f.estacionId(), null, null, f.disciplina(),
                PageRequest.of(p, PAGINA, Sort.by(Sort.Order.desc("creadoEn")))))
                .stream()
                .filter(o -> o.creadoEn() == null || (o.creadoEn().getYear() == f.anio()
                        && (f.mes() == null || o.creadoEn().getMonthValue() == f.mes())))
                .toList();
    }

    /** Una estación con su participación en las intervenciones del año y su nivel de criticidad. */
    record FilaAvance(IndicadorEstacionResponse i, Double participacion, String criticidad) {
    }

    private List<FilaAvance> avance(Filtros f) {
        List<IndicadorEstacionResponse> todas = indicadoresUseCase.indicadoresPorEstacion(f.anio(), f.disciplina());
        int total = todas.stream().mapToInt(i -> valor(i.ejecutadoTotal())).sum();
        return todas.stream()
                .filter(i -> f.estacionId() == null || f.estacionId().equals(i.estacionId()))
                .map(i -> {
                    Double part = total == 0 ? null : valor(i.ejecutadoTotal()) * 100.0 / total;
                    return new FilaAvance(i, part, nivelCriticidad(part));
                })
                .toList();
    }

    static String nivelCriticidad(Double participacion) {
        if (participacion == null) {
            return "Bajo";
        }
        return participacion >= CRITICIDAD_ALTA ? "Alto" : participacion >= CRITICIDAD_MEDIA ? "Medio" : "Bajo";
    }

    private static <T> List<T> todasLasPaginas(Function<Integer, Page<T>> pagina) {
        List<T> todo = new ArrayList<>();
        Page<T> p;
        int n = 0;
        do {
            p = pagina.apply(n++);
            todo.addAll(p.getContent());
        } while (p.hasNext());
        return todo;
    }

    // ---------------------------------------------------------------------
    // Valores y colores
    // ---------------------------------------------------------------------

    private Map<String, String> nombresDisciplina() {
        return disciplinaRepository.findAll().stream()
                .collect(Collectors.toMap(DisciplinaEntity::getCodigo,
                        d -> d.getNombre() != null ? d.getNombre() : d.getCodigo(), (a, b) -> a));
    }

    static String etiqueta(String codigo) {
        return codigo == null ? "" : ETIQUETAS.getOrDefault(codigo, codigo);
    }

    private static String origen(EjecucionResponse e) {
        if (e.orden() != null) {
            return "Orden " + e.orden().consecutivo();
        }
        return Boolean.TRUE.equals(e.esProgramada()) ? "Cronograma" : "Imprevisto";
    }

    private static String actividad(EjecucionResponse e) {
        return e.actividadNombre() != null ? e.actividadNombre() : Objects.toString(e.descripcionLibre(), "");
    }

    private static String conjuntos(EjecucionResponse e) {
        List<String> nombres = new ArrayList<>(e.conjuntos() == null ? List.of()
                : e.conjuntos().stream().map(ItemRefResponse::nombre).toList());
        if (e.conjuntoOtro() != null) {
            nombres.add("Otro: " + e.conjuntoOtro());
        }
        return String.join(", ", nombres);
    }

    /** "Transformador › Estado físico (con óxido), Subestación › Fusibles". */
    private static String elementos(EjecucionResponse e) {
        List<String> nombres = new ArrayList<>(e.elementos() == null ? List.of() : e.elementos().stream()
                .map(ExportacionEstacionesService::elemento).toList());
        if (e.elementoOtro() != null) {
            nombres.add("Otro: " + e.elementoOtro());
        }
        return String.join(", ", nombres);
    }

    private static String elemento(ElementoMarcadoResponse m) {
        return (m.conjuntoNombre() != null ? m.conjuntoNombre() + " › " : "") + m.nombre()
                + (m.detalle() != null ? " (" + m.detalle() + ")" : "");
    }

    private static Long diasAbierto(EjecucionResponse e) {
        if (e.fecha() == null) {
            return null;
        }
        LocalDate fin = e.seguimiento() != null && e.seguimiento().cerradoEn() != null
                ? e.seguimiento().cerradoEn().toLocalDate() : LocalDate.now();
        return Math.max(0, ChronoUnit.DAYS.between(e.fecha(), fin));
    }

    private static int atrasadas(IndicadorEstacionResponse i) {
        return Math.max(0, valor(i.vencidas()) - valor(i.ejecutadasVencidas()));
    }

    private static int valor(Integer n) {
        return n == null ? 0 : n;
    }

    private static Estado estadoResultado(String resultado) {
        if (resultado == null) {
            return null;
        }
        return switch (resultado) {
            case "CONFORME" -> Estado.OK;
            case "CON_HALLAZGOS" -> Estado.ALERTA;
            case "REQUIERE_INTERVENCION" -> Estado.MAL;
            default -> null;
        };
    }

    private static Estado estadoSeguimiento(EjecucionResponse e) {
        if (e.seguimiento() == null) {
            return null;
        }
        return switch (e.seguimiento().estado()) {
            case "RESUELTO" -> Estado.OK;
            case "EN_PROCESO" -> Estado.ALERTA;
            case "ABIERTO" -> Estado.MAL;
            default -> null;
        };
    }

    /** Verde desde 90 %, amarillo desde 70 %, rojo por debajo; sin color si no hay citas vencidas. */
    static Estado estadoPorcentaje(BigDecimal pct) {
        if (pct == null) {
            return null;
        }
        double v = pct.doubleValue();
        return v >= 90 ? Estado.OK : v >= 70 ? Estado.ALERTA : Estado.MAL;
    }

}
