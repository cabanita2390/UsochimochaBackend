package com.app.usochicamochabackend.vehicleinspection.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla: insp_detalle_elementos
 * Sección 4 — Elementos de Seguridad (Si = true / No = false)
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "insp_detalle_elementos")
public class InspDetalleElementosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_elementos")
    private Long idElementos;

    @Column(name = "id_inspeccion")
    private Long idInspeccion;

    @Column(name = "tiene_botiquin")
    private Boolean tieneBotiquin;

    /** Nota: la columna en BD se llama tiene_senalizacion (sin ñ). */
    @Column(name = "tiene_senalizacion")
    private Boolean tieneSeñalizacion;

    /**
     * La BD no tiene columna separada para líneas de emergencia —
     * se reutiliza tiene_extintor para mapear tieneLineasEmergencia
     * hasta que se agregue la columna definitiva.
     * Ajustar cuando se modifique el esquema.
     */
    @Column(name = "tiene_extintor")
    private Boolean tieneLineasEmergencia;

    @Column(name = "tiene_llanta_repuesto", length = 50)
    private String tieneLlantaRepuesto;

    @Column(name = "tiene_gato_hidraulico", length = 50)
    private String tieneGatoHidraulico;
}
