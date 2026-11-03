package com.app.usochicamochabackend.substation.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * "Elementos inspeccionados o intervenidos" del formulario eléctrico (Breaker, Variador…).
 * Lista genérica por disciplina, igual para todas las estaciones.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_elemento")
public class ElementoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "disciplina_id", nullable = false)
    private DisciplinaEntity disciplina;

    /** De V29: filas sacadas del texto libre histórico sin depurar. Las que se crean desde la web no lo son. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean provisional = false;

    @Builder.Default
    private Boolean status = true;
}
