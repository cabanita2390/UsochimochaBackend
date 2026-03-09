package com.app.usochicamochabackend.vehicleinspection.infrastructure.repository;

import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.InspDetalleDocumentosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspDetalleDocumentosRepository extends JpaRepository<InspDetalleDocumentosEntity, Long> {
}
