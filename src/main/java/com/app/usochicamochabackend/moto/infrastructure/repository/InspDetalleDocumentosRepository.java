package com.app.usochicamochabackend.moto.infrastructure.repository;

import com.app.usochicamochabackend.moto.infrastructure.entity.InspDetalleDocumentosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository("motoInspDetalleDocumentosRepository")
public interface InspDetalleDocumentosRepository extends JpaRepository<InspDetalleDocumentosEntity, Long> {

    /** Retorna el último check de documentos para el vehículo indicado */
    @Query("""
                SELECT d FROM MotoInspDetalleDocumentosEntity d
                WHERE d.idInspeccion = (
                    SELECT MAX(i.id) FROM InspeccionEntity i
                    WHERE i.vehiculo.id = :idVehiculo
                )
            """)
    Optional<InspDetalleDocumentosEntity> findLatestByVehiculoId(@Param("idVehiculo") Integer idVehiculo);
}
