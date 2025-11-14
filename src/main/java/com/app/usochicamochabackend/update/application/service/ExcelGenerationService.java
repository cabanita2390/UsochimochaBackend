package com.app.usochicamochabackend.update.application.service;

import com.app.usochicamochabackend.context.application.dto.MachineCurriculumDTO;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.update.application.dto.ConsolidateHydraulicAndMotorOilDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class ExcelGenerationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generateConsolidatedMachinesExcel(List<ConsolidateHydraulicAndMotorOilDTO> consolidatedData) throws IOException {
        log.info("Generando archivo Excel para {} máquinas", consolidatedData.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Consolidado de Máquinas");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Pertenece", "Maquina", "Horometro Actual", "Último Reporte",
                // Motor Oil
                "Tipo / Marca", "Cantidad (GI)", "Promedio Cambio (Horas)", "Fecha Ultimo Cambio",
                "Horómetro Último Cambio", "Horómetro Próximo Cambio", "Tiempo Último Cambio (Mes)",
                "Restante Cambio (Horas)", "Estado Actual",
                // Hydraulic Oil
                "Tipo / Marca", "Cantidad (GI)", "Promedio Cambio (Horas)", "Fecha Ultimo Cambio",
                "Horómetro Último Cambio", "Horómetro Próximo Cambio", "Tiempo Último Cambio (Mes)",
                "Restante Cambio (Horas)", "Estado Actual"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);

            int rowNum = 1;
            for (ConsolidateHydraulicAndMotorOilDTO data : consolidatedData) {
                Row row = sheet.createRow(rowNum++);

                int colNum = 0;

                row.createCell(colNum++).setCellValue(data.machine().belongsTo());
                row.createCell(colNum++).setCellValue(data.machine().name());

                if (data.currentData() != null) {
                    row.createCell(colNum++).setCellValue(data.currentData().currentHourMeter());
                    row.createCell(colNum++).setCellValue(data.currentData().lastUpdate() != null ?
                        data.currentData().lastUpdate().format(DATETIME_FORMATTER) : "");
                } else {
                    row.createCell(colNum++).setCellValue("");
                    row.createCell(colNum++).setCellValue("");
                }

                if (data.consolidateMotorOil() != null) {
                    row.createCell(colNum++).setCellValue(data.consolidateMotorOil().brand() != null ?
                        data.consolidateMotorOil().brand().getName() : "");
                    row.createCell(colNum++).setCellValue(data.consolidateMotorOil().quantity());
                    row.createCell(colNum++).setCellValue(data.consolidateMotorOil().averageChangeHours());
                    row.createCell(colNum++).setCellValue(data.consolidateMotorOil().dateLastUpdate() != null ?
                        data.consolidateMotorOil().dateLastUpdate().format(DATE_FORMATTER) : "");
                    row.createCell(colNum++).setCellValue(data.consolidateMotorOil().hourMeterLastUpdate());
                    row.createCell(colNum++).setCellValue(data.consolidateMotorOil().hourMeterNextUpdate());
                    row.createCell(colNum++).setCellValue(data.consolidateMotorOil().timeLastUpdateMouths());
                    row.createCell(colNum++).setCellValue(data.consolidateMotorOil().remainingHoursNextUpdateMouths());
                    row.createCell(colNum++).setCellValue(data.consolidateMotorOil().status());
                } else {
                    for (int i = 0; i < 9; i++) {
                        row.createCell(colNum++).setCellValue("");
                    }
                }

                if (data.consolidateHydraulicOil() != null) {
                    row.createCell(colNum++).setCellValue(data.consolidateHydraulicOil().brand() != null ?
                        data.consolidateHydraulicOil().brand().getName() : "");
                    row.createCell(colNum++).setCellValue(data.consolidateHydraulicOil().quantity());
                    row.createCell(colNum++).setCellValue(data.consolidateHydraulicOil().averageChangeHours());
                    row.createCell(colNum++).setCellValue(data.consolidateHydraulicOil().dateLastUpdate() != null ?
                        data.consolidateHydraulicOil().dateLastUpdate().format(DATE_FORMATTER) : "");
                    row.createCell(colNum++).setCellValue(data.consolidateHydraulicOil().hourMeterLastUpdate());
                    row.createCell(colNum++).setCellValue(data.consolidateHydraulicOil().hourMeterNextUpdate());
                    row.createCell(colNum++).setCellValue(data.consolidateHydraulicOil().timeLastUpdateMouths());
                    row.createCell(colNum++).setCellValue(data.consolidateHydraulicOil().remainingHoursNextUpdateMouths());
                    row.createCell(colNum++).setCellValue(data.consolidateHydraulicOil().status());
                } else {
                    for (int i = 0; i < 9; i++) {
                        row.createCell(colNum++).setCellValue("");
                    }
                }

                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Establecer un mínimo ancho para evitar columnas muy estrechas
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
                // Máximo ancho para evitar columnas excesivamente anchas
                if (sheet.getColumnWidth(i) > 8000) {
                    sheet.setColumnWidth(i, 8000);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            log.info("Archivo Excel generado exitosamente con {} filas de datos", consolidatedData.size());
            return outputStream.toByteArray();
        }
    }

    public byte[] generateMachineCurriculumExcel(List<MachineCurriculumDTO> curriculumData) throws IOException {
        log.info("Generando archivo Excel para curriculum de {} máquinas", curriculumData.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            for (MachineCurriculumDTO curriculum : curriculumData) {
                Sheet sheet = workbook.createSheet(curriculum.machine().name());

                // Styles
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);

                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);

                // First table: Machine info
                int rowNum = 0;
                Row machineHeaderRow = sheet.createRow(rowNum++);
                String[] machineHeaders = {"Equipo", "N Interno de identificacion", "Modelo", "Numero de Motor", "Vigencia soat", "Vigencia runt"};
                for (int i = 0; i < machineHeaders.length; i++) {
                    Cell cell = machineHeaderRow.createCell(i);
                    cell.setCellValue(machineHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }

                Row machineDataRow = sheet.createRow(rowNum++);
                machineDataRow.createCell(0).setCellValue(curriculum.machine().name());
                machineDataRow.createCell(1).setCellValue(curriculum.machine().numInterIdentification());
                machineDataRow.createCell(2).setCellValue(curriculum.machine().model());
                machineDataRow.createCell(3).setCellValue(curriculum.machine().numEngine());
                machineDataRow.createCell(4).setCellValue(curriculum.machine().soat() != null ? curriculum.machine().soat().format(DATE_FORMATTER) : "");
                machineDataRow.createCell(5).setCellValue(curriculum.machine().runt() != null ? curriculum.machine().runt().format(DATE_FORMATTER) : "");

                for (int i = 0; i < machineHeaders.length; i++) {
                    machineDataRow.getCell(i).setCellStyle(dataStyle);
                }

                // Skip two rows
                rowNum += 2;

                // Second table: Results
                Row resultsHeaderRow = sheet.createRow(rowNum++);
                String[] resultsHeaders = {"Fecha", "Horometro", "Descripcion", "REF", "Nombre", "Cantidad", "Valor", "Mecanico de planta", "Contratista", "Tiempo empleado", "Valor", "Valor total", "Descripcion mano de obre"};
                for (int i = 0; i < resultsHeaders.length; i++) {
                    Cell cell = resultsHeaderRow.createCell(i);
                    cell.setCellValue(resultsHeaders[i]);
                    cell.setCellStyle(headerStyle);
                }

                for (var result : curriculum.results()) {
                    Row row = sheet.createRow(rowNum++);
                    int colNum = 0;

                    // Fecha
                    Cell cell0 = row.createCell(colNum++);
                    cell0.setCellValue(result.date() != null ? result.date().format(DATETIME_FORMATTER) : "");

                    row.createCell(colNum++).setCellValue(result.hourMeter());

                    // Descripcion
                    row.createCell(colNum++).setCellValue(result.description());

                    // REF
                    row.createCell(colNum++).setCellValue(result.sparePart() != null ? result.sparePart().ref() : "");

                    // Nombre
                    row.createCell(colNum++).setCellValue(result.sparePart() != null ? result.sparePart().name() : "");

                    // Cantidad
                    row.createCell(colNum++).setCellValue(result.sparePart() != null ? result.sparePart().quantity() : "");

                    // Valor (spare part)
                    row.createCell(colNum++).setCellValue(result.sparePart() != null ? result.sparePart().price().toString() : "");

                    // Mecanico de planta
                    row.createCell(colNum++).setCellValue(result.labor() != null && result.labor().user() != null ? result.labor().user().getFullName() : "");

                    // Contratista
                    row.createCell(colNum++).setCellValue(result.labor() != null ? result.labor().contractor() : "");

                    // Tiempo empleado
                    row.createCell(colNum++).setCellValue(result.timeSpent());

                    // Valor (labor)
                    row.createCell(colNum++).setCellValue(result.labor() != null ? result.labor().price().toString() : "");

                    // Valor total
                    row.createCell(colNum++).setCellValue(result.totalPrice().toString());

                    row.createCell(colNum++).setCellValue(result.labor() != null ? result.labor().observations() : "");

                    for (int i = 0; i < resultsHeaders.length; i++) {
                        row.getCell(i).setCellStyle(dataStyle);
                    }
                }

                // Auto-size columns
                for (int i = 0; i < resultsHeaders.length; i++) {
                    sheet.autoSizeColumn(i);
                    if (sheet.getColumnWidth(i) < 3000) {
                        sheet.setColumnWidth(i, 3000);
                    }
                    if (sheet.getColumnWidth(i) > 8000) {
                        sheet.setColumnWidth(i, 8000);
                    }
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            log.info("Archivo Excel de curriculum generado exitosamente con {} máquinas", curriculumData.size());
            return outputStream.toByteArray();
        }
    }

    public byte[] generateInspectionsExcel(List<InspectionEntity> inspections) throws IOException {
        log.info("Generando archivo Excel para {} inspecciones", inspections.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inspecciones");

            // Styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);

            // Headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Mes", "Marca Temporal", "Maquina", "Horometro", "Fugas en el Sistema",
                "Sistema de Frenos", "Estado de Correas y Poleas", "Estado de Llantas y/o Carriles",
                "Sistema de Encendido", "Sistema Eléctrico", "Sistema Mecánico", "Nivel de Temperatura",
                "Nivel de Aceite", "Nivel de Hidraulico", "Nivel de Refrigerante", "Estado Estructural en General",
                "Vigencia EXTINTOR", "Observaciones", "Engrasado", "Observaciones Engrasado", "Responsable Inspección"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (InspectionEntity inspection : inspections) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                // Mes
                String month = inspection.getDateStamp().getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
                row.createCell(colNum++).setCellValue(month);

                // Marca Temporal
                row.createCell(colNum++).setCellValue(inspection.getDateStamp().format(DATETIME_FORMATTER));

                // Maquina
                row.createCell(colNum++).setCellValue(inspection.getMachine() != null ? inspection.getMachine().getName() : "");

                // Horometro
                row.createCell(colNum++).setCellValue(inspection.getHourMeter() != null ? inspection.getHourMeter().toString() : "");

                // Status fields
                row.createCell(colNum++).setCellValue(inspection.getLeakStatus() != null ? inspection.getLeakStatus() : "");
                row.createCell(colNum++).setCellValue(inspection.getBrakeStatus() != null ? inspection.getBrakeStatus() : "");
                row.createCell(colNum++).setCellValue(inspection.getBeltsPulleysStatus() != null ? inspection.getBeltsPulleysStatus() : "");
                row.createCell(colNum++).setCellValue(inspection.getTireLanesStatus() != null ? inspection.getTireLanesStatus() : "");
                row.createCell(colNum++).setCellValue(inspection.getCarIgnitionStatus() != null ? inspection.getCarIgnitionStatus() : "");
                row.createCell(colNum++).setCellValue(inspection.getElectricalStatus() != null ? inspection.getElectricalStatus() : "");
                row.createCell(colNum++).setCellValue(inspection.getMechanicalStatus() != null ? inspection.getMechanicalStatus() : "");
                row.createCell(colNum++).setCellValue(inspection.getTemperatureStatus() != null ? inspection.getTemperatureStatus() : "");
                row.createCell(colNum++).setCellValue(inspection.getOilStatus() != null ? inspection.getOilStatus() : "");
                row.createCell(colNum++).setCellValue(inspection.getHydraulicStatus() != null ? inspection.getHydraulicStatus() : "");
                row.createCell(colNum++).setCellValue(inspection.getCoolantStatus() != null ? inspection.getCoolantStatus() : "");
                row.createCell(colNum++).setCellValue(inspection.getStructuralStatus() != null ? inspection.getStructuralStatus() : "");

                // Vigencia EXTINTOR
                row.createCell(colNum++).setCellValue(inspection.getExpirationDateFireExtinguisher() != null ? inspection.getExpirationDateFireExtinguisher() : "");

                // Observaciones
                row.createCell(colNum++).setCellValue(inspection.getObservations() != null ? inspection.getObservations() : "");

                // Engrasado
                row.createCell(colNum++).setCellValue(inspection.getGreasingAction() != null ? inspection.getGreasingAction() : "");

                // Observaciones Engrasado
                row.createCell(colNum++).setCellValue(inspection.getGreasingObservations() != null ? inspection.getGreasingObservations() : "");

                // Responsable Inspección
                row.createCell(colNum++).setCellValue(inspection.getUser() != null ? inspection.getUser().getFullName() : "");

                // Apply style to all cells in the row
                for (int i = 0; i < headers.length; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
                if (sheet.getColumnWidth(i) > 8000) {
                    sheet.setColumnWidth(i, 8000);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            log.info("Archivo Excel de inspecciones generado exitosamente con {} filas de datos", inspections.size());
            return outputStream.toByteArray();
        }
    }
}