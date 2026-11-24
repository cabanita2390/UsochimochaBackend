package com.app.usochicamochabackend.common.excel.reporte;

import com.app.usochicamochabackend.common.excel.RespuestaExcel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Todas las descargas a Excel del sistema por el mismo camino. */
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes a Excel", description = "Un solo motor para todas las descargas: cada reporte dice qué trae; el motor pone la portada, el estilo, el nombre y los permisos.")
public class ReportesController {

    private static final Set<String> RESERVADOS = Set.of("desde", "hasta", "columnas");

    private final MotorReportes motor;

    @GetMapping
    @Operation(summary = "Reportes que puede descargar el usuario",
            description = "Para cada uno: si usa periodo (y con qué atajo abre), sus filtros (los de tipo CONTEXTO los pone la pantalla) y sus columnas elegibles.")
    public ResponseEntity<List<MotorReportes.Info>> listar(Authentication authentication) {
        return ResponseEntity.ok(motor.listar(roles(authentication)));
    }

    @GetMapping("/{clave}")
    @Operation(summary = "Descargar un reporte",
            description = "desde / hasta (aaaa-mm-dd, opcionales) en los reportes con periodo; columnas separadas por coma en los que las tienen; cualquier otro parámetro es un filtro del reporte.")
    public ResponseEntity<byte[]> descargar(
            @PathVariable String clave,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String columnas,
            @RequestParam Map<String, String> parametros,
            Authentication authentication) throws IOException {
        Map<String, String> filtros = new HashMap<>(parametros);
        RESERVADOS.forEach(filtros::remove);
        List<String> cols = columnas == null || columnas.isBlank() ? List.of()
                : Arrays.stream(columnas.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        PeticionReporte p = new PeticionReporte(new com.app.usochicamochabackend.common.excel.Periodo(desde, hasta),
                filtros, cols, RespuestaExcel.usuarioActual());
        MotorReportes.Archivo a = motor.generar(clave, p, roles(authentication));
        return RespuestaExcel.de(a.contenido(), a.nombre());
    }

    static List<String> roles(Authentication a) {
        return a == null ? List.of() : a.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }
}
