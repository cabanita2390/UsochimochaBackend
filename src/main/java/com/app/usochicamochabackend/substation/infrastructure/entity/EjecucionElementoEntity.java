package com.app.usochicamochabackend.substation.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Un elemento marcado en una visita, con el conjunto donde se trabajó (V54). "Fusibles" del
 * Transformador y "Fusibles" de la Subestación son dos filas distintas.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_ejecucion_elemento")
public class EjecucionElementoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ejecucion_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EjecucionEntity ejecucion;

    /** null solo en visitas anteriores a V54 ("sin conjunto"). */
    @ManyToOne
    @JoinColumn(name = "conjunto_id")
    private ConjuntoEntity conjunto;

    @ManyToOne
    @JoinColumn(name = "elemento_id", nullable = false)
    private ElementoEntity elemento;

    /** Lo que escribió el operario en un elemento que lo pide ("Estado físico"); null si no aplica. */
    @Column(length = 500)
    private String detalle;
}
