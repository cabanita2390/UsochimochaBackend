package com.app.usochicamochabackend.substation.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** "Tipo de conjunto intervenido" */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_conjunto")
public class ConjuntoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "disciplina_id", nullable = false)
    private DisciplinaEntity disciplina;

    /** Código del ícono que muestra el móvil (ver ItemCatalogoRequest.ICONOS); null = genérico. */
    @Column(length = 30)
    private String icono;

    @Builder.Default
    private Boolean status = true;
}
