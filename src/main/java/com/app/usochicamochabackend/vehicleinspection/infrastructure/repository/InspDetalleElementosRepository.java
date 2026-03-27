package com.app.usochicamochabackend.vehicleinspection.infrastructure.repository;

import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspDetalleElementosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspDetalleElementosRepository extends JpaRepository<InspDetalleElementosEntity, Long> {
}
