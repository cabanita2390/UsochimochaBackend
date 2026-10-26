package com.app.usochicamochabackend.tire.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tabla: asset_llanta_config
 * Perfil de llantas asignado a un activo: exactamente uno de vehicleId / machineId.
 * Quitar el perfil desactiva la fila (status = false), no la borra.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "asset_llanta_config")
public class AssetLlantaConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", unique = true)
    private Integer vehicleId;

    @Column(name = "machine_id", unique = true)
    private Long machineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    private LlantaPerfilEntity perfil;

    @Builder.Default
    @Column(nullable = false)
    private Boolean status = true;
}
