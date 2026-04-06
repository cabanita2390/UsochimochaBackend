package com.app.usochicamochabackend.vehicleinspection.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla: insp_detalle_mecanico
 * Sección 2 — Inspección Mecánica (Bueno / Regular / Malo)
 */
@Data
@Entity
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

    @Column(name = "nivel_refrigerante", length = 20)
    private String nivelRefrigerante;

    @Column(name = "nivel_frenos", length = 20)
    private String nivelFrenos;

    @Column(name = "estado_llantas", length = 20)
    private String estadoLlantas;

    @Column(name = "luces_general", length = 20)
    private String lucesGeneral;

    @Column(name = "estado_visual", length = 20)
    private String estadoVisual;

    @Column(name = "limpieza_general", length = 20)
    private String limpiezaGeneral;
}
