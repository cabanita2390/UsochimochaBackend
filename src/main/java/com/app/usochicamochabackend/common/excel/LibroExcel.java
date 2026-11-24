package com.app.usochicamochabackend.common.excel;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Estilo común de todas las descargas a Excel del sistema.
 *
 * <ul>
 *   <li><b>Portada</b> (primera hoja, "Resumen"): franja con el nombre del sistema, título del
 *   reporte, cuándo y quién lo generó, el periodo, los filtros, las cifras principales y un índice
 *   con un enlace a cada hoja y cuántos registros trae.</li>
 *   <li><b>Hojas</b>: título y periodo arriba, encabezado de color, fila congelada, filtro de Excel,
 *   filas alternadas, fechas dd/mm/aaaa, números a la derecha, colores de estado de la web, fila de
 *   totales cuando aplica, y lista para imprimir (horizontal, a lo ancho de la página, encabezado
 *   repetido y número de página).</li>
 * </ul>
 * Escribe por partes ({@link SXSSFWorkbook}) para no llenar la memoria con muchas filas.
 *
 * <pre>
 * try (LibroExcel libro = new LibroExcel("Inspecciones de vehículos", usuario, periodo)) {
 *     libro.filtro("Tipo", "Vehículos").cifra("Inspecciones", 120);
 *     libro.tabla("Inspecciones", "Inspecciones pre-operativas", columnas, filas);
 *     return libro.bytes();
 * }
 * </pre>
 */
public final class LibroExcel implements AutoCloseable {

    public static final String SISTEMA = "USOCHICAMOCHA · Distrito de Riego · Sistema de gestión";
    public static final String TIPO_CONTENIDO = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final byte[] MORADO = rgb(0x5B, 0x3F, 0xA0);
    private static final byte[] MORADO_SUAVE = rgb(0xEF, 0xEA, 0xF8);
    private static final byte[] ALTERNA = rgb(0xF8, 0xF6, 0xFC);
    private static final byte[] GRIS = rgb(0x6B, 0x6A, 0x66);
    private static final byte[] BORDE = rgb(0xE2, 0xDD, 0xEC);
    private static final byte[] BLANCO = rgb(0xFF, 0xFF, 0xFF);
    private static final Map<ColumnaExcel.Estado, byte[][]> COLORES_ESTADO = Map.of(
            ColumnaExcel.Estado.OK, new byte[][]{rgb(0xE6, 0xF2, 0xE6), rgb(0x00, 0x63, 0x00)},
            ColumnaExcel.Estado.ALERTA, new byte[][]{rgb(0xFD, 0xF3, 0xD8), rgb(0x8A, 0x5A, 0x00)},
            ColumnaExcel.Estado.MAL, new byte[][]{rgb(0xFB, 0xE7, 0xE7), rgb(0xB0, 0x2A, 0x2A)});
    private static final int ANCHO_MAXIMO = 55;
    private static final int ANCHO_MINIMO = 9;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SXSSFWorkbook libro = new SXSSFWorkbook(200);
    private final SXSSFSheet portada;
    private String titulo;
    private final String usuario;
    private final Periodo periodo;
    private final List<String[]> filtros = new ArrayList<>();
    private final List<Object[]> cifras = new ArrayList<>();
    private final List<String> notas = new ArrayList<>();
    private final List<Object[]> indice = new ArrayList<>();
    private final Set<String> nombresUsados = new HashSet<>();

    private final CellStyle sFranja;
    private final CellStyle sTitulo;
    private final CellStyle sSubtitulo;
    private final CellStyle sSeccion;
    private final CellStyle sEtiqueta;
    private final CellStyle sValor;
    private final CellStyle sCifra;
    private final CellStyle sEnlace;
    private final CellStyle sTituloHoja;
    private final CellStyle sEncabezado;
    private final CellStyle sNota;
    private final Map<ColumnaExcel.Tipo, CellStyle[]> porTipo = new EnumMap<>(ColumnaExcel.Tipo.class);
    private final Map<ColumnaExcel.Tipo, CellStyle> totales = new EnumMap<>(ColumnaExcel.Tipo.class);
    private final Map<ColumnaExcel.Tipo, Map<ColumnaExcel.Estado, CellStyle>> porEstado = new EnumMap<>(ColumnaExcel.Tipo.class);

    /**
     * @param titulo  nombre del reporte ("Inspecciones de vehículos").
     * @param usuario quién lo descarga (sale en la portada).
     * @param periodo periodo pedido; {@link Periodo#todo()} si el reporte no filtra por fechas.
     */
    public LibroExcel(String titulo, String usuario, Periodo periodo) {
        this.titulo = titulo;
        this.usuario = usuario == null || usuario.isBlank() ? "—" : usuario;
        this.periodo = periodo == null ? Periodo.todo() : periodo;

        sFranja = estilo(MORADO, fuente(true, 10, BLANCO, false), HorizontalAlignment.LEFT);
        sTitulo = estilo(null, fuente(true, 20, rgb(0x1F, 0x1E, 0x1C), false), HorizontalAlignment.LEFT);
        sSubtitulo = estilo(null, fuente(false, 10, GRIS, false), HorizontalAlignment.LEFT);
        sSeccion = estilo(MORADO_SUAVE, fuente(true, 11, MORADO, false), HorizontalAlignment.LEFT);
        sEtiqueta = conBorde(estilo(null, fuente(true, 10, GRIS, false), HorizontalAlignment.LEFT));
        sValor = conBorde(estilo(null, fuente(false, 10, null, false), HorizontalAlignment.LEFT));
        sValor.setWrapText(true);
        sCifra = conBorde(estilo(null, fuente(true, 14, MORADO, false), HorizontalAlignment.LEFT));
        sEnlace = conBorde(estilo(null, fuente(false, 10, rgb(0x2A, 0x5B, 0xD6), true), HorizontalAlignment.LEFT));
        sTituloHoja = estilo(null, fuente(true, 14, rgb(0x1F, 0x1E, 0x1C), false), HorizontalAlignment.LEFT);
        sNota = estilo(null, fuente(false, 9, GRIS, false), HorizontalAlignment.LEFT);
        sNota.setWrapText(true);

        XSSFCellStyle enc = (XSSFCellStyle) estilo(MORADO, fuente(true, 10, BLANCO, false), HorizontalAlignment.CENTER);
        enc.setVerticalAlignment(VerticalAlignment.CENTER);
        enc.setWrapText(true);
        enc.setBorderRight(BorderStyle.THIN);
        enc.setRightBorderColor(new XSSFColor(rgb(0x7B, 0x62, 0xB8), null));
        sEncabezado = enc;

        for (ColumnaExcel.Tipo tipo : ColumnaExcel.Tipo.values()) {
            porTipo.put(tipo, new CellStyle[]{celda(tipo, null, null, false), celda(tipo, ALTERNA, null, false)});
            totales.put(tipo, celda(tipo, MORADO_SUAVE, MORADO, true));
            Map<ColumnaExcel.Estado, CellStyle> estados = new EnumMap<>(ColumnaExcel.Estado.class);
            COLORES_ESTADO.forEach((estado, c) -> estados.put(estado, celda(tipo, c[0], c[1], false)));
            porEstado.put(tipo, estados);
        }
        portada = libro.createSheet("Resumen");
        nombresUsados.add("resumen");
    }

    // ---------------------------------------------------------------------
    // Portada
    // ---------------------------------------------------------------------

    /**
     * Cambia el título del libro cuando depende de los datos ("Historial de rendimiento · ABC-123").
     * Llamarlo antes de crear la primera hoja: cada hoja lo repite en su subtítulo.
     */
    public LibroExcel titulo(String nuevo) {
        this.titulo = nuevo;
        return this;
    }

    /** Un filtro usado ("Disciplina" → "Eléctrico"). Sale en la portada. */
    public LibroExcel filtro(String etiqueta, Object valor) {
        filtros.add(new String[]{etiqueta, valor == null || valor.toString().isBlank() ? "Todos" : valor.toString()});
        return this;
    }

    /** Una cifra principal ("Inspecciones" → 120). Sale grande en la portada. */
    public LibroExcel cifra(String etiqueta, Object valor) {
        cifras.add(new Object[]{etiqueta, valor});
        return this;
    }

    /** Una aclaración al pie de la portada ("Los imprevistos no suman al avance"). */
    public LibroExcel nota(String texto) {
        notas.add(texto);
        return this;
    }

    // ---------------------------------------------------------------------
    // Hojas
    // ---------------------------------------------------------------------

