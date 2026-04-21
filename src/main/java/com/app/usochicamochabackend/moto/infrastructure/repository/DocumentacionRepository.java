package com.app.usochicamochabackend.moto.infrastructure.repository;

import com.app.usochicamochabackend.moto.infrastructure.entity.DocumentacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentacionRepository extends JpaRepository<DocumentacionEntity, Integer> {
    List<DocumentacionEntity> findByIdVehiculoAndActivoTrue(Integer idVehiculo);

    @org.springframework.data.jpa.repository.Query(value = """
                SELECT * FROM documentacion_y_elementos
                WHERE id_vehiculo = :idVehiculo
                  AND tipo_documento = :tipoDocumento
                  AND activo = true
                ORDER BY fecha_vencimiento DESC
                LIMIT 1
            """, nativeQuery = true)
    Optional<DocumentacionEntity> findLatestByVehiculoAndTipo(
            @org.springframework.data.repository.query.Param("idVehiculo") Integer idVehiculo,
            @org.springframework.data.repository.query.Param("tipoDocumento") String tipoDocumento);

    // Encuentra el último registro que tenga una imagen para rescatarla
    Optional<DocumentacionEntity> findFirstByIdVehiculoAndTipoDocumentoAndImagenUrlIsNotNullOrderByIdDesc(
            Integer idVehiculo, String tipoDocumento);
}
