package com.app.usochicamochabackend.common.excel;

import com.app.usochicamochabackend.exception.BadRequestException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Estilo común de las descargas: portada, hojas, totales, periodo y nombres de hoja. */
class LibroExcelTest {

    record Fila(LocalDateTime fecha, String maquina, String estado, BigDecimal valor, Integer horas) {
    }

    private static final List<ColumnaExcel<Fila>> COLUMNAS = List.of(
            ColumnaExcel.fechaHora("Fecha", Fila::fecha),
            ColumnaExcel.texto("Máquina", Fila::maquina),
            ColumnaExcel.texto("Estado", (Fila f) -> f.estado())
                    .conEstado(f -> "Malo".equals(f.estado()) ? ColumnaExcel.Estado.MAL : ColumnaExcel.Estado.OK),
            ColumnaExcel.moneda("Valor", Fila::valor).conTotal(),
            ColumnaExcel.numero("Horas", Fila::horas).conTotal());

    private static List<Fila> filas() {
        return List.of(
                new Fila(LocalDateTime.of(2026, 3, 2, 8, 30), "Retroexcavadora CAT 1", "Bueno", new BigDecimal("150000"), 3),
                new Fila(LocalDateTime.of(2026, 3, 9, 14, 0), "Pala 105", "Malo", new BigDecimal("420000"), 5));
    }

    private static List<List<String>> leer(Sheet s) {
        List<List<String>> filas = new ArrayList<>();
        for (Row r : s) {
            List<String> celdas = new ArrayList<>();
            r.forEach(c -> celdas.add(c.toString()));
            filas.add(celdas);
        }
        return filas;
    }

    @Test
    void portadaConPeriodoFiltrosCifrasEIndiceYHojaConTotales() throws Exception {
        byte[] xlsx;
        try (LibroExcel libro = new LibroExcel("Hojas de vida · Maquinaria", "admin",
                new Periodo(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)))) {
            libro.filtro("Área", "Distrito").cifra("Máquinas", 2).cifra("Valor total", "$ 570.000").nota("Una nota al pie.");
            libro.tabla("Trabajos", "Trabajos realizados", COLUMNAS, filas());
            LibroExcel.Hoja h = libro.hoja("Pala 105", "Hoja de vida · Pala 105", "Ficha y trabajos");
            h.ficha("Datos de la máquina", List.of(Map.entry("Equipo", "Pala 105"), Map.entry("Modelo", "2019")));
            h.tabla("Trabajos realizados", COLUMNAS, filas().subList(1, 2));
            h.terminar(5);
            xlsx = libro.bytes();
        }
        String muestra = System.getProperty("excel.muestra");
        if (muestra != null) {
            Files.write(Path.of(muestra, "muestra_estilo_comun.xlsx"), xlsx);
        }

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            assertEquals(List.of("Resumen", "Trabajos", "Pala 105"),
                    List.of(wb.getSheetName(0), wb.getSheetName(1), wb.getSheetName(2)));
            List<List<String>> portada = leer(wb.getSheet("Resumen"));
            String todo = portada.toString();
            assertTrue(todo.contains(LibroExcel.SISTEMA));
            assertTrue(todo.contains("Hojas de vida · Maquinaria"));
            assertTrue(todo.contains("por admin"));
            assertTrue(todo.contains("[Periodo, 1 ene 2026 – 31 mar 2026]"), todo);
            assertTrue(todo.contains("[Área, Distrito]"));
            assertTrue(todo.contains("[Máquinas, 2]"));
            assertTrue(todo.contains("[Trabajos, Trabajos realizados"), "Índice con la hoja");
            Row enIndice = null;
            for (Row r : wb.getSheet("Resumen")) {
                if (r.getCell(0) != null && "Trabajos".equals(r.getCell(0).toString())) {
                    enIndice = r;
                }
            }
            assertNotNull(enIndice);
            assertNotNull(enIndice.getCell(0).getHyperlink(), "La hoja del índice es un enlace");
            assertEquals("'Trabajos'!A1", enIndice.getCell(0).getHyperlink().getAddress());

            Sheet t = wb.getSheet("Trabajos");
            assertEquals("Trabajos realizados", t.getRow(0).getCell(0).getStringCellValue());
            assertTrue(t.getRow(1).getCell(0).getStringCellValue().startsWith("1 ene 2026 – 31 mar 2026"));
            assertEquals("Fecha", t.getRow(3).getCell(0).getStringCellValue());
            Row total = t.getRow(6);
            assertEquals("Total", total.getCell(0).getStringCellValue());
            assertEquals(570000.0, total.getCell(3).getNumericCellValue(), 0.01);
            assertEquals(8.0, total.getCell(4).getNumericCellValue(), 0.01);
            assertNotNull(t.getPaneInformation(), "Encabezado congelado");
            assertTrue(t.getPrintSetup().getLandscape(), "Lista para imprimir horizontal");
        }
    }

    @Test
    void hojaSinRegistrosLoDiceYNombresDeHojaSeLimpianYNoSeRepiten() throws Exception {
        byte[] xlsx;
        try (LibroExcel libro = new LibroExcel("Prueba", null, Periodo.todo())) {
            libro.tabla("Inspecciones", "Vacía", COLUMNAS, List.of());
            libro.tabla("Inspecciones", "Otra", COLUMNAS, filas());
            libro.tabla("Placa: ABC/123 [x]", "Raros", COLUMNAS, filas());
            xlsx = libro.bytes();
        }
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(xlsx))) {
            assertEquals("Inspecciones (2)", wb.getSheetName(2));
            assertFalse(wb.getSheetName(3).matches(".*[:/\\[\\]].*"), wb.getSheetName(3));
            assertTrue(leer(wb.getSheet("Inspecciones")).toString().contains("Sin registros"));
            assertTrue(leer(wb.getSheet("Resumen")).toString().contains("Todo el historial"));
        }
    }

    @Test
    void periodo() {
        Periodo p = new Periodo(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        assertTrue(p.contiene(LocalDate.of(2026, 1, 31)));
        assertFalse(p.contiene(LocalDate.of(2026, 2, 1)));
        assertTrue(p.contiene(LocalDateTime.of(2026, 1, 31, 23, 59)));
        assertFalse(p.contiene((LocalDate) null));
        assertTrue(Periodo.todo().contiene((LocalDate) null));
        assertEquals("_2026-01-01_a_2026-01-31", p.sufijoArchivo());
        assertEquals("", Periodo.todo().sufijoArchivo());
        assertThrows(BadRequestException.class, () -> new Periodo(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)));
    }
}
