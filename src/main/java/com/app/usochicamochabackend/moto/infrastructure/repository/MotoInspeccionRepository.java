package com.app.usochicamochabackend.moto.infrastructure.repository;

import com.app.usochicamochabackend.moto.infrastructure.entity.InspeccionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MotoInspeccionRepository extends JpaRepository<InspeccionEntity, Long> {

    @Query("SELECT i FROM InspeccionEntity i LEFT JOIN FETCH i.vehiculo v LEFT JOIN FETCH v.tipoVehiculo LEFT JOIN FETCH i.ubicacion ORDER BY i.fechaRegistro DESC")
    Page<InspeccionEntity> findAllWithDetails(Pageable pageable);

    @Query("SELECT i FROM InspeccionEntity i LEFT JOIN FETCH i.vehiculo v LEFT JOIN FETCH v.tipoVehiculo LEFT JOIN FETCH i.ubicacion WHERE UPPER(v.tipoVehiculo.nombreTipo) = UPPER(:tipo) ORDER BY i.fechaRegistro DESC")
    Page<InspeccionEntity> findAllByTipoVehiculo(@Param("tipo") String tipo, Pageable pageable);
}

