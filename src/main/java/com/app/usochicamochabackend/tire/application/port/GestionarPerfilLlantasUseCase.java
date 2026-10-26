package com.app.usochicamochabackend.tire.application.port;

import com.app.usochicamochabackend.tire.application.dto.AsignacionLlantasRequest;
import com.app.usochicamochabackend.tire.application.dto.ConfigLlantasActivoResponse;
import com.app.usochicamochabackend.tire.application.dto.PerfilLlantasRequest;
import com.app.usochicamochabackend.tire.application.dto.PerfilLlantasResponse;

import java.util.List;

public interface GestionarPerfilLlantasUseCase {
    List<PerfilLlantasResponse> listarPerfiles();
    PerfilLlantasResponse crearPerfil(PerfilLlantasRequest request);
    PerfilLlantasResponse actualizarPerfil(Long id, PerfilLlantasRequest request);
    void desactivarPerfil(Long id);

    /** Asignaciones vigentes de todos los activos (admin web y caché del móvil). */
    List<ConfigLlantasActivoResponse> listarAsignaciones();
    ConfigLlantasActivoResponse asignarVehiculo(Integer vehicleId, AsignacionLlantasRequest request);
    ConfigLlantasActivoResponse asignarMaquina(Long machineId, AsignacionLlantasRequest request);
}
