package com.app.usochicamochabackend.tire.infrastructure.repository;

import com.app.usochicamochabackend.tire.infrastructure.entity.InspPresionLlantaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface InspPresionLlantaRepository extends JpaRepository<InspPresionLlantaEntity, Long> {
    List<InspPresionLlantaEntity> findByIdInspeccionVehiculoInOrderByOrdenAsc(Collection<Long> ids);
    List<InspPresionLlantaEntity> findByIdInspeccionMaquinaInOrderByOrdenAsc(Collection<Long> ids);
    boolean existsByIdInspeccionMaquina(Long idInspeccionMaquina);
}
