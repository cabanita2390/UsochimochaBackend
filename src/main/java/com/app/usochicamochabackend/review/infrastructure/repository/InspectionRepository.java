package com.app.usochicamochabackend.review.infrastructure.repository;

import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionRepository extends JpaRepository<InspectionEntity, Long> {
    List<InspectionEntity> findByMachineId(Long machineId);

    @Query(
            value = "SELECT * FROM inspections WHERE machine_id = :machineId ORDER BY date_stamp DESC LIMIT 1",
            nativeQuery = true
    )
    InspectionEntity getLastInspection(@Param("machineId") Long machineId);

    List<InspectionEntity> findByMachineIdAndUserIdAndDateStampAfter(Long machineId, Long userId, LocalDateTime dateStamp);

    Optional<InspectionEntity> findByUUID(String uuid);

    @Query("SELECT i FROM InspectionEntity i JOIN FETCH i.machine JOIN FETCH i.user")
    List<InspectionEntity> findAllWithMachineAndUser();
}