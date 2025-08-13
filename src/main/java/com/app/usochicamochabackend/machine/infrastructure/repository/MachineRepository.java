package com.app.usochicamochabackend.machine.infrastructure.repository;

import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineRepository extends JpaRepository<MachineEntity, Long> {}
