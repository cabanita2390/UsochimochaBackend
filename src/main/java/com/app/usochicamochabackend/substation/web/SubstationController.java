package com.app.usochicamochabackend.substation.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.substation.application.dto.CriticidadResponse;
import com.app.usochicamochabackend.substation.application.dto.CumplimientoResponse;
import com.app.usochicamochabackend.substation.application.dto.EjecucionEditRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionResponse;
import com.app.usochicamochabackend.substation.application.dto.EvidenciaResponse;
import com.app.usochicamochabackend.substation.application.dto.IndicadorEstacionResponse;
import com.app.usochicamochabackend.substation.application.dto.ProgramacionResponse;
import com.app.usochicamochabackend.substation.application.dto.ResumenActividadResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationCatalogUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationIndicadoresUseCase;
import com.app.usochicamochabackend.substation.application.service.CalendarioMantenimiento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/substation")
@RequiredArgsConstructor
@Tag(name = "Subestaciones", description = "Mantenimiento de estaciones de bombeo e infraestructura complementaria (MVP: disciplina CIVIL).")
public class SubstationController {

    private final SubstationCatalogUseCase catalogUseCase;
    private final SubstationEjecucionUseCase ejecucionUseCase;
    private final SubstationIndicadoresUseCase indicadoresUseCase;
    private final CalendarioMantenimiento calendario;

    @GetMapping("/programacion")
    @Operation(summary = "Citas del cronograma para una estación+mes+disciplina")
    public ResponseEntity<List<ProgramacionResponse>> listarProgramacion(
            @RequestParam Long estacionId,
            @RequestParam Integer anio,
            @RequestParam Integer mes,
            @RequestParam String disciplina) {
        return ResponseEntity.ok(catalogUseCase.listarProgramacion(estacionId, anio, mes, disciplina));
    }

    @PostMapping(path = "/ejecuciones", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registrar una ejecución de mantenimiento", description = "Idempotente por uuidCliente: reenviar el mismo uuid no duplica el registro.")
    public ResponseEntity<EjecucionResponse> registrarEjecucion(
            @RequestBody EjecucionRequest request,
            Authentication authentication) throws URISyntaxException {
        UserPrincipal usuario = (UserPrincipal) authentication.getPrincipal();
        EjecucionResponse saved;
        try {
            saved = ejecucionUseCase.registrarEjecucion(request, usuario);
        } catch (DataIntegrityViolationException e) {
            // Dos envíos simultáneos del mismo registro (reintento del móvil mientras el primero
            // seguía en curso): el segundo choca con el índice único de uuidCliente. Se responde
            // con el que ya quedó guardado; un 409 haría que el móvil lo marcara como fallido.
            saved = ejecucionUseCase.buscarPorUuidCliente(request.uuidCliente()).orElseThrow(() -> e);
        }
        return ResponseEntity.created(new URI("/api/v1/substation/ejecuciones/" + saved.id())).body(saved);
    }

    @PutMapping(path = "/ejecuciones/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Corregir una ejecución ya registrada", description = "Exige motivoEdicion (mínimo 15 caracteres); queda en el historial del registro. No permite cambiar estación, disciplina ni la cita de programación asociada.")
    public ResponseEntity<EjecucionResponse> editarEjecucion(
            @PathVariable Long id,
            @RequestBody EjecucionEditRequest request,
            Authentication authentication) {
        UserPrincipal usuario = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ejecucionUseCase.editarEjecucion(id, request, usuario));
    }

    @PostMapping(path = "/ejecuciones/{id}/evidencia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir una foto de evidencia para una ejecución ya registrada",
            description = "Idempotente por (ejecucionId, hash del archivo): reenviar la misma foto tras un reintento de red no duplica el registro.")
    public ResponseEntity<EvidenciaResponse> agregarEvidencia(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(ejecucionUseCase.agregarEvidencia(id, file));
    }

    @GetMapping("/ejecuciones/{id}")
    @Operation(summary = "Detalle de una ejecución, con sus evidencias")
    public ResponseEntity<EjecucionResponse> obtenerEjecucion(@PathVariable Long id) {
        return ResponseEntity.ok(ejecucionUseCase.obtenerEjecucion(id));
    }

    @GetMapping("/ejecuciones/por-programacion/{programacionId}")
    @Operation(summary = "Ejecución registrada para una cita del cronograma", description = "Para 'ver detalle' desde una cita ya marcada como cumplida en el cronograma.")
    public ResponseEntity<EjecucionResponse> obtenerEjecucionPorProgramacion(@PathVariable Long programacionId) {
        return ResponseEntity.ok(ejecucionUseCase.obtenerEjecucionPorProgramacion(programacionId));
    }

