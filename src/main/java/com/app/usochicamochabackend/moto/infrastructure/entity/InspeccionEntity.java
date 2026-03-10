package com.app.usochicamochabackend.moto.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inspeccion_pre_operativa")
public class InspeccionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inspeccion")
    private Long id;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehiculo")
    private VehiculoEntity vehiculo;

    @Column(name = "kilometraje_reportado")
    private Integer kilometrajeReportado;

    @Column(name = "aprobado_ruta")
    private Boolean aprobadoRuta;

    @Column(name = "observaciones_finales")
    private String observacionesFinales;

    @Column(name = "estado_vehiculo")
    private String estadoVehiculo;

    @Column(name = "login_user")
    private String loginUser;

    @Column(name = "id_ubicacion")
    private Integer idUbicacion;
}
