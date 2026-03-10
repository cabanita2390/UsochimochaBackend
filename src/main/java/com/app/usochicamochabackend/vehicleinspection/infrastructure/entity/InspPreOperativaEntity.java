package com.app.usochicamochabackend.vehicleinspection.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tabla: Inspección_pre_operativa
 * Cabecera de la inspección pre-operativa de vehículos.
 * Las tablas detalle se persisten en el servicio después de obtener el id
 * generado.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inspeccion_pre_operativa")
public class InspPreOperativaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inspeccion")
    private Long idInspeccion;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    @Column(name = "login_user", nullable = false, length = 100)
    private String loginUser;

    @Column(name = "kilometraje_reportado", nullable = false)
    private Integer kilometrajeReportado;

    @Column(name = "aprobado_ruta")
    private Boolean aprobadoRuta;

    @Column(name = "observaciones_finales")
    private String observacionesFinales;
}
