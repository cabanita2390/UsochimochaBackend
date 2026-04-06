package com.app.usochicamochabackend.moto.infrastructure.repository;

import com.app.usochicamochabackend.moto.infrastructure.entity.VehiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<VehiculoEntity, Integer> {

    @Query("SELECT v FROM VehiculoEntity v WHERE v.activo = true AND UPPER(v.tipoVehiculo.nombreTipo) = UPPER(:tipo)")
    List<VehiculoEntity> findActivosByTipo(@Param("tipo") String tipo);

    Optional<VehiculoEntity> findByPlacaAndActivoTrue(String placa);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE VehiculoEntity v SET v.kilometrajeActual = :km, v.fechaUltimoReporte = :fecha WHERE v.id = :id")
    void updateKilometraje(@Param("id") Integer id, @Param("km") Integer km, @Param("fecha") java.time.LocalDateTime fecha);
}
