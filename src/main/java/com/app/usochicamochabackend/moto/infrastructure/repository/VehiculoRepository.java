package com.app.usochicamochabackend.moto.infrastructure.repository;

import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<VehicleEntity, Integer> {

    @Query("SELECT v FROM VehicleEntity v WHERE v.activo = true AND UPPER(v.tipoVehiculo.nombreTipo) = UPPER(:tipo)")
    List<VehicleEntity> findActivosByTipo(@Param("tipo") String tipo);

    Optional<VehicleEntity> findByPlacaAndActivoTrue(String placa);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE VehicleEntity v SET v.kilometrajeActual = :km, v.fechaUltimoReporte = :fecha WHERE v.idVehiculo = :id")
    void updateKilometraje(@Param("id") Integer id, @Param("km") Integer km, @Param("fecha") java.time.LocalDateTime fecha);
}
