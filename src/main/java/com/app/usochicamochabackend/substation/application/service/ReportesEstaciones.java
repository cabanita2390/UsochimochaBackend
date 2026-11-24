package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.common.excel.LibroExcel;
import com.app.usochicamochabackend.common.excel.Periodo;
import com.app.usochicamochabackend.common.excel.reporte.ColumnaReporte;
import com.app.usochicamochabackend.common.excel.reporte.FiltroReporte;
import com.app.usochicamochabackend.common.excel.reporte.PeticionReporte;
import com.app.usochicamochabackend.common.excel.reporte.ReporteExcel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Las hojas de Estaciones de Bombeo registradas en el motor único ({@code MotorReportes}), una por
 * hoja: "estaciones-ejecuciones", "estaciones-hallazgos"… Sus filtros (disciplina, año, mes,
 * estación) y columnas los pone {@link ExportacionEstacionesService}; el periodo sale del año y mes.
 */
@Configuration
public class ReportesEstaciones {

    /** Una hoja de Estaciones como reporte. */
    public static final class Hoja implements ReporteExcel {
        private final ExportacionEstacionesService servicio;
        private final String hoja;
        private final String titulo;

        public Hoja(ExportacionEstacionesService servicio, String hoja, String titulo) {
            this.servicio = servicio;
            this.hoja = hoja;
            this.titulo = titulo;
        }

        @Override public String clave() { return ReportesEstaciones.clave(hoja); }
        @Override public String titulo() { return titulo; }
        @Override public String modulo() { return ReporteExcel.ESTACIONES; }
        @Override public List<FiltroReporte> filtros() { return servicio.filtrosDe(hoja); }
        @Override public List<ColumnaReporte> columnas() { return servicio.columnasDe(hoja); }
        @Override public Periodo periodo(PeticionReporte p) { return servicio.periodoDe(hoja, p); }
        @Override public String tituloLibro(PeticionReporte p) { return ReporteExcel.ESTACIONES + " · " + titulo; }
        @Override public String nombreArchivo(PeticionReporte p) { return servicio.nombreArchivoDe(hoja, p); }

        @Override
        public void llenar(LibroExcel libro, PeticionReporte p) {
            servicio.llenar(hoja, libro, p);
        }
    }

    /** "EJECUCIONES" → "estaciones-ejecuciones". */
    public static String clave(String hoja) {
        return "estaciones-" + hoja.toLowerCase();
    }

    @Bean ReporteExcel reporteEstacionesEjecuciones(ExportacionEstacionesService s) { return hoja(s, 0); }
    @Bean ReporteExcel reporteEstacionesHallazgos(ExportacionEstacionesService s) { return hoja(s, 1); }
    @Bean ReporteExcel reporteEstacionesOrdenes(ExportacionEstacionesService s) { return hoja(s, 2); }
    @Bean ReporteExcel reporteEstacionesAvance(ExportacionEstacionesService s) { return hoja(s, 3); }
    @Bean ReporteExcel reporteEstacionesActividades(ExportacionEstacionesService s) { return hoja(s, 4); }

    private static ReporteExcel hoja(ExportacionEstacionesService s, int i) {
        Map.Entry<String, String> h = ExportacionEstacionesService.HOJAS.get(i);
        return new Hoja(s, h.getKey(), h.getValue());
    }
}
