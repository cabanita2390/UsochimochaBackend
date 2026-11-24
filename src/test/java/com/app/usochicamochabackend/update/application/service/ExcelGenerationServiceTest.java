package com.app.usochicamochabackend.update.application.service;

import com.app.usochicamochabackend.common.excel.Periodo;
import com.app.usochicamochabackend.context.application.dto.MachineCurriculumDTO;
import com.app.usochicamochabackend.context.application.dto.MachineInspectionRecordDTO;
import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.performance.application.dto.ResultDTO;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Descargas de flota con el estilo común. */
class ExcelGenerationServiceTest {

    private final ExcelGenerationService service = new ExcelGenerationService();

    private static MachineInspectionRecordDTO inspeccion(LocalDateTime fecha, String trabajo, String valor) {
        ResultDTO r = new ResultDTO(1L, fecha, trabajo, 1200.0, "2 h", null, null, new BigDecimal(valor));
        return new MachineInspectionRecordDTO(1L, fecha, 1200.0, null, null, null, null, null, null, null, null,
                null, null, null, null, null, List.of(r));
    }

    @Test
    void hojaDeVidaDeUnaMaquinaSinSoatNiRunt() throws Exception {
        // Una máquina sin SOAT ni RUNT (datos reales): antes rompía con NullPointerException (Map.entry).
        MachineResponse m = new MachineResponse(12L, "Tractor 13", "Asociación", "2020", null, "John Deere", null,
                "MOT0012", "INT-012", 1200);
        MachineCurriculumDTO hoja = new MachineCurriculumDTO(m, List.of(
                inspeccion(LocalDateTime.of(2025, 6, 1, 8, 0), "Cambio de filtro", "100000"),
                inspeccion(LocalDateTime.of(2026, 3, 2, 8, 0), "Cambio de aceite", "250000")));

        byte[] todo = hojasDeVida(List.of(hoja), Periodo.todo());
        byte[] soloEsteAnio = hojasDeVida(List.of(hoja), new Periodo(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(todo))) {
            assertEquals("Resumen", wb.getSheetName(0));
            Sheet s = wb.getSheet("Tractor 13");
            String texto = s.getRow(0).getCell(0).getStringCellValue();
            assertEquals("Hoja de vida · Tractor 13", texto);
            boolean sinSoat = false;
            for (Row r : s) {
                if (r.getCell(0) != null && "Vigencia SOAT".equals(r.getCell(0).toString())) {
                    sinSoat = "—".equals(r.getCell(1).toString());
                }
            }
            assertTrue(sinSoat, "Un dato vacío sale como «—»");
        }
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(soloEsteAnio))) {
            assertTrue(contarFilas(wb.getSheet("Tractor 13"), "Cambio de aceite") == 1);
            assertTrue(contarFilas(wb.getSheet("Tractor 13"), "Cambio de filtro") == 0, "El trabajo de 2025 queda fuera del periodo");
        }
    }

    private byte[] hojasDeVida(List<MachineCurriculumDTO> hojas, Periodo periodo) throws java.io.IOException {
        try (com.app.usochicamochabackend.common.excel.LibroExcel libro =
                new com.app.usochicamochabackend.common.excel.LibroExcel("Hojas de vida · Maquinaria", "admin", periodo)) {
            service.llenarHojasDeVidaMaquinaria(libro, hojas, periodo);
            return libro.bytes();
        }
    }

    private static int contarFilas(Sheet s, String texto) {
        int n = 0;
        for (Row r : s) {
            for (var c : r) {
                if (texto.equals(c.toString())) {
                    n++;
                }
            }
        }
        return n;
    }

    @Test
    void coloresSegunElTextoQueGuardaElSistema() {
        assertEquals(com.app.usochicamochabackend.common.excel.ColumnaExcel.Estado.MAL, ExcelGenerationService.estadoDe("VENCIDO"));
        assertEquals(com.app.usochicamochabackend.common.excel.ColumnaExcel.Estado.ALERTA, ExcelGenerationService.estadoDe("Próximo"));
        assertEquals(com.app.usochicamochabackend.common.excel.ColumnaExcel.Estado.OK, ExcelGenerationService.estadoDe("Bueno"));
        assertEquals(null, ExcelGenerationService.estadoDe("N/A"));
    }

    @Test
    void siNoEnRojoSoloLaRespuestaMala() {
        var sobrio = ExcelGenerationService.<Boolean>siNoCol("Sobriedad", b -> b, true);
        var medicamentos = ExcelGenerationService.<Boolean>siNoCol("Medicamentos", b -> b, false);
        assertEquals(null, sobrio.estado().apply(true));
        assertEquals(com.app.usochicamochabackend.common.excel.ColumnaExcel.Estado.MAL, sobrio.estado().apply(false));
        // Medicamentos que afectan los sentidos: lo malo es el "Sí".
        assertEquals(com.app.usochicamochabackend.common.excel.ColumnaExcel.Estado.MAL, medicamentos.estado().apply(true));
        assertEquals(null, medicamentos.estado().apply(false));
        assertEquals(null, medicamentos.estado().apply(null));
        assertEquals("No", medicamentos.valor().apply(false));
    }
}
