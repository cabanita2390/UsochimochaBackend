package com.app.usochicamochabackend.tire.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla: llanta_perfil
 * Plantilla de llantas reutilizable (posiciones + rango de presión en PSI) que se asigna a
 * vehículos, motos o máquinas.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "llanta_perfil")
public class LlantaPerfilEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    /** VEHICULO | MOTOCICLETA | MAQUINARIA */
    @Column(name = "aplica_a", nullable = false, length = 15)
    private String aplicaA;

    private String descripcion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean status = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL)
    @OrderBy("orden ASC")
    private List<LlantaPerfilPosicionEntity> posiciones = new ArrayList<>();
}
