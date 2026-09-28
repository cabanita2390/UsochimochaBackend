package com.app.usochicamochabackend.substation.application.port;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.substation.application.dto.ActividadRequest;
import com.app.usochicamochabackend.substation.application.dto.ActividadResponse;
import com.app.usochicamochabackend.substation.application.dto.EstacionRequest;
import com.app.usochicamochabackend.substation.application.dto.EstacionResponse;

import java.util.List;

/** Catálogo de estaciones y actividades: lectura (móvil y web) y administración (solo ADMIN, ver SecurityConfig). */
public interface SubstationCatalogAdminUseCase {

    // ----- Estaciones -----

    /** Sin inactivas es lo que ve el móvil; con inactivas, la pantalla Configuración de la web. */
    List<EstacionResponse> listarEstaciones(boolean incluirInactivas);

    EstacionResponse crearEstacion(EstacionRequest request, UserPrincipal usuario);

    EstacionResponse actualizarEstacion(Long id, EstacionRequest request, UserPrincipal usuario);

    /** Soft-delete: no toca citas ni ejecuciones de la estación. */
    EstacionResponse cambiarEstadoEstacion(Long id, boolean activa, UserPrincipal usuario);

    // ----- Actividades -----

    /**
     * Sin inactivas: solo activas y habilitadas para captura móvil (lo que ve el móvil).
     * Con inactivas: todas, para Configuración. {@code disciplina} null = todas las disciplinas.
     */
    List<ActividadResponse> listarActividades(String disciplina, boolean incluirInactivas);

    ActividadResponse crearActividad(ActividadRequest request, UserPrincipal usuario);

    ActividadResponse actualizarActividad(Long id, ActividadRequest request, UserPrincipal usuario);

    /** Soft-delete: el móvil deja de verla en su próxima sincronización de catálogos. */
    ActividadResponse cambiarEstadoActividad(Long id, boolean activa, UserPrincipal usuario);
}
