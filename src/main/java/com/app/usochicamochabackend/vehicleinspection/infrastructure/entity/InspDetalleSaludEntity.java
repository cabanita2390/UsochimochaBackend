package com.app.usochicamochabackend.vehicleinspection.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tabla: insp_detalle_salud
 * Sección 5 — Salud del Conductor + consciente_responsabilidad del cierre
 * (Sección 6)
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "insp_detalle_salud")
public class InspDetalleSaludEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_salud")
    private Long idSalud;

    @Column(name = "id_inspeccion")
    private Long idInspeccion;

    @Column(name = "salud_fisica")
    private Boolean saludFisica;

    @Column(name = "salud_mental")
    private Boolean saludMental;

    @Column(name = "sobrio")
    private Boolean sobrio;

    @Column(name = "medicamentos")
    private Boolean medicamentos;

    @Column(name = "condicion_para_conducir")
    private Boolean condicionParaConducir;

    /** Campo del cierre (Sección 6): ¿Consciente de la responsabilidad? */
    @Column(name = "consciente_responsabilidad")
    private Boolean conscienteResponsabilidad;
}
