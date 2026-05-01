package com.app.usochicamochabackend.vehicleinspection.infrastructure.repository;

import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspPreOperativaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface InspPreOperativaRepository extends JpaRepository<InspPreOperativaEntity, Long> {
    
    @Query("SELECT i FROM InspPreOperativaEntity i WHERE i.idVehiculo = :vehicleId ORDER BY i.fechaRegistro DESC LIMIT 1")
    Optional<InspPreOperativaEntity> findLatestByVehicleId(@Param("vehicleId") Integer vehicleId);
}
