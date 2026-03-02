package com.app.usochicamochabackend.moto.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vehiculos")
public class VehiculoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_vehiculo")
    private TipoVehiculoEntity tipoVehiculo;

    private String placa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ubicacion")
    private UbicacionEntity ubicacion;

    @Column(name = "kilometraje_actual")
    private Integer kilometrajeActual;

    @Column(name = "estado_vehiculo")
    private String estadoVehiculo;

    private Boolean activo;

    @Column(name = "login_user")
    private String loginUser;

    @Column(name = "fecha_ultimo_reporte")
    private LocalDateTime fechaUltimoReporte;
}
