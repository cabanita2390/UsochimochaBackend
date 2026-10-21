package com.app.usochicamochabackend.substation.infrastructure.entity;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Orden de trabajo de estación (OT-EB-00001) para corregir un hallazgo.
 * Ciclo: ASIGNADA → POR_VERIFICAR → CERRADA; POR_VERIFICAR → ASIGNADA (devuelta); ANULADA.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_orden_trabajo")
public class OrdenTrabajoEntity {

    public static final String ASIGNADA = "ASIGNADA";
    public static final String POR_VERIFICAR = "POR_VERIFICAR";
    public static final String CERRADA = "CERRADA";
    public static final String ANULADA = "ANULADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String consecutivo;

    // Las dos ejecuciones apuntan de vuelta a la orden (mant_ejecucion.orden_id): fuera de
    // equals/hashCode/toString para no entrar en un ciclo.
    @ManyToOne
    @JoinColumn(name = "ejecucion_origen_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EjecucionEntity ejecucionOrigen; // la visita en que se encontró el hallazgo

    @ManyToOne
    @JoinColumn(name = "estacion_id", nullable = false)
    private EstacionEntity estacion;

    @ManyToOne
    @JoinColumn(name = "disciplina_id", nullable = false)
    private DisciplinaEntity disciplina;

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    private ActividadEntity actividad;

    @Column(nullable = false, length = 10)
    private String prioridad; // ALTA / MEDIA (CHECK en BD)

    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @ManyToOne
    @JoinColumn(name = "asignado_a")
    private UserEntity asignadoA; // null = para cualquier operario, hasta que alguno la ejecuta

    @Column(nullable = false, columnDefinition = "TEXT")
    private String instrucciones;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = ASIGNADA;

    @ManyToOne
    @JoinColumn(name = "ejecucion_cierre_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EjecucionEntity ejecucionCierre; // la visita del operario que la corrigió

    @Column(name = "observacion_cierre", columnDefinition = "TEXT")
    private String observacionCierre;

    @ManyToOne
    @JoinColumn(name = "verificado_por")
    private UserEntity verificadoPor;

    @Column(name = "verificado_en")
    private LocalDateTime verificadoEn;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @ManyToOne
    @JoinColumn(name = "creado_por", nullable = false)
    private UserEntity creadoPor;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @Builder.Default
    private Boolean status = true;

    /** Sigue abierta: el operario o el supervisor todavía tienen algo que hacer. */
    public boolean enCurso() {
        return ASIGNADA.equals(estado) || POR_VERIFICAR.equals(estado);
    }
}
