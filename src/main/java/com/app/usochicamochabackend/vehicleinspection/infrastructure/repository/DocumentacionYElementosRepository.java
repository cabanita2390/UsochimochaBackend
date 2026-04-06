package com.app.usochicamochabackend.vehicleinspection.infrastructure.repository;

import com.app.usochicamochabackend.vehicleinspection.infrastructure.entity.DocumentacionYElementosEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentacionYElementosRepository extends JpaRepository<DocumentacionYElementosEntity, Integer> {

    /**
     * Trae el documento más reciente (mayor fecha_vencimiento) de un vehículo
     * filtrada por tipo de documento (SOAT, TECNO, LICENCIA, EXTINTOR).
     */
    @Query(value = """
            SELECT * FROM documentacion_y_elementos
            WHERE id_vehiculo = :idVehiculo
              AND tipo_documento = :tipoDocumento
              AND activo = true
            ORDER BY fecha_vencimiento DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<DocumentacionYElementosEntity> findLatestByVehiculoAndTipo(
            @Param("idVehiculo") Integer idVehiculo,
            @Param("tipoDocumento") String tipoDocumento);
}
