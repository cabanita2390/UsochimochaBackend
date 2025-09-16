package com.app.usochicamochabackend.review.infrastructure.repository;

import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.utils.TestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InspectionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Test
    void save_ShouldPersistInspection() {
        // Given
        UserEntity user = TestDataBuilder.createTestUser();
        user.setId(null);
        UserEntity savedUser = entityManager.persistAndFlush(user);

        MachineEntity machine = TestDataBuilder.createTestMachine();
        machine.setId(null);
        MachineEntity savedMachine = entityManager.persistAndFlush(machine);

        InspectionEntity inspection = TestDataBuilder.createTestInspection(savedMachine, savedUser);
        inspection.setId(null);

        // When
        InspectionEntity savedInspection = inspectionRepository.save(inspection);

        // Then
        assertNotNull(savedInspection.getId());
        assertEquals("test-uuid-123", savedInspection.getUUID());
        assertEquals("GOOD", savedInspection.getLeakStatus());
        assertEquals("Test observations", savedInspection.getObservations());
        assertFalse(savedInspection.getUnexpected());
    }

    @Test
    void findById_ShouldReturnInspection_WhenInspectionExists() {
        // Given
        UserEntity user = TestDataBuilder.createTestUser();
        user.setId(null);
        UserEntity savedUser = entityManager.persistAndFlush(user);

        MachineEntity machine = TestDataBuilder.createTestMachine();
        machine.setId(null);
        MachineEntity savedMachine = entityManager.persistAndFlush(machine);

        InspectionEntity inspection = TestDataBuilder.createTestInspection(savedMachine, savedUser);
        inspection.setId(null);
        InspectionEntity savedInspection = entityManager.persistAndFlush(inspection);

        // When
        Optional<InspectionEntity> foundInspection = inspectionRepository.findById(savedInspection.getId());

        // Then
        assertTrue(foundInspection.isPresent());
        assertEquals("test-uuid-123", foundInspection.get().getUUID());
        assertEquals("GOOD", foundInspection.get().getLeakStatus());
    }

    @Test
    void findByMachineId_ShouldReturnInspectionsForMachine() {
        // Given
        UserEntity user = TestDataBuilder.createTestUser();
        user.setId(null);
        UserEntity savedUser = entityManager.persistAndFlush(user);

        MachineEntity machine = TestDataBuilder.createTestMachine();
        machine.setId(null);
        MachineEntity savedMachine = entityManager.persistAndFlush(machine);

        InspectionEntity inspection1 = TestDataBuilder.createTestInspection(savedMachine, savedUser);
        inspection1.setId(null);
        inspection1.setUUID("uuid-1");

        InspectionEntity inspection2 = TestDataBuilder.createTestInspection(savedMachine, savedUser);
        inspection2.setId(null);
        inspection2.setUUID("uuid-2");

        entityManager.persistAndFlush(inspection1);
        entityManager.persistAndFlush(inspection2);

        // When
        List<InspectionEntity> inspections = inspectionRepository.findByMachineId(savedMachine.getId());

        // Then
        assertEquals(2, inspections.size());
        assertTrue(inspections.stream().anyMatch(i -> "uuid-1".equals(i.getUUID())));
        assertTrue(inspections.stream().anyMatch(i -> "uuid-2".equals(i.getUUID())));
    }

    @Test
    void findAll_ShouldReturnAllInspections() {
        // Given
        UserEntity user = TestDataBuilder.createTestUser();
        user.setId(null);
        UserEntity savedUser = entityManager.persistAndFlush(user);

        MachineEntity machine = TestDataBuilder.createTestMachine();
        machine.setId(null);
        MachineEntity savedMachine = entityManager.persistAndFlush(machine);

        InspectionEntity inspection1 = TestDataBuilder.createTestInspection(savedMachine, savedUser);
        inspection1.setId(null);
        inspection1.setUUID("uuid-1");

        InspectionEntity inspection2 = TestDataBuilder.createTestInspection(savedMachine, savedUser);
        inspection2.setId(null);
        inspection2.setUUID("uuid-2");

        entityManager.persistAndFlush(inspection1);
        entityManager.persistAndFlush(inspection2);

        // When
        List<InspectionEntity> inspections = inspectionRepository.findAll();

        // Then
        assertEquals(2, inspections.size());
    }
}
