package com.app.usochicamochabackend.substation.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.substation.application.dto.ActividadRequest;
import com.app.usochicamochabackend.substation.application.dto.ActividadResponse;
import com.app.usochicamochabackend.substation.application.dto.CambioEstadoRequest;
import com.app.usochicamochabackend.substation.application.dto.EstacionRequest;
import com.app.usochicamochabackend.substation.application.dto.EstacionResponse;
import com.app.usochicamochabackend.substation.application.dto.ItemCatalogoRequest;
import com.app.usochicamochabackend.substation.application.dto.ItemCatalogoResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationCatalogAdminUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/substation")
@RequiredArgsConstructor
@Tag(name = "Subestaciones - Catálogo", description = "Estaciones, actividades, conjuntos y elementos: consulta y administración (alta, edición, activar/desactivar).")
public class SubstationCatalogAdminController {

    private final SubstationCatalogAdminUseCase catalogAdminUseCase;

    @GetMapping("/estaciones")
    @Operation(summary = "Listar estaciones", description = "Por defecto solo activas (lo que usa el móvil). incluirInactivas=true para Configuración.")
    public ResponseEntity<List<EstacionResponse>> listarEstaciones(
            @RequestParam(defaultValue = "false") boolean incluirInactivas) {
        return ResponseEntity.ok(catalogAdminUseCase.listarEstaciones(incluirInactivas));
    }

    @PostMapping("/estaciones")
    @Operation(summary = "Crear estación", description = "409 si ya existe una estación con ese nombre.")
    public ResponseEntity<EstacionResponse> crearEstacion(
            @RequestBody @Valid EstacionRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogAdminUseCase.crearEstacion(request, usuario(authentication)));
    }

    @PutMapping("/estaciones/{id}")
    @Operation(summary = "Editar estación", description = "409 si el nombre nuevo ya lo usa otra estación.")
    public ResponseEntity<EstacionResponse> actualizarEstacion(
            @PathVariable Long id, @RequestBody @Valid EstacionRequest request, Authentication authentication) {
        return ResponseEntity.ok(catalogAdminUseCase.actualizarEstacion(id, request, usuario(authentication)));
    }

    @PatchMapping("/estaciones/{id}/estado")
    @Operation(summary = "Activar o desactivar estación", description = "Soft-delete: no toca sus citas publicadas ni sus ejecuciones.")
    public ResponseEntity<EstacionResponse> cambiarEstadoEstacion(
            @PathVariable Long id, @RequestBody @Valid CambioEstadoRequest request, Authentication authentication) {
        return ResponseEntity.ok(catalogAdminUseCase.cambiarEstadoEstacion(id, request.activa(), usuario(authentication)));
    }

    @GetMapping("/actividades")
    @Operation(summary = "Listar actividades",
            description = "Por defecto solo activas y habilitadas para captura móvil (lo que usa el móvil). "
                    + "incluirInactivas=true devuelve todas, para Configuración. Sin disciplina: todas las disciplinas.")
    public ResponseEntity<List<ActividadResponse>> listarActividades(
            @RequestParam(required = false) String disciplina,
            @RequestParam(defaultValue = "false") boolean incluirInactivas) {
        return ResponseEntity.ok(catalogAdminUseCase.listarActividades(disciplina, incluirInactivas));
    }

    @PostMapping("/actividades")
    @Operation(summary = "Crear actividad", description = "409 si ya existe con ese nombre en la misma disciplina; 400 si la disciplina no existe.")
    public ResponseEntity<ActividadResponse> crearActividad(
            @RequestBody @Valid ActividadRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogAdminUseCase.crearActividad(request, usuario(authentication)));
    }

    @PutMapping("/actividades/{id}")
    @Operation(summary = "Editar actividad", description = "409 si el nombre nuevo ya lo usa otra actividad de la misma disciplina.")
    public ResponseEntity<ActividadResponse> actualizarActividad(
            @PathVariable Long id, @RequestBody @Valid ActividadRequest request, Authentication authentication) {
        return ResponseEntity.ok(catalogAdminUseCase.actualizarActividad(id, request, usuario(authentication)));
    }

    @PatchMapping("/actividades/{id}/estado")
    @Operation(summary = "Activar o desactivar actividad", description = "Soft-delete: el móvil deja de verla en su próxima sincronización.")
    public ResponseEntity<ActividadResponse> cambiarEstadoActividad(
            @PathVariable Long id, @RequestBody @Valid CambioEstadoRequest request, Authentication authentication) {
        return ResponseEntity.ok(catalogAdminUseCase.cambiarEstadoActividad(id, request.activa(), usuario(authentication)));
    }

    @GetMapping("/conjuntos")
    @Operation(summary = "Listar conjuntos",
            description = "\"Tipo de conjunto intervenido\" del formulario eléctrico. Por defecto solo activos (lo que usa "
                    + "el móvil); incluirInactivas=true para Configuración. Sin disciplina: todas las disciplinas.")
    public ResponseEntity<List<ItemCatalogoResponse>> listarConjuntos(
            @RequestParam(required = false) String disciplina,
            @RequestParam(defaultValue = "false") boolean incluirInactivas) {
        return ResponseEntity.ok(catalogAdminUseCase.listarConjuntos(disciplina, incluirInactivas));
    }

    @PostMapping("/conjuntos")
    @Operation(summary = "Crear conjunto", description = "409 si ya existe con ese nombre en la misma disciplina; 400 si la disciplina no existe.")
    public ResponseEntity<ItemCatalogoResponse> crearConjunto(
            @RequestBody @Valid ItemCatalogoRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogAdminUseCase.crearConjunto(request, usuario(authentication)));
    }

    @PutMapping("/conjuntos/{id}")
    @Operation(summary = "Editar conjunto", description = "409 si el nombre ya lo usa otro conjunto de la disciplina, o si se cambia la disciplina de uno ya registrado en visitas.")
    public ResponseEntity<ItemCatalogoResponse> actualizarConjunto(
            @PathVariable Long id, @RequestBody @Valid ItemCatalogoRequest request, Authentication authentication) {
        return ResponseEntity.ok(catalogAdminUseCase.actualizarConjunto(id, request, usuario(authentication)));
    }

    @PatchMapping("/conjuntos/{id}/estado")
    @Operation(summary = "Activar o desactivar conjunto", description = "Soft-delete: el móvil deja de ofrecerlo; las visitas que ya lo marcaron lo conservan.")
    public ResponseEntity<ItemCatalogoResponse> cambiarEstadoConjunto(
            @PathVariable Long id, @RequestBody @Valid CambioEstadoRequest request, Authentication authentication) {
        return ResponseEntity.ok(catalogAdminUseCase.cambiarEstadoConjunto(id, request.activa(), usuario(authentication)));
    }

    @GetMapping("/elementos")
    @Operation(summary = "Listar elementos",
            description = "\"Elementos inspeccionados o intervenidos\" del formulario eléctrico. Por defecto solo activos (lo "
                    + "que usa el móvil); incluirInactivas=true para Configuración. Sin disciplina: todas las disciplinas.")
    public ResponseEntity<List<ItemCatalogoResponse>> listarElementos(
            @RequestParam(required = false) String disciplina,
            @RequestParam(defaultValue = "false") boolean incluirInactivas) {
        return ResponseEntity.ok(catalogAdminUseCase.listarElementos(disciplina, incluirInactivas));
    }

    @PostMapping("/elementos")
    @Operation(summary = "Crear elemento", description = "409 si ya existe con ese nombre en la misma disciplina; 400 si la disciplina no existe.")
    public ResponseEntity<ItemCatalogoResponse> crearElemento(
            @RequestBody @Valid ItemCatalogoRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogAdminUseCase.crearElemento(request, usuario(authentication)));
    }

    @PutMapping("/elementos/{id}")
    @Operation(summary = "Editar elemento", description = "409 si el nombre ya lo usa otro elemento de la disciplina, o si se cambia la disciplina de uno ya registrado en visitas.")
    public ResponseEntity<ItemCatalogoResponse> actualizarElemento(
            @PathVariable Long id, @RequestBody @Valid ItemCatalogoRequest request, Authentication authentication) {
        return ResponseEntity.ok(catalogAdminUseCase.actualizarElemento(id, request, usuario(authentication)));
    }

    @PatchMapping("/elementos/{id}/estado")
    @Operation(summary = "Activar o desactivar elemento", description = "Soft-delete: el móvil deja de ofrecerlo; las visitas que ya lo marcaron lo conservan.")
    public ResponseEntity<ItemCatalogoResponse> cambiarEstadoElemento(
            @PathVariable Long id, @RequestBody @Valid CambioEstadoRequest request, Authentication authentication) {
        return ResponseEntity.ok(catalogAdminUseCase.cambiarEstadoElemento(id, request.activa(), usuario(authentication)));
    }

    private UserPrincipal usuario(Authentication authentication) {
        return (UserPrincipal) authentication.getPrincipal();
    }
}
