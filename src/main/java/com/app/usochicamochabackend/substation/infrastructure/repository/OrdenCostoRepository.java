package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.OrdenCostoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenCostoRepository extends JpaRepository<OrdenCostoEntity, Long> {

    List<OrdenCostoEntity> findByOrden_IdOrderByIdAsc(Long ordenId);

    void deleteByOrden_Id(Long ordenId);
}
