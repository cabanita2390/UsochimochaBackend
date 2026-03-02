package com.app.usochicamochabackend.moto.infrastructure.repository;

import com.app.usochicamochabackend.moto.infrastructure.entity.DocumentacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentacionRepository extends JpaRepository<DocumentacionEntity, Integer> {
    List<DocumentacionEntity> findByIdVehiculoAndActivoTrue(Integer idVehiculo);

    // Encuentra el último registro que tenga una imagen para rescatarla
    Optional<DocumentacionEntity> findFirstByIdVehiculoAndTipoDocumentoAndImagenUrlIsNotNullOrderByIdDesc(
            Integer idVehiculo, String tipoDocumento);
}
