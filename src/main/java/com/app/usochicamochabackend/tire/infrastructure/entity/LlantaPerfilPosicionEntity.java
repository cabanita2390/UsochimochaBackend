package com.app.usochicamochabackend.tire.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Tabla: llanta_perfil_posicion
 * Una llanta del perfil (p. ej. DEL "Delantera", T1_IZQ_EXT "Eje 1 izq. exterior") con su rango en PSI.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "llanta_perfil_posicion")
public class LlantaPerfilPosicionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id", nullable = false)
    private LlantaPerfilEntity perfil;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String etiqueta;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "psi_min", nullable = false, precision = 5, scale = 1)
    private BigDecimal psiMin;

    @Column(name = "psi_objetivo", precision = 5, scale = 1)
    private BigDecimal psiObjetivo;

    @Column(name = "psi_max", nullable = false, precision = 5, scale = 1)
    private BigDecimal psiMax;

    @Builder.Default
    @Column(nullable = false)
    private Boolean status = true;
}
