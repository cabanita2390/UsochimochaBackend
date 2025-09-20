package com.app.usochicamochabackend.machine.infrastructure.repository;

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
class MachineRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MachineRepository machineRepository;

    @Test
    void save_ShouldPersistMachine() {
        // Given
        MachineEntity machine = TestDataBuilder.createTestMachine();
        machine.setId(null); // Let JPA generate the ID

        // When
        MachineEntity savedMachine = machineRepository.save(machine);

        // Then
        assertNotNull(savedMachine.getId());
        assertEquals("Test Machine", savedMachine.getName());
        assertEquals("Model X", savedMachine.getModel());
        assertEquals("Test Company", savedMachine.getBelongsTo());
        assertTrue(savedMachine.getStatus());
    }

    @Test
    void findById_ShouldReturnMachine_WhenMachineExists() {
        // Given
        MachineEntity machine = TestDataBuilder.createTestMachine();
        machine.setId(null); // Let JPA generate the ID
        MachineEntity savedMachine = entityManager.persistAndFlush(machine);

        // When
        Optional<MachineEntity> foundMachine = machineRepository.findById(savedMachine.getId());

        // Then
        assertTrue(foundMachine.isPresent());
        assertEquals("Test Machine", foundMachine.get().getName());
        assertEquals("Model X", foundMachine.get().getModel());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenMachineDoesNotExist() {
        // When
        Optional<MachineEntity> foundMachine = machineRepository.findById(999L);

        // Then
        assertFalse(foundMachine.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllMachines() {
        // Given
        MachineEntity machine1 = TestDataBuilder.createTestMachine();
        machine1.setId(null);
        machine1.setName("Machine 1");

        MachineEntity machine2 = TestDataBuilder.createTestMachine();
        machine2.setId(null);
        machine2.setName("Machine 2");

        entityManager.persistAndFlush(machine1);
        entityManager.persistAndFlush(machine2);

        // When
        List<MachineEntity> machines = machineRepository.findAll();

        // Then
        assertEquals(2, machines.size());
        assertTrue(machines.stream().anyMatch(m -> "Machine 1".equals(m.getName())));
        assertTrue(machines.stream().anyMatch(m -> "Machine 2".equals(m.getName())));
    }

    @Test
    void delete_ShouldRemoveMachine() {
        // Given
        MachineEntity machine = TestDataBuilder.createTestMachine();
        machine.setId(null); // Let JPA generate the ID
        MachineEntity savedMachine = entityManager.persistAndFlush(machine);

        // When
        machineRepository.delete(savedMachine);
        entityManager.flush();

        // Then
        Optional<MachineEntity> foundMachine = machineRepository.findById(savedMachine.getId());
        assertFalse(foundMachine.isPresent());
    }
}
