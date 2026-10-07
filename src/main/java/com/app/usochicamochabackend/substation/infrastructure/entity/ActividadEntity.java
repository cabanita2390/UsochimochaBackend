package com.app.usochicamochabackend.substation.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_actividad")
public class ActividadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "disciplina_id", nullable = false)
    private DisciplinaEntity disciplina;

    /** Para los chips de la grilla del Cronograma; null → la web recorta el nombre. */
    @Column(name = "nombre_corto", length = 24)
    private String nombreCorto;

    @Column(name = "captura_movil_habilitada", nullable = false)
    @Builder.Default
    private Boolean capturaMovilHabilitada = false;

    @Builder.Default
    private Boolean status = true;
}
