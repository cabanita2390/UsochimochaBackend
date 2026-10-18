package com.app.usochicamochabackend.substation.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.substation.application.dto.ComentarioOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.CrearOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.EditarOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.OperarioResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenDetalleResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenMovilResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenResumenResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationOrdenUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/substation/ordenes")
@RequiredArgsConstructor
@Tag(name = "Subestaciones - Órdenes de trabajo",
        description = "OT-EB para corregir hallazgos: ASIGNADA → POR_VERIFICAR → CERRADA (o ANULADA).")
public class SubstationOrdenController {

    private final SubstationOrdenUseCase ordenUseCase;

    @PostMapping
    @Operation(summary = "Crear una orden de trabajo para un hallazgo",
            description = "404 si la ejecución no existe; 409 si es CONFORME, ya está resuelto o ya tiene una orden vigente; "
                    + "400 si falta un dato, la fecha límite ya pasó, el asignado no es un OPERARIO activo o la actividad "
                    + "es de otra disciplina. Pasa el hallazgo a EN_PROCESO.")
    public ResponseEntity<OrdenResumenResponse> crear(@RequestBody CrearOrdenRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenUseCase.crear(request, usuario(authentication)));
    }

    @GetMapping
    @Operation(summary = "Órdenes de trabajo, con filtros opcionales",
            description = "estado acepta varios valores separados por coma (ej. estado=ASIGNADA,POR_VERIFICAR). "
                    + "vencidas=true deja las que siguen en curso con la fecha límite pasada. Por defecto, las más recientes primero.")
    public ResponseEntity<Page<OrdenResumenResponse>> listar(
            @RequestParam(required = false) List<String> estado,
            @RequestParam(required = false) Long estacionId,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) Boolean vencidas,
            @RequestParam(required = false) String disciplina,
            Pageable pageable) {
        return ResponseEntity.ok(ordenUseCase.listar(estado, estacionId, prioridad, vencidas, disciplina, pageable));
    }

    @GetMapping("/operarios")
    @Operation(summary = "Operarios activos a los que se puede asignar una orden")
    public ResponseEntity<List<OperarioResponse>> operarios() {
        return ResponseEntity.ok(ordenUseCase.operarios());
    }

    @GetMapping("/asignadas")
    @Operation(summary = "Órdenes asignadas al usuario (lista \"Prioritarias\" del móvil)",
            description = "Solo las ASIGNADA, primero las de prioridad alta y las que vencen antes; con las fotos del hallazgo "
                    + "y, si la devolvieron, el motivo.")
    public ResponseEntity<List<OrdenMovilResponse>> asignadas(Authentication authentication) {
        return ResponseEntity.ok(ordenUseCase.asignadas(usuario(authentication)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle: hallazgo (antes), visita que la corrigió (después) y línea de tiempo")
    public ResponseEntity<OrdenDetalleResponse> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(ordenUseCase.detalle(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar una orden ASIGNADA (reasignar, plazo, prioridad, actividad, instrucciones)",
            description = "Los campos null no cambian. 409 si la orden ya no está ASIGNADA.")
    public ResponseEntity<OrdenResumenResponse> editar(@PathVariable Long id, @RequestBody EditarOrdenRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ordenUseCase.editar(id, request, usuario(authentication)));
    }

    @PutMapping("/{id}/anular")
    @Operation(summary = "Anular una orden en curso (motivo obligatorio)",
            description = "El hallazgo vuelve a ABIERTO: se puede resolver sin orden o crearle otra. 409 si ya está cerrada o anulada.")
    public ResponseEntity<OrdenResumenResponse> anular(@PathVariable Long id, @RequestBody ComentarioOrdenRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ordenUseCase.anular(id, request, usuario(authentication)));
    }

    @PutMapping("/{id}/cerrar")
    @Operation(summary = "Verificar y cerrar una orden POR_VERIFICAR (observación obligatoria)",
            description = "El hallazgo queda RESUELTO en la visita de la orden. 409 si la orden no está POR_VERIFICAR.")
    public ResponseEntity<OrdenResumenResponse> cerrar(@PathVariable Long id, @RequestBody ComentarioOrdenRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ordenUseCase.cerrar(id, request, usuario(authentication)));
    }

    @PutMapping("/{id}/devolver")
    @Operation(summary = "Devolver al operario una orden POR_VERIFICAR (motivo obligatorio)",
            description = "Vuelve a ASIGNADA y a la lista del móvil con el motivo. 409 si la orden no está POR_VERIFICAR.")
    public ResponseEntity<OrdenResumenResponse> devolver(@PathVariable Long id, @RequestBody ComentarioOrdenRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(ordenUseCase.devolver(id, request, usuario(authentication)));
    }

    private UserPrincipal usuario(Authentication authentication) {
        return (UserPrincipal) authentication.getPrincipal();
    }
}
