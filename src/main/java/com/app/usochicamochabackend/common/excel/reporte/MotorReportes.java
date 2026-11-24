package com.app.usochicamochabackend.common.excel.reporte;

import com.app.usochicamochabackend.common.excel.LibroExcel;
import com.app.usochicamochabackend.common.excel.Periodo;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * El único motor de descargas a Excel. Junta todos los {@link ReporteExcel} del sistema y los
 * genera siempre igual: mismo libro ({@link LibroExcel}), misma portada, mismo nombre de archivo y
 * mismos permisos. Ningún reporte arma su archivo por su cuenta.
 */
@Slf4j
@Service
public class MotorReportes {

    private final Map<String, ReporteExcel> reportes = new LinkedHashMap<>();

    public MotorReportes(List<ReporteExcel> todos) {
        todos.stream()
                .sorted(Comparator.comparing(ReporteExcel::modulo).thenComparing(ReporteExcel::titulo))
                .forEach(r -> {
                    if (reportes.put(r.clave(), r) != null) {
                        throw new IllegalStateException("Dos reportes con la clave " + r.clave());
                    }
                });
        log.info("Reportes a Excel registrados: {}", reportes.keySet());
    }

    /** El archivo generado y su nombre ("ordenes_maquinaria_2026-01-01_a_2026-12-31.xlsx"). */
    public record Archivo(String nombre, byte[] contenido) {
    }

    /** Lo que la web necesita para el botón y el panel de cada reporte. */
    public record Info(String clave, String titulo, String modulo, boolean usaPeriodo, String periodoInicial,
            List<FiltroReporte> filtros, List<ColumnaReporte> columnas) {
    }

    /** Los reportes que puede descargar quien tenga alguno de esos roles. */
    public List<Info> listar(Collection<String> roles) {
        return reportes.values().stream()
                .filter(r -> puede(r, roles))
                .map(r -> new Info(r.clave(), r.titulo(), r.modulo(), r.usaPeriodo(), r.periodoInicial(), r.filtros(),
                        r.columnas()))
                .toList();
    }

    public ReporteExcel buscar(String clave) {
        ReporteExcel r = reportes.get(clave);
        if (r == null) {
            throw new ResourceNotFoundException("Reporte desconocido: " + clave);
        }
        return r;
    }

    /**
     * Genera el reporte para quien tenga esos roles (403 si no puede). Sin transacción propia: cada
     * reporte lee como lo hace su pantalla (algunas lecturas de flota guardan de paso, p. ej. alertas).
     */
    public Archivo generar(String clave, PeticionReporte p, Collection<String> roles) throws IOException {
        ReporteExcel r = buscar(clave);
        if (!puede(r, roles)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tiene permiso para descargar " + r.titulo());
        }
        r.filtros().stream()
                .filter(FiltroReporte::obligatorio)
                .forEach(f -> p.filtroObligatorio(f.clave()));
        return generar(r, p);
    }

    /** Genera un reporte sin pasar por el registro ni los permisos (pruebas y servicios que ya validaron). */
    public static Archivo generar(ReporteExcel r, PeticionReporte p) throws IOException {
        PeticionReporte efectiva = r.usaPeriodo() ? p : new PeticionReporte(Periodo.todo(), p.filtros(), p.columnas(), p.usuario());
        Periodo periodo = r.periodo(efectiva);
        try (LibroExcel libro = new LibroExcel(r.tituloLibro(efectiva), efectiva.usuario(), periodo)) {
            r.llenar(libro, efectiva);
            return new Archivo(nombre(r, efectiva, periodo), libro.bytes());
        }
    }

    /**
     * Para los endpoints anteriores al motor (siguen respondiendo igual): genera el reporte para el
     * usuario en sesión y lo devuelve como descarga.
     */
    public org.springframework.http.ResponseEntity<byte[]> descargar(String clave, LocalDate desde, LocalDate hasta,
            Map<String, String> filtros) throws IOException {
        return descargar(clave, desde, hasta, filtros, null);
    }

    /** Igual, con las columnas elegidas (Estaciones). */
    public org.springframework.http.ResponseEntity<byte[]> descargar(String clave, LocalDate desde, LocalDate hasta,
            Map<String, String> filtros, List<String> columnas) throws IOException {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = auth == null ? List.of()
                : auth.getAuthorities().stream().map(org.springframework.security.core.GrantedAuthority::getAuthority).toList();
        PeticionReporte p = new PeticionReporte(new Periodo(desde, hasta), filtros, columnas,
                com.app.usochicamochabackend.common.excel.RespuestaExcel.usuarioActual());
        Archivo a = generar(clave, p, roles);
        return com.app.usochicamochabackend.common.excel.RespuestaExcel.de(a.contenido(), a.nombre());
    }

    private static String nombre(ReporteExcel r, PeticionReporte p, Periodo periodo) {
        String base = r.nombreArchivo(p);
        String sufijo = periodo.esTodo()
                ? (r.usaPeriodo() ? "" : "_" + LocalDate.now())
                : periodo.sufijoArchivo();
        return base + sufijo + ".xlsx";
    }

    private static boolean puede(ReporteExcel r, Collection<String> roles) {
        return roles.stream().map(x -> x.startsWith("ROLE_") ? x.substring(5) : x).anyMatch(r.roles()::contains);
    }
}
