package com.app.usochicamochabackend.moto.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla: insp_detalle_mecanico
 * Sección para motos que se guarda en la tabla compartida de detalle mecánico.
 */
@Data
@Entity(name = "MotoInspDetalleMecanicoEntity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "insp_detalle_mecanico")
public class InspDetalleMecanicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mecanico")
    private Long idMecanico;

    @Column(name = "id_inspeccion")
    private Long idInspeccion;

    @Column(name = "nivel_aceite", length = 20)
    private String nivelAceite;

    @Column(name = "estado_llantas", length = 20)
    private String estadoLlantas;

    @Column(name = "luces_general", length = 20)
    private String lucesGeneral;

    @Column(name = "estado_visual", length = 20)
    private String estadoVisual;
}