    /** Una hoja con una sola tabla: título y periodo arriba, filtro de Excel y totales si aplican. */
    public <T> int tabla(String nombreHoja, String tituloTabla, List<ColumnaExcel<T>> columnas, Iterable<T> filas) {
        Hoja h = hoja(nombreHoja, tituloTabla, tituloTabla);
        int n = h.tabla(null, columnas, filas, true);
        h.terminar(columnas.size());
        return n;
    }

    /**
     * Una hoja armada por partes (fichas y varias tablas), p. ej. la hoja de vida de una máquina:
     * sus datos y luego sus inspecciones. Llamar {@link Hoja#terminar(int)} al final.
     */
    public Hoja hoja(String nombreHoja, String tituloHoja, String descripcion) {
        String nombre = nombreUnico(nombreHoja);
        SXSSFSheet s = libro.createSheet(nombre);
        return new Hoja(s, nombre, tituloHoja, descripcion);
    }

    /** Una hoja en construcción. */
    public final class Hoja {
        private final SXSSFSheet s;
        private final String nombre;
        private final String descripcion;
        private int fila;
        private int registros;
        private int[] anchos = new int[0];
        private int tablas;
        private int filaEncabezado = -1;

        private Hoja(SXSSFSheet s, String nombre, String tituloHoja, String descripcion) {
            this.s = s;
            this.nombre = nombre;
            this.descripcion = descripcion;
            Row t = s.createRow(0);
            t.setHeightInPoints(24);
            celda(t, 0, tituloHoja, sTituloHoja);
            Row p = s.createRow(1);
            celda(p, 0, periodo.texto() + "  ·  " + titulo, sSubtitulo);
            fila = 3;
        }

        /** Bloque de datos etiqueta / valor (la ficha de una máquina). */
        public Hoja ficha(String tituloBloque, List<Map.Entry<String, Object>> datos) {
            if (tituloBloque != null) {
                Row r = s.createRow(fila++);
                celda(r, 0, tituloBloque, sSeccion);
                celda(r, 1, "", sSeccion);
                celda(r, 2, "", sSeccion);
                celda(r, 3, "", sSeccion);
            }
            for (int i = 0; i < datos.size(); i += 2) {
                Row r = s.createRow(fila++);
                escribirPar(r, 0, datos.get(i));
                if (i + 1 < datos.size()) {
                    escribirPar(r, 2, datos.get(i + 1));
                }
            }
            ancho(0, 22);
            ancho(1, 30);
            ancho(2, 22);
            ancho(3, 30);
            fila++;
            return this;
        }

        private void escribirPar(Row r, int col, Map.Entry<String, Object> par) {
            celda(r, col, par.getKey(), sEtiqueta);
            Object v = par.getValue();
            String texto = v == null ? "—" : v instanceof LocalDate d ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : v instanceof LocalDateTime dt ? dt.format(FECHA_HORA) : v.toString();
            celda(r, col + 1, texto.isBlank() ? "—" : texto, sValor);
        }

        /** Una tabla dentro de la hoja; devuelve cuántas filas escribió. */
        public <T> int tabla(String tituloTabla, List<ColumnaExcel<T>> columnas, Iterable<T> filas) {
            return tabla(tituloTabla, columnas, filas, false);
        }

