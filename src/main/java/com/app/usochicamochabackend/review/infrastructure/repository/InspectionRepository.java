package com.app.usochicamochabackend.review.infrastructure.repository;

import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<InspectionEntity, Long> {
    List<InspectionEntity> findByMachineId(Long machineId);

    @Query(
            value = "SELECT * FROM users WHERE machine_id = :machineId ORDER BY time_stamp DESC LIMIT 1",
            nativeQuery = true
    )
    InspectionEntity getLastInspection(@Param("machineId") Long machineId);
}