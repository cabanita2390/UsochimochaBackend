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

    @Column(name = "kilometraje_actual")
    private Integer kilometrajeActual;

    private Boolean activo;

    @Column(name = "fecha_ultimo_reporte")
    private LocalDateTime fechaUltimoReporte;
}
