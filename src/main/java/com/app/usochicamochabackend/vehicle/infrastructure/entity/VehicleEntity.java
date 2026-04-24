package com.app.usochicamochabackend.vehicle.infrastructure.entity;

import com.app.usochicamochabackend.moto.infrastructure.entity.TipoVehiculoEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vehiculos")
public class VehicleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    private String placa;

    @Column(name = "id_marca")
    private Integer idMarca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca", insertable = false, updatable = false)
    private MarcaModeloEntity marca;

    @Column(name = "id_tipo_vehiculo")
    private Integer idTipoVehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_vehiculo", insertable = false, updatable = false)
    private TipoVehiculoEntity tipoVehiculo;

    private Boolean activo;

    @Column(name = "kilometraje_actual")
    private Integer kilometrajeActual;

    @Column(name = "fecha_ultimo_reporte")
    private LocalDateTime fechaUltimoReporte;
}
