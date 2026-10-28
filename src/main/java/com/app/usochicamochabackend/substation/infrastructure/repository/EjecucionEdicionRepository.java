package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionEdicionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EjecucionEdicionRepository extends JpaRepository<EjecucionEdicionEntity, Long> {

    List<EjecucionEdicionEntity> findByEjecucion_IdOrderByEditadoEnAsc(Long ejecucionId);

    List<EjecucionEdicionEntity> findByEjecucion_IdInOrderByEditadoEnAsc(Collection<Long> ejecucionIds);
}
