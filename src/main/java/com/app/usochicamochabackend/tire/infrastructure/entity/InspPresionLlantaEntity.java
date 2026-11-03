package com.app.usochicamochabackend.tire.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tabla: insp_presion_llanta
 * Lectura de presión de una llanta en una inspección. Pertenece a una inspección de vehículo/moto
 * ({@code inspeccion_pre_operativa}) o de máquina ({@code inspections}). La etiqueta y el rango se
 * copian del perfil al registrar, para que el historial no cambie si luego se edita el perfil.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "insp_presion_llanta")
public class InspPresionLlantaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_inspeccion_vehiculo")
    private Long idInspeccionVehiculo;

    @Column(name = "id_inspeccion_maquina")
    private Long idInspeccionMaquina;

    @Column(name = "codigo_posicion", nullable = false, length = 30)
    private String codigoPosicion;

    @Column(name = "etiqueta_posicion", nullable = false, length = 80)
    private String etiquetaPosicion;

    private Integer orden;

    @Column(name = "psi_medido", nullable = false, precision = 5, scale = 1)
    private BigDecimal psiMedido;

    @Column(name = "psi_min", precision = 5, scale = 1)
    private BigDecimal psiMin;

    @Column(name = "psi_max", precision = 5, scale = 1)
    private BigDecimal psiMax;

    /** OK | BAJA | ALTA | SIN_RANGO */
    @Column(nullable = false, length = 10)
    private String resultado;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
