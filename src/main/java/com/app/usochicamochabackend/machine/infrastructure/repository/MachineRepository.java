package com.app.usochicamochabackend.machine.infrastructure.repository;

import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MachineRepository extends JpaRepository<MachineEntity, Long> {}
