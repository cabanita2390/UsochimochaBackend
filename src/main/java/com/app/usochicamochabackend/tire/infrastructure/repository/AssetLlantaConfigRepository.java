package com.app.usochicamochabackend.tire.infrastructure.repository;

import com.app.usochicamochabackend.tire.infrastructure.entity.AssetLlantaConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AssetLlantaConfigRepository extends JpaRepository<AssetLlantaConfigEntity, Long> {
    Optional<AssetLlantaConfigEntity> findByVehicleId(Integer vehicleId);
    Optional<AssetLlantaConfigEntity> findByMachineId(Long machineId);
    boolean existsByPerfilIdAndStatusTrue(Long perfilId);

    /** Asignaciones activas cuyo perfil también está activo (lo que descarga el móvil). */
    @Query("SELECT c FROM AssetLlantaConfigEntity c JOIN FETCH c.perfil p WHERE c.status = true AND p.status = true")
    List<AssetLlantaConfigEntity> findAllVigentes();
}
