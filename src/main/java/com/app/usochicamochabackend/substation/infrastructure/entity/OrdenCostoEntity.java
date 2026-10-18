package com.app.usochicamochabackend.substation.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/** Costo de una orden de trabajo: mano de obra (propia o contratista) o material. */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_orden_costo")
public class OrdenCostoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orden_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdenTrabajoEntity orden;

    @Column(nullable = false, length = 10)
    private String tipo; // MANO_OBRA / MATERIAL (CHECK en BD)

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(length = 200)
    private String proveedor; // contratista o proveedor del material

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "valor_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTotal;
}
