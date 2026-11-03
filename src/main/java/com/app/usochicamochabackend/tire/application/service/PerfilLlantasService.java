package com.app.usochicamochabackend.tire.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.tire.application.dto.*;
import com.app.usochicamochabackend.tire.application.port.GestionarPerfilLlantasUseCase;
import com.app.usochicamochabackend.tire.infrastructure.entity.AssetLlantaConfigEntity;
import com.app.usochicamochabackend.tire.infrastructure.entity.LlantaPerfilEntity;
import com.app.usochicamochabackend.tire.infrastructure.entity.LlantaPerfilPosicionEntity;
import com.app.usochicamochabackend.tire.infrastructure.repository.AssetLlantaConfigRepository;
import com.app.usochicamochabackend.tire.infrastructure.repository.LlantaPerfilRepository;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PerfilLlantasService implements GestionarPerfilLlantasUseCase {

    static final Set<String> APLICA_A_VALIDOS = Set.of("VEHICULO", "MOTOCICLETA", "MAQUINARIA");
    private static final Pattern CODIGO_VALIDO = Pattern.compile("[A-Z0-9_]{1,30}");
    // NUMERIC(5,1) en BD; ninguna llanta del parque (incluidas volquetas) se acerca a este tope.
    private static final BigDecimal PSI_TOPE = new BigDecimal("300");

    private final LlantaPerfilRepository perfilRepository;
    private final AssetLlantaConfigRepository assetConfigRepository;
    private final VehicleRepository vehicleRepository;
    private final MachineRepository machineRepository;
    private final SaveActionUseCase saveActionUseCase;

    @Override
    @Transactional(readOnly = true)
    public List<PerfilLlantasResponse> listarPerfiles() {
        return perfilRepository.findByStatusTrueOrderByNombreAsc().stream().map(PerfilLlantasService::toResponse).toList();
    }

    @Override
    @Transactional
    public PerfilLlantasResponse crearPerfil(PerfilLlantasRequest request) {
        String nombre = validarCabecera(request);
        if (perfilRepository.existsByNombreIgnoreCaseAndStatusTrue(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un perfil de llantas llamado '" + nombre + "'.");
        }
        List<PosicionLlantaRequest> posiciones = validarPosiciones(request.posiciones());

        LocalDateTime ahora = LocalDateTime.now();
        LlantaPerfilEntity perfil = LlantaPerfilEntity.builder()
                .nombre(nombre)
                .aplicaA(request.aplicaA().trim().toUpperCase(Locale.ROOT))
                .descripcion(blankToNull(request.descripcion()))
                .creadoEn(ahora)
                .actualizadoEn(ahora)
                .build();
        for (int i = 0; i < posiciones.size(); i++) {
            LlantaPerfilPosicionEntity p = new LlantaPerfilPosicionEntity();
            p.setPerfil(perfil);
            aplicarPosicion(p, posiciones.get(i), i);
            perfil.getPosiciones().add(p);
        }
        perfil = perfilRepository.save(perfil);

        saveActionUseCase.save("Se creó el perfil de llantas '" + perfil.getNombre() + "' (" + posiciones.size() + " llantas)");
        return toResponse(perfil);
    }

    @Override
    @Transactional
    public PerfilLlantasResponse actualizarPerfil(Long id, PerfilLlantasRequest request) {
        LlantaPerfilEntity perfil = perfilRepository.findByIdAndStatusTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el perfil de llantas con id=" + id));
        String nombre = validarCabecera(request);
        if (perfilRepository.existsByNombreIgnoreCaseAndStatusTrueAndIdNot(nombre, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un perfil de llantas llamado '" + nombre + "'.");
        }
        String aplicaA = request.aplicaA().trim().toUpperCase(Locale.ROOT);
        if (!aplicaA.equals(perfil.getAplicaA()) && assetConfigRepository.existsByPerfilIdAndStatusTrue(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El perfil ya está asignado a activos de tipo " + perfil.getAplicaA() + "; no se puede cambiar a " + aplicaA + ".");
        }
        List<PosicionLlantaRequest> posiciones = validarPosiciones(request.posiciones());

        perfil.setNombre(nombre);
        perfil.setAplicaA(aplicaA);
        perfil.setDescripcion(blankToNull(request.descripcion()));
        perfil.setActualizadoEn(LocalDateTime.now());

        // Las posiciones se casan por código: las que siguen se actualizan, las nuevas se agregan
        // y las que ya no vienen se desactivan (las lecturas históricas guardan su propia copia).
        Map<String, LlantaPerfilPosicionEntity> vigentes = new HashMap<>();
        for (LlantaPerfilPosicionEntity p : perfil.getPosiciones()) {
            if (Boolean.TRUE.equals(p.getStatus())) {
                vigentes.put(p.getCodigo(), p);
            }
        }
        for (int i = 0; i < posiciones.size(); i++) {
            PosicionLlantaRequest req = posiciones.get(i);
            LlantaPerfilPosicionEntity p = vigentes.remove(req.codigo());
            if (p == null) {
                p = new LlantaPerfilPosicionEntity();
                p.setPerfil(perfil);
                perfil.getPosiciones().add(p);
            }
            aplicarPosicion(p, req, i);
        }
        vigentes.values().forEach(p -> p.setStatus(false));
        perfil = perfilRepository.save(perfil);

        saveActionUseCase.save("Se editó el perfil de llantas '" + perfil.getNombre() + "'");
        return toResponse(perfil);
    }

    @Override
    @Transactional
    public void desactivarPerfil(Long id) {
        LlantaPerfilEntity perfil = perfilRepository.findByIdAndStatusTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el perfil de llantas con id=" + id));
        if (assetConfigRepository.existsByPerfilIdAndStatusTrue(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El perfil está asignado a uno o más activos; quíteselo antes de eliminarlo.");
        }
        perfil.setStatus(false);
        perfil.setActualizadoEn(LocalDateTime.now());
        perfilRepository.save(perfil);
        saveActionUseCase.save("Se eliminó el perfil de llantas '" + perfil.getNombre() + "'");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConfigLlantasActivoResponse> listarAsignaciones() {
        return assetConfigRepository.findAllVigentes().stream().map(PerfilLlantasService::toConfigResponse).toList();
    }

    @Override
    @Transactional
    public ConfigLlantasActivoResponse asignarVehiculo(Integer vehicleId, AsignacionLlantasRequest request) {
        VehicleEntity vehiculo = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el vehículo con id=" + vehicleId));
        String tipo = vehiculo.getTipoVehiculo() != null && vehiculo.getTipoVehiculo().getNombreTipo() != null
                ? vehiculo.getTipoVehiculo().getNombreTipo().toUpperCase(Locale.ROOT)
                : "";
        // Mismo criterio que VehiculoInspectionService para distinguir motos de vehículos.
        String aplicaA = tipo.contains("MOTO") ? "MOTOCICLETA" : "VEHICULO";

        AssetLlantaConfigEntity config = assetConfigRepository.findByVehicleId(vehicleId)
                .orElseGet(() -> AssetLlantaConfigEntity.builder().vehicleId(vehicleId).build());
        return asignar(config, request, aplicaA, "del vehículo " + vehiculo.getPlaca());
    }

    @Override
    @Transactional
    public ConfigLlantasActivoResponse asignarMaquina(Long machineId, AsignacionLlantasRequest request) {
        var maquina = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la máquina con id=" + machineId));
        AssetLlantaConfigEntity config = assetConfigRepository.findByMachineId(machineId)
                .orElseGet(() -> AssetLlantaConfigEntity.builder().machineId(machineId).build());
        return asignar(config, request, "MAQUINARIA", "de la máquina " + maquina.getName());
    }

    /** Devuelve null cuando la asignación queda quitada. */
    private ConfigLlantasActivoResponse asignar(AssetLlantaConfigEntity config, AsignacionLlantasRequest request,
                                                String aplicaAEsperado, String descripcionActivo) {
        Long perfilId = request != null ? request.perfilId() : null;
        if (perfilId == null) {
            if (config.getId() != null && Boolean.TRUE.equals(config.getStatus())) {
                config.setStatus(false);
                assetConfigRepository.save(config);
                saveActionUseCase.save("Se quitó el perfil de llantas " + descripcionActivo);
            }
            return null;
        }
        LlantaPerfilEntity perfil = perfilRepository.findByIdAndStatusTrue(perfilId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el perfil de llantas con id=" + perfilId));
        if (!aplicaAEsperado.equals(perfil.getAplicaA())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El perfil '" + perfil.getNombre() + "' es para " + perfil.getAplicaA() + " y el activo es " + aplicaAEsperado + ".");
        }
        config.setPerfil(perfil);
        config.setStatus(true);
        config = assetConfigRepository.save(config);
        saveActionUseCase.save("Se asignó el perfil de llantas '" + perfil.getNombre() + "' " + descripcionActivo);
        return toConfigResponse(config);
    }

    private static String validarCabecera(PerfilLlantasRequest request) {
        if (request == null || request.nombre() == null || request.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del perfil es obligatorio.");
        }
        String nombre = request.nombre().trim();
        if (nombre.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del perfil admite máximo 100 caracteres.");
        }
        if (request.aplicaA() == null || !APLICA_A_VALIDOS.contains(request.aplicaA().trim().toUpperCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "aplicaA debe ser uno de " + APLICA_A_VALIDOS + ".");
        }
        return nombre;
    }

    /** Valida y normaliza (código en mayúsculas, etiqueta sin espacios sobrantes). */
    private static List<PosicionLlantaRequest> validarPosiciones(List<PosicionLlantaRequest> posiciones) {
        if (posiciones == null || posiciones.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El perfil debe tener al menos una llanta.");
        }
        Set<String> codigos = new HashSet<>();
        List<PosicionLlantaRequest> normalizadas = new ArrayList<>();
        for (PosicionLlantaRequest p : posiciones) {
            if (p == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay una llanta vacía en el perfil.");
            }
            String codigo = p.codigo() == null ? "" : p.codigo().trim().toUpperCase(Locale.ROOT);
            if (!CODIGO_VALIDO.matcher(codigo).matches()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Código de llanta inválido '" + p.codigo() + "': use letras, números o _ (máx. 30).");
            }
            if (!codigos.add(codigo)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código de llanta '" + codigo + "' está repetido.");
            }
            if (p.etiqueta() == null || p.etiqueta().isBlank() || p.etiqueta().trim().length() > 80) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La llanta " + codigo + " necesita un nombre (máx. 80 caracteres).");
            }
            if (p.psiMin() == null || p.psiMax() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La llanta " + codigo + " necesita presión mínima y máxima.");
            }
            if (p.psiMin().signum() <= 0 || p.psiMax().compareTo(PSI_TOPE) > 0 || p.psiMin().compareTo(p.psiMax()) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Rango inválido en la llanta " + codigo + ": debe cumplir 0 < mínimo ≤ máximo ≤ " + PSI_TOPE + " PSI.");
            }
            if (p.psiObjetivo() != null
                    && (p.psiObjetivo().compareTo(p.psiMin()) < 0 || p.psiObjetivo().compareTo(p.psiMax()) > 0)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La presión recomendada de la llanta " + codigo + " debe estar entre el mínimo y el máximo.");
            }
            normalizadas.add(new PosicionLlantaRequest(codigo, p.etiqueta().trim(), p.orden(),
                    p.psiMin(), p.psiObjetivo(), p.psiMax()));
        }
        return normalizadas;
    }

    private static void aplicarPosicion(LlantaPerfilPosicionEntity p, PosicionLlantaRequest req, int indice) {
        p.setCodigo(req.codigo());
        p.setEtiqueta(req.etiqueta());
        p.setOrden(req.orden() != null ? req.orden() : indice + 1);
        p.setPsiMin(req.psiMin());
        p.setPsiObjetivo(req.psiObjetivo());
        p.setPsiMax(req.psiMax());
        p.setStatus(true);
    }

    static List<PosicionLlantaResponse> posicionesVigentes(LlantaPerfilEntity perfil) {
        return perfil.getPosiciones().stream()
                .filter(p -> Boolean.TRUE.equals(p.getStatus()))
                .sorted(Comparator.comparing(LlantaPerfilPosicionEntity::getOrden))
                .map(p -> new PosicionLlantaResponse(p.getCodigo(), p.getEtiqueta(), p.getOrden(),
                        p.getPsiMin(), p.getPsiObjetivo(), p.getPsiMax()))
                .toList();
    }

    private static PerfilLlantasResponse toResponse(LlantaPerfilEntity perfil) {
        return new PerfilLlantasResponse(perfil.getId(), perfil.getNombre(), perfil.getAplicaA(),
                perfil.getDescripcion(), posicionesVigentes(perfil));
    }

    private static ConfigLlantasActivoResponse toConfigResponse(AssetLlantaConfigEntity config) {
        LlantaPerfilEntity perfil = config.getPerfil();
        return new ConfigLlantasActivoResponse(config.getVehicleId(), config.getMachineId(), perfil.getId(),
                perfil.getNombre(), perfil.getAplicaA(), posicionesVigentes(perfil));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