        private <T> int tabla(String tituloTabla, List<ColumnaExcel<T>> columnas, Iterable<T> filas, boolean unica) {
            tablas++;
            if (tituloTabla != null) {
                Row r = s.createRow(fila++);
                celda(r, 0, tituloTabla, sSeccion);
                for (int i = 1; i < Math.max(4, columnas.size()); i++) {
                    celda(r, i, "", sSeccion);
                }
            }
            int enc = fila++;
            Row e = s.createRow(enc);
            e.setHeightInPoints(32);
            for (int i = 0; i < columnas.size(); i++) {
                celda(e, i, columnas.get(i).titulo(), sEncabezado);
                ancho(i, columnas.get(i).titulo().length() + 3);
            }
            double[] sumas = new double[columnas.size()];
            int n = 0;
            for (T f : filas) {
                Row r = s.createRow(fila++);
                boolean alterna = ++n % 2 == 0;
                for (int i = 0; i < columnas.size(); i++) {
                    ColumnaExcel<T> col = columnas.get(i);
                    Cell c = r.createCell(i);
                    Object v = col.valor().apply(f);
                    ancho(i, escribir(c, col.tipo(), v) + 2);
                    if (col.total() && v instanceof Number num) {
                        sumas[i] += num.doubleValue();
                    }
                    ColumnaExcel.Estado est = col.estado() == null ? null : col.estado().apply(f);
                    c.setCellStyle(est != null ? porEstado.get(col.tipo()).get(est) : porTipo.get(col.tipo())[alterna ? 1 : 0]);
                }
            }
            if (n == 0) {
                Row r = s.createRow(fila++);
                celda(r, 0, "Sin registros para el periodo y los filtros elegidos.", sNota);
            } else if (columnas.stream().anyMatch(ColumnaExcel::total)) {
                Row r = s.createRow(fila++);
                for (int i = 0; i < columnas.size(); i++) {
                    ColumnaExcel<T> col = columnas.get(i);
                    Cell c = r.createCell(i);
                    if (i == 0) {
                        c.setCellValue("Total");
                        c.setCellStyle(totales.get(ColumnaExcel.Tipo.TEXTO));
                    } else if (col.total()) {
                        c.setCellValue(sumas[i]);
                        c.setCellStyle(totales.get(col.tipo()));
                    } else {
                        c.setCellStyle(totales.get(ColumnaExcel.Tipo.TEXTO));
                    }
                }
            }
            if (unica && n > 0) {
                s.setAutoFilter(new CellRangeAddress(enc, enc + n, 0, columnas.size() - 1));
            }
            if (filaEncabezado < 0) {
                filaEncabezado = enc;
            }
            registros += n;
            fila++;
            return n;
        }

        /** Cierra la hoja: anchos, fila congelada (si tiene una sola tabla) e impresión. */
        public void terminar(int columnasMinimas) {
            for (int i = 0; i < anchos.length; i++) {
                s.setColumnWidth(i, Math.max(ANCHO_MINIMO, Math.min(ANCHO_MAXIMO, anchos[i])) * 256);
            }
            int ultima = Math.max(columnasMinimas, anchos.length) - 1;
            if (ultima > 0) {
                s.addMergedRegion(new CellRangeAddress(0, 0, 0, Math.min(ultima, 8)));
                s.addMergedRegion(new CellRangeAddress(1, 1, 0, Math.min(ultima, 8)));
            }
            if (tablas == 1 && filaEncabezado >= 0) {
                s.createFreezePane(0, filaEncabezado + 1);
                s.setRepeatingRows(new CellRangeAddress(filaEncabezado, filaEncabezado, -1, -1));
            }
            imprimir(s);
            indice.add(new Object[]{nombre, descripcion, registros});
        }

        private void ancho(int col, int valor) {
            if (col >= anchos.length) {
                anchos = java.util.Arrays.copyOf(anchos, col + 1);
            }
            anchos[col] = Math.max(anchos[col], valor);
        }
    }

    // ---------------------------------------------------------------------
    // Escritura
    // ---------------------------------------------------------------------

