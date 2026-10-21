package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.OrdenEventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenEventoRepository extends JpaRepository<OrdenEventoEntity, Long> {

    List<OrdenEventoEntity> findByOrden_IdOrderByCreadoEnAscIdAsc(Long ordenId);
}
