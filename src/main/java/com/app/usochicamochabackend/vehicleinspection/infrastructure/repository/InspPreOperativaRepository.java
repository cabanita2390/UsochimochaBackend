package com.app.usochicamochabackend.vehicleinspection.infrastructure.repository;

import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspPreOperativaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InspPreOperativaRepository extends JpaRepository<InspPreOperativaEntity, Long> {

    @Query("SELECT i FROM InspPreOperativaEntity i LEFT JOIN FETCH i.vehiculo ORDER BY i.fechaRegistro DESC")
    Page<InspPreOperativaEntity> findAllWithVehiculo(Pageable pageable);
}
