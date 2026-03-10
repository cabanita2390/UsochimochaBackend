package com.app.usochicamochabackend.vehicle.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vehiculos")
public class VehicleEntity {

    @Id
    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    private String placa;

    @Column(name = "id_marca")
    private Integer idMarca;

    @Column(name = "id_tipo_vehiculo")
    private Integer idTipoVehiculo;

    private Boolean activo;

    @Column(name = "kilometraje_actual")
    private Integer kilometrajeActual;
}