    @GetMapping("/ejecuciones")
    @Operation(summary = "Listado de ejecuciones por rango de fecha, con filtros opcionales",
            description = "Sin estacionId: todas las estaciones. Con estacionId: solo esa estación. "
                    + "esProgramada, actividadId, tipoMantenimiento y tipoActividad son opcionales. "
                    + "resultado acepta uno o varios valores separados por coma (ej. resultado=CON_HALLAZGOS,"
                    + "REQUIERE_INTERVENCION para el preset 'solo hallazgos' de la pantalla de Ejecuciones). "
                    + "seguimiento filtra por estado del hallazgo (ej. seguimiento=ABIERTO,EN_PROCESO para "
                    + "el chip 'Solo hallazgos abiertos').")
    public ResponseEntity<Page<EjecucionResponse>> listarEjecuciones(
            @RequestParam(required = false) Long estacionId,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin,
            @RequestParam(required = false) Boolean esProgramada,
            @RequestParam(required = false) List<String> resultado,
            @RequestParam(required = false) Long actividadId,
            @RequestParam(required = false) String tipoMantenimiento,
            @RequestParam(required = false) String tipoActividad,
            @RequestParam(required = false) List<String> seguimiento,
            @RequestParam(required = false) String disciplina,
            Pageable pageable) {
        return ResponseEntity.ok(ejecucionUseCase.listarEjecuciones(
                estacionId, fechaInicio, fechaFin, esProgramada,
                resultado, actividadId, tipoMantenimiento, tipoActividad, seguimiento, disciplina, pageable));
    }

    @GetMapping("/indicadores/cumplimiento")
    @Operation(summary = "Cumplimiento del cronograma",
            description = "Sin estacionId: todas las estaciones para un mes+año dado. Con estacionId: esa estación a lo largo del año (ignora mes).")
    public ResponseEntity<List<CumplimientoResponse>> cumplimiento(
            @RequestParam(required = false) Long estacionId,
            @RequestParam Integer anio,
            @RequestParam(required = false) Integer mes,
            @RequestParam String disciplina) {
        if (estacionId != null) {
            return ResponseEntity.ok(indicadoresUseCase.cumplimientoPorEstacion(estacionId, anio, disciplina));
        }
        return ResponseEntity.ok(indicadoresUseCase.cumplimientoPorMes(anio, mes, disciplina));
    }

    @GetMapping("/indicadores/por-estacion")
    @Operation(summary = "Dashboard de estaciones",
            description = "Una fila por estación activa con lo publicado y lo ejecutado del año y la disciplina. "
                    + "% de cumplimiento = ejecutadasVencidas / vencidas (null si aún no hay citas vencidas). "
                    + "anio: por defecto el actual; disciplina: sin ella, todas las disciplinas.")
    public ResponseEntity<List<IndicadorEstacionResponse>> indicadoresPorEstacion(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) String disciplina) {
        return ResponseEntity.ok(indicadoresUseCase.indicadoresPorEstacion(
                anio != null ? anio : calendario.anioActual(), disciplina));
    }

    @GetMapping("/indicadores/por-actividad")
    @Operation(summary = "Resumen por actividad",
            description = "Una fila por actividad activa de la disciplina, con lo publicado y lo ejecutado del año. "
                    + "Mismo % de cumplimiento que el Dashboard. anio: por defecto el actual; "
                    + "disciplina: sin ella, las actividades de todas las disciplinas.")
    public ResponseEntity<List<ResumenActividadResponse>> resumenPorActividad(
            @RequestParam(required = false) String disciplina,
            @RequestParam(required = false) Integer anio) {
        return ResponseEntity.ok(indicadoresUseCase.resumenPorActividad(
                disciplina, anio != null ? anio : calendario.anioActual()));
    }

    @GetMapping("/indicadores/criticidad")
    @Operation(summary = "Actividades más críticas de una estación",
            description = "Intervenciones acumuladas (todos los años) por actividad en la estación, de más a menos. "
                    + "Intervención = ejecución con actividad del catálogo; las libres no cuentan. "
                    + "Para el bloque \"Actividades más críticas\" del Detalle por estación. "
                    + "disciplina: sin ella, todas.")
    public ResponseEntity<List<CriticidadResponse>> criticidadPorEstacion(
            @RequestParam Long estacionId,
            @RequestParam(required = false) String disciplina) {
        return ResponseEntity.ok(indicadoresUseCase.criticidadPorEstacion(estacionId, disciplina));
    }
}
