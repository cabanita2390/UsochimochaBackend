package com.app.usochicamochabackend.substation.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Disciplina y lo que hace en el sistema (V53). Es la única fuente: la web y el móvil leen estas
 * características de {@code GET /api/v1/substation/disciplinas} en vez de tenerlas en su código.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_disciplina")
public class DisciplinaEntity {

    public static final String CONJUNTOS_NO = "NO";
    public static final String CONJUNTOS_OPCIONAL = "OPCIONAL";
    public static final String CONJUNTOS_OBLIGATORIO = "OBLIGATORIO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo; // ELECTRICO / ELECTROMECANICO / CIVIL

    /** Nombre visible ("Eléctrico"). NOT NULL en la base; null solo en fixtures de prueba (se usa el código). */
    private String nombre;

    /** Letra de las celdas del cronograma ("E"). NOT NULL en la base, como el nombre. */
    private String sigla;

    @Builder.Default
    @Column(nullable = false)
    private Integer orden = 0;

    /** El móvil permite registrar visitas de esta disciplina. */
    @Builder.Default
    @Column(name = "captura_movil", nullable = false)
    private Boolean capturaMovil = false;

    /** NO / OPCIONAL / OBLIGATORIO: conjuntos en el paso "¿Qué se intervino?". */
    @Builder.Default
    @Column(nullable = false)
    private String conjuntos = CONJUNTOS_NO;

    @Builder.Default
    @Column(name = "usa_elementos", nullable = false)
    private Boolean usaElementos = false;

    @Builder.Default
    @Column(name = "usa_equipos", nullable = false)
    private Boolean usaEquipos = false;

    @Builder.Default
    @Column(name = "usa_controles", nullable = false)
    private Boolean usaControles = false;
}
