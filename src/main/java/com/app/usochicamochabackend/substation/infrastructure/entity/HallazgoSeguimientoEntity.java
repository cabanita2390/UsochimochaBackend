package com.app.usochicamochabackend.substation.infrastructure.entity;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Seguimiento de una ejecución con hallazgo. Ciclo: ABIERTO → EN_PROCESO → RESUELTO. */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_hallazgo_seguimiento")
public class HallazgoSeguimientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ejecucion_id", nullable = false, unique = true)
    private EjecucionEntity ejecucion;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "ABIERTO"; // ABIERTO / EN_PROCESO / RESUELTO (CHECK en BD)

    @ManyToOne
    @JoinColumn(name = "resuelto_en_ejecucion_id")
    private EjecucionEntity resueltoEnEjecucion; // ejecución posterior de la misma estación

    @Column(name = "resuelto_misma_visita", nullable = false)
    @Builder.Default
    private Boolean resueltoMismaVisita = false;

    @Column(name = "observaciones_cierre", columnDefinition = "TEXT")
    private String observacionesCierre;

    @ManyToOne
    @JoinColumn(name = "cerrado_por")
    private UserEntity cerradoPor;

    @Column(name = "cerrado_en")
    private LocalDateTime cerradoEn;

    @ManyToOne
    @JoinColumn(name = "actualizado_por")
    private UserEntity actualizadoPor;

    @Column(name = "actualizado_en", nullable = false)
    @Builder.Default
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    @Builder.Default
    private Boolean status = true; // false si la ejecución pasó a CONFORME
}
