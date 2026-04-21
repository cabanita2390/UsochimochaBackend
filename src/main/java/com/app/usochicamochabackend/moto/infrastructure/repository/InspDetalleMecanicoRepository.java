package com.app.usochicamochabackend.moto.infrastructure.repository;

import com.app.usochicamochabackend.moto.infrastructure.entity.InspDetalleMecanicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("motoInspDetalleMecanicoRepository")
public interface InspDetalleMecanicoRepository extends JpaRepository<InspDetalleMecanicoEntity, Long> {
}
