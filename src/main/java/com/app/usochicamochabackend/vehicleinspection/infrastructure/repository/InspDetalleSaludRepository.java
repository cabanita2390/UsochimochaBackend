package com.app.usochicamochabackend.vehicleinspection.infrastructure.repository;

import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspDetalleSaludEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspDetalleSaludRepository extends JpaRepository<InspDetalleSaludEntity, Long> {
}
