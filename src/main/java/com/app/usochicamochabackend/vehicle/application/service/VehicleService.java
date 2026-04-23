package com.app.usochicamochabackend.vehicle.application.service;

import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.vehicle.application.dto.*;
import com.app.usochicamochabackend.vehicle.application.port.FindAllVehiclesUseCase;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.MarcaModeloEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.ReferenciaAceiteEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.MarcaModeloRepository;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.ReferenciaAceiteRepository;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.VehicleRepository;
import com.app.usochicamochabackend.moto.infrastructure.entity.TipoVehiculoEntity;
import com.app.usochicamochabackend.moto.infrastructure.entity.UbicacionEntity;
import com.app.usochicamochabackend.moto.infrastructure.repository.TipoVehiculoRepository;
import com.app.usochicamochabackend.moto.infrastructure.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService implements FindAllVehiclesUseCase {

    private final VehicleRepository vehicleRepository;
    private final MarcaModeloRepository marcaModeloRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final ReferenciaAceiteRepository referenciaAceiteRepository;

    /* ───────── READ ───────── */

    @Override
    public List<VehicleResponse> findAllVehicles() {
        return vehicleRepository.findAllActiveVehicles().stream()
                .map(v -> new VehicleResponse(v.getId(), v.getPlaca(), v.getMarca(), v.getTipoVehiculo(), v.getKilometrajeActual()))
                .toList();
    }

    @Override
    public VehicleResponse findByPlaca(String placa) {
        return vehicleRepository.findVehicleDetailByPlaca(placa)
                .map(v -> new VehicleResponse(v.getId(), v.getPlaca(), v.getMarca(), v.getTipoVehiculo(), v.getKilometrajeActual()))
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado con placa: " + placa));
    }

    /** Retorna todos los vehículos (activos e inactivos) para el panel admin */
    public List<VehicleFullResponse> findAllVehiclesFull() {
        return vehicleRepository.findAll().stream()
                .map(v -> {
                    String marcaDesc = v.getMarca() != null ? v.getMarca().getDescripcion() : "N/A";
                    String tipoDesc  = v.getTipoVehiculo() != null ? v.getTipoVehiculo().getNombreTipo() : "N/A";
                    return new VehicleFullResponse(
                            v.getIdVehiculo(), v.getPlaca(),
                            v.getIdMarca(), marcaDesc,
                            v.getIdTipoVehiculo(), tipoDesc,
                            v.getKilometrajeActual(), v.getActivo()
                    );
                })
                .toList();
    }

    /* ───────── CREATE ───────── */

    @Transactional
    public VehicleFullResponse createVehicle(VehicleRequest req) {
        VehicleEntity entity = new VehicleEntity();
        entity.setPlaca(req.placa());
        entity.setIdMarca(req.idMarca());
        entity.setIdTipoVehiculo(req.idTipoVehiculo());
        entity.setKilometrajeActual(req.kilometrajeActual() != null ? req.kilometrajeActual() : 0);
        entity.setActivo(req.activo() != null ? req.activo() : true);
        VehicleEntity saved = vehicleRepository.save(entity);
        return toFullResponse(saved);
    }

    /* ───────── UPDATE ───────── */

    @Transactional
    public VehicleFullResponse updateVehicle(Long id, VehicleRequest req) {
        VehicleEntity entity = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado: " + id));
        entity.setPlaca(req.placa());
        entity.setIdMarca(req.idMarca());
        entity.setIdTipoVehiculo(req.idTipoVehiculo());
        if (req.kilometrajeActual() != null) entity.setKilometrajeActual(req.kilometrajeActual());
        if (req.activo() != null) entity.setActivo(req.activo());
        VehicleEntity saved = vehicleRepository.save(entity);
        return toFullResponse(saved);
    }

    /* ───────── DELETE (soft) ───────── */

    @Transactional
    public void deleteVehicle(Long id) {
        VehicleEntity entity = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado: " + id));
        entity.setActivo(false);
        vehicleRepository.save(entity);
    }

    /* ─────── CATÁLOGOS ─────── */

    // Marcas / Modelos
    public List<CatalogoResponse> findAllMarcas() {
        return marcaModeloRepository.findAll().stream()
                .map(m -> new CatalogoResponse(m.getIdMarca(), m.getDescripcion(), true))
                .toList();
    }

    @Transactional
    public CatalogoResponse createMarca(CatalogoRequest req) {
        MarcaModeloEntity entity = new MarcaModeloEntity();
        entity.setDescripcion(req.descripcion());
        MarcaModeloEntity saved = marcaModeloRepository.save(entity);
        return new CatalogoResponse(saved.getIdMarca(), saved.getDescripcion(), true);
    }

    @Transactional
    public CatalogoResponse updateMarca(Integer id, CatalogoRequest req) {
        MarcaModeloEntity entity = marcaModeloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada: " + id));
        entity.setDescripcion(req.descripcion());
        MarcaModeloEntity saved = marcaModeloRepository.save(entity);
        return new CatalogoResponse(saved.getIdMarca(), saved.getDescripcion(), true);
    }

    @Transactional
    public void deleteMarca(Integer id) {
        marcaModeloRepository.deleteById(id);
    }

    // Tipos de Vehículo
    public List<CatalogoResponse> findAllTipos() {
        return tipoVehiculoRepository.findAll().stream()
                .map(t -> new CatalogoResponse(t.getId(), t.getNombreTipo(), t.getActivo()))
                .toList();
    }

    @Transactional
    public CatalogoResponse createTipo(CatalogoRequest req) {
        TipoVehiculoEntity entity = new TipoVehiculoEntity();
        entity.setNombreTipo(req.descripcion());
        entity.setActivo(true);
        TipoVehiculoEntity saved = tipoVehiculoRepository.save(entity);
        return new CatalogoResponse(saved.getId(), saved.getNombreTipo(), saved.getActivo());
    }

    @Transactional
    public CatalogoResponse updateTipo(Integer id, CatalogoRequest req) {
        TipoVehiculoEntity entity = tipoVehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo no encontrado: " + id));
        entity.setNombreTipo(req.descripcion());
        TipoVehiculoEntity saved = tipoVehiculoRepository.save(entity);
        return new CatalogoResponse(saved.getId(), saved.getNombreTipo(), saved.getActivo());
    }

    @Transactional
    public void deleteTipo(Integer id) {
        TipoVehiculoEntity entity = tipoVehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo no encontrado: " + id));
        entity.setActivo(false);
        tipoVehiculoRepository.save(entity);
    }

    // Ubicaciones
    public List<CatalogoResponse> findAllUbicaciones() {
        return ubicacionRepository.findAll().stream()
                .map(u -> new CatalogoResponse(u.getId(), u.getNombreUbicacion(), u.getActivo()))
                .toList();
    }

    @Transactional
    public CatalogoResponse createUbicacion(CatalogoRequest req) {
        UbicacionEntity entity = new UbicacionEntity();
        entity.setNombreUbicacion(req.descripcion());
        entity.setActivo(true);
        UbicacionEntity saved = ubicacionRepository.save(entity);
        return new CatalogoResponse(saved.getId(), saved.getNombreUbicacion(), saved.getActivo());
    }

    @Transactional
    public CatalogoResponse updateUbicacion(Integer id, CatalogoRequest req) {
        UbicacionEntity entity = ubicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada: " + id));
        entity.setNombreUbicacion(req.descripcion());
        UbicacionEntity saved = ubicacionRepository.save(entity);
        return new CatalogoResponse(saved.getId(), saved.getNombreUbicacion(), saved.getActivo());
    }

    @Transactional
    public void deleteUbicacion(Integer id) {
        UbicacionEntity entity = ubicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada: " + id));
        entity.setActivo(false);
        ubicacionRepository.save(entity);
    }

    // Referencias de Aceite
    public List<CatalogoResponse> findAllAceites() {
        return referenciaAceiteRepository.findAll().stream()
                .map(a -> new CatalogoResponse(a.getIdAceite(), a.getDescripcion(), a.getActivo()))
                .toList();
    }

    @Transactional
    public CatalogoResponse createAceite(CatalogoRequest req) {
        ReferenciaAceiteEntity entity = new ReferenciaAceiteEntity();
        entity.setDescripcion(req.descripcion());
        entity.setActivo(true);
        ReferenciaAceiteEntity saved = referenciaAceiteRepository.save(entity);
        return new CatalogoResponse(saved.getIdAceite(), saved.getDescripcion(), saved.getActivo());
    }

    @Transactional
    public CatalogoResponse updateAceite(Integer id, CatalogoRequest req) {
        ReferenciaAceiteEntity entity = referenciaAceiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia de aceite no encontrada: " + id));
        entity.setDescripcion(req.descripcion());
        ReferenciaAceiteEntity saved = referenciaAceiteRepository.save(entity);
        return new CatalogoResponse(saved.getIdAceite(), saved.getDescripcion(), saved.getActivo());
    }

    @Transactional
    public void deleteAceite(Integer id) {
        ReferenciaAceiteEntity entity = referenciaAceiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referencia de aceite no encontrada: " + id));
        entity.setActivo(false);
        referenciaAceiteRepository.save(entity);
    }

    /* ─────── HELPERS ─────── */

    private VehicleFullResponse toFullResponse(VehicleEntity v) {
        String marcaDesc = "N/A";
        String tipoDesc  = "N/A";
        if (v.getIdMarca() != null) {
            marcaDesc = marcaModeloRepository.findById(v.getIdMarca())
                    .map(MarcaModeloEntity::getDescripcion).orElse("N/A");
        }
        if (v.getIdTipoVehiculo() != null) {
            tipoDesc = tipoVehiculoRepository.findById(v.getIdTipoVehiculo())
                    .map(TipoVehiculoEntity::getNombreTipo).orElse("N/A");
        }
        return new VehicleFullResponse(
                v.getIdVehiculo(), v.getPlaca(),
                v.getIdMarca(), marcaDesc,
                v.getIdTipoVehiculo(), tipoDesc,
                v.getKilometrajeActual(), v.getActivo()
        );
    }
}
