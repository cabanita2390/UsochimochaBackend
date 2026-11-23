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
import lombok.NoArgsConstructor;

/**
 * Un elemento dentro de un conjunto (V54): el conjunto es la caja y estos son su contenido. El
 * nombre del elemento ({@link ElementoEntity}) se reutiliza entre conjuntos.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_conjunto_elemento")
public class ConjuntoElementoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conjunto_id", nullable = false)
    private ConjuntoEntity conjunto;

    @ManyToOne
    @JoinColumn(name = "elemento_id", nullable = false)
    private ElementoEntity elemento;

    /** Al marcarlo el operario escribe algo (ej. "Estado físico": cómo se encuentra). */
    @Builder.Default
    @Column(name = "pide_detalle", nullable = false)
    private Boolean pideDetalle = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer orden = 0;

    /** Lo agregó un operario con "Otro" desde el móvil; el administrador lo revisa. */
    @Builder.Default
    @Column(nullable = false)
    private Boolean provisional = false;

    /** false = se quitó del conjunto (nunca se borra: las visitas viejas lo siguen usando). */
    @Builder.Default
    @Column(nullable = false)
    private Boolean status = true;
}