    public byte[] bytes() throws IOException {
        escribirPortada();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            libro.write(out);
            return out.toByteArray();
        }
    }

    private void escribirPortada() {
        int f = 0;
        Row franja = portada.createRow(f++);
        franja.setHeightInPoints(20);
        for (int i = 0; i < 4; i++) {
            celda(franja, i, i == 0 ? SISTEMA : "", sFranja);
        }
        portada.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        f++;
        Row t = portada.createRow(f++);
        t.setHeightInPoints(30);
        celda(t, 0, titulo, sTitulo);
        portada.addMergedRegion(new CellRangeAddress(t.getRowNum(), t.getRowNum(), 0, 3));
        Row sub = portada.createRow(f++);
        celda(sub, 0, "Generado el " + LocalDateTime.now().format(FECHA_HORA) + " por " + usuario, sSubtitulo);
        portada.addMergedRegion(new CellRangeAddress(sub.getRowNum(), sub.getRowNum(), 0, 3));
        f++;

        f = seccion(f, "Periodo y filtros");
        f = par(f, "Periodo", periodo.texto(), sValor);
        for (String[] fl : filtros) {
            f = par(f, fl[0], fl[1], sValor);
        }
        f++;
        if (!cifras.isEmpty()) {
            f = seccion(f, "Cifras principales");
            for (Object[] c : cifras) {
                f = par(f, (String) c[0], formatoCifra(c[1]), sCifra);
            }
            f++;
        }
        f = seccion(f, "Contenido");
        Row ce = portada.createRow(f++);
        celda(ce, 0, "Hoja", sEtiqueta);
        celda(ce, 1, "Qué trae", sEtiqueta);
        celda(ce, 2, "Registros", sEtiqueta);
        for (Object[] h : indice) {
            Row r = portada.createRow(f++);
            Cell c = r.createCell(0);
            c.setCellValue((String) h[0]);
            Hyperlink link = libro.getCreationHelper().createHyperlink(HyperlinkType.DOCUMENT);
            link.setAddress("'" + ((String) h[0]).replace("'", "''") + "'!A1");
            c.setHyperlink(link);
            c.setCellStyle(sEnlace);
            celda(r, 1, h[1] == null ? "" : (String) h[1], sValor);
            Cell n = r.createCell(2);
            n.setCellValue(((Number) h[2]).doubleValue());
            n.setCellStyle(porTipo.get(ColumnaExcel.Tipo.NUMERO)[0]);
        }
        if (!notas.isEmpty()) {
            f++;
            for (String nota : notas) {
                Row r = portada.createRow(f++);
                celda(r, 0, nota, sNota);
                portada.addMergedRegion(new CellRangeAddress(r.getRowNum(), r.getRowNum(), 0, 3));
            }
        }
        portada.setColumnWidth(0, 26 * 256);
        portada.setColumnWidth(1, 52 * 256);
        portada.setColumnWidth(2, 14 * 256);
        portada.setColumnWidth(3, 14 * 256);
        portada.setDisplayGridlines(false);
        imprimir(portada);
        portada.getPrintSetup().setLandscape(false);
    }

    private int seccion(int f, String texto) {
        Row r = portada.createRow(f);
        for (int i = 0; i < 4; i++) {
            celda(r, i, i == 0 ? texto : "", sSeccion);
        }
        return f + 1;
    }

    private int par(int f, String etiqueta, String valor, CellStyle estiloValor) {
        Row r = portada.createRow(f);
        celda(r, 0, etiqueta, sEtiqueta);
        celda(r, 1, valor, estiloValor);
        return f + 1;
    }

    private static String formatoCifra(Object v) {
        if (v == null) {
            return "—";
        }
        if (v instanceof Double d) {
            return String.format(java.util.Locale.forLanguageTag("es-CO"), "%,.2f", d);
        }
        if (v instanceof Number n) {
            return String.format(java.util.Locale.forLanguageTag("es-CO"), "%,d", n.longValue());
        }
        return v.toString();
    }

    private void imprimir(SXSSFSheet s) {
        PrintSetup ps = s.getPrintSetup();
        ps.setLandscape(true);
        ps.setPaperSize(PrintSetup.LETTER_PAPERSIZE);
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);
        s.setFitToPage(true);
        s.setHorizontallyCenter(true);
        s.getFooter().setLeft(titulo);
        s.getFooter().setRight("Página &P de &N");
    }

    private String nombreUnico(String nombre) {
        // Excel no acepta \ / ? * [ ] : ni una comilla simple al inicio o al final.
        String base = nombre == null ? "" : nombre.replaceAll("[\\\\/?*\\[\\]:]", " ").trim()
                .replaceAll("^'+|'+$", "").trim();
        if (base.isEmpty()) {
            base = "Hoja";
        }
        base = base.length() > 31 ? base.substring(0, 31) : base;
        String n = base;
        int i = 2;
        while (!nombresUsados.add(n.toLowerCase())) {
            String suf = " (" + i++ + ")";
            n = (base.length() + suf.length() > 31 ? base.substring(0, 31 - suf.length()) : base) + suf;
        }
        return n;
    }

    /** Escribe el valor con su tipo; devuelve cuántos caracteres ocupa (para el ancho). */
    private static int escribir(Cell c, ColumnaExcel.Tipo tipo, Object v) {
        if (v == null || (v instanceof String s && s.isBlank())) {
            return 0;
        }
        switch (tipo) {
            case FECHA, FECHA_HORA -> {
                if (v instanceof LocalDate d) {
                    c.setCellValue(d);
                } else if (v instanceof LocalDateTime dt) {
                    if (tipo == ColumnaExcel.Tipo.FECHA) {
                        c.setCellValue(dt.toLocalDate());
                    } else {
                        c.setCellValue(dt);
                    }
                } else {
                    c.setCellValue(v.toString());
                }
                return tipo == ColumnaExcel.Tipo.FECHA ? 11 : 17;
            }
            case PORCENTAJE -> {
                if (!(v instanceof Number num)) {
                    c.setCellValue(v.toString());
                    return v.toString().length();
                }
                c.setCellValue(num.doubleValue() / 100.0);
                return 8;
            }
            case NUMERO, DECIMAL, MONEDA -> {
                if (!(v instanceof Number num)) {
                    c.setCellValue(v.toString());
                    return v.toString().length();
                }
                double d = v instanceof BigDecimal b ? b.doubleValue() : num.doubleValue();
                c.setCellValue(d);
                return String.format("%,.2f", d).length() + (tipo == ColumnaExcel.Tipo.MONEDA ? 2 : 0);
            }
            default -> {
                String s = v.toString();
                c.setCellValue(s);
                int largo = 0;
                for (String linea : s.split("\n")) {
                    largo = Math.max(largo, linea.length());
                }
                return largo;
            }
        }
    }

    // ---------------------------------------------------------------------
    // Estilos
    // ---------------------------------------------------------------------

    private CellStyle celda(ColumnaExcel.Tipo tipo, byte[] fondo, byte[] texto, boolean negrita) {
        XSSFCellStyle s = (XSSFCellStyle) libro.createCellStyle();
        s.setVerticalAlignment(VerticalAlignment.TOP);
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBottomBorderColor(new XSSFColor(BORDE, null));
        if (negrita) {
            s.setBorderTop(BorderStyle.THIN);
            s.setTopBorderColor(new XSSFColor(MORADO, null));
        }
        if (fondo != null) {
            s.setFillForegroundColor(new XSSFColor(fondo, null));
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if (texto != null || negrita) {
            s.setFont(fuente(true, 10, texto, false));
        }
        s.setDataFormat(libro.createDataFormat().getFormat(switch (tipo) {
            case FECHA -> "dd/mm/yyyy";
            case FECHA_HORA -> "dd/mm/yyyy hh:mm";
            case PORCENTAJE -> "0.0%";
            case NUMERO -> "#,##0";
            case DECIMAL -> "#,##0.00";
            case MONEDA -> "\"$\" #,##0";
            case TEXTO -> "@";
        }));
        s.setAlignment(tipo == ColumnaExcel.Tipo.TEXTO ? HorizontalAlignment.LEFT
                : tipo == ColumnaExcel.Tipo.FECHA || tipo == ColumnaExcel.Tipo.FECHA_HORA ? HorizontalAlignment.CENTER
                : HorizontalAlignment.RIGHT);
        s.setWrapText(tipo == ColumnaExcel.Tipo.TEXTO);
        return s;
    }

    private CellStyle estilo(byte[] fondo, Font fuente, HorizontalAlignment alineacion) {
        XSSFCellStyle s = (XSSFCellStyle) libro.createCellStyle();
        if (fondo != null) {
            s.setFillForegroundColor(new XSSFColor(fondo, null));
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        s.setFont(fuente);
        s.setAlignment(alineacion);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle conBorde(CellStyle s) {
        s.setBorderBottom(BorderStyle.HAIR);
        ((XSSFCellStyle) s).setBottomBorderColor(new XSSFColor(BORDE, null));
        return s;
    }

    private Font fuente(boolean negrita, int tamano, byte[] color, boolean subrayada) {
        XSSFFont f = (XSSFFont) libro.createFont();
        f.setBold(negrita);
        f.setFontHeightInPoints((short) tamano);
        if (color != null) {
            f.setColor(new XSSFColor(color, null));
        }
        if (subrayada) {
            f.setUnderline(Font.U_SINGLE);
        }
        return f;
    }

    private static void celda(Row r, int col, String valor, CellStyle estilo) {
        Cell c = r.createCell(col);
        c.setCellValue(valor);
        c.setCellStyle(estilo);
    }

    @Override
    public void close() {
        libro.dispose();
        try {
            libro.close();
        } catch (IOException ignored) {
            // nada que liberar más allá de los temporales, ya borrados con dispose()
        }
    }

    private static byte[] rgb(int r, int g, int b) {
        return new byte[]{(byte) r, (byte) g, (byte) b};
    }
}
