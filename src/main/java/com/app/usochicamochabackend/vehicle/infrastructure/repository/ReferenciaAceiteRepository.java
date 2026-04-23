package com.app.usochicamochabackend.vehicle.infrastructure.repository;

import com.app.usochicamochabackend.vehicle.infrastructure.entity.ReferenciaAceiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferenciaAceiteRepository extends JpaRepository<ReferenciaAceiteEntity, Integer> {
}
