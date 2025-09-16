package com.app.usochicamochabackend.machine.application.service;

import com.app.usochicamochabackend.machine.application.dto.MachineRequest;
import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.utils.TestDataBuilder;
import com.app.usochicamochabackend.utils.TestSecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MachineServiceTest {

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private SaveActionUseCase saveActionUseCase;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MachineService machineService;

    private MachineEntity testMachine;

    @BeforeEach
    void setUp() {
        testMachine = TestDataBuilder.createTestMachine();
        TestSecurityUtils.setUpSecurityContext(1L, "testuser", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void createMachine_ShouldReturnMachineResponse_WhenMachineIsCreated() {
        // Given
        MachineRequest request = new MachineRequest(
                "New Machine",
                "Model Y",
                "Company ABC",
                LocalDate.now().plusMonths(6),
                "Brand X",
                LocalDate.now().plusMonths(12),
                "ENG456",
                "ID456"
        );
        
        MachineEntity savedMachine = MachineEntity.builder()
                .id(2L)
                .name("New Machine")
                .model("Model Y")
                .belongsTo("Company ABC")
                .soat(LocalDate.now().plusMonths(6))
                .brand("Brand X")
                .runt(LocalDate.now().plusMonths(12))
                .status(true)
                .numEngine("ENG456")
                .numInterIdentification("ID456")
                .build();

        when(machineRepository.save(any(MachineEntity.class))).thenReturn(savedMachine);

        // When
        MachineResponse response = machineService.createMachine(request);

        // Then
        assertNotNull(response);
        assertEquals("New Machine", response.name());
        assertEquals("Model Y", response.model());
        assertEquals("Company ABC", response.belongsTo());
        assertEquals("Brand X", response.brand());

        verify(machineRepository).save(any(MachineEntity.class));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("actions-updated");
        verify(notificationService).notify("machines-updated");
    }

    @Test
    void findAllMachines_ShouldReturnListOfMachines() {
        // Given
        MachineEntity machine2 = MachineEntity.builder()
                .id(2L)
                .name("Machine 2")
                .model("Model Z")
                .belongsTo("Company XYZ")
                .soat(LocalDate.now().plusMonths(3))
                .brand("Brand Y")
                .runt(LocalDate.now().plusMonths(9))
                .status(true)
                .numEngine("ENG789")
                .numInterIdentification("ID789")
                .build();

        MachineEntity inactiveMachine = MachineEntity.builder()
                .id(3L)
                .name("Inactive Machine")
                .model("Model W")
                .belongsTo("Company ABC")
                .soat(LocalDate.now().plusMonths(1))
                .brand("Brand Z")
                .runt(LocalDate.now().plusMonths(6))
                .status(false)
                .numEngine("ENG999")
                .numInterIdentification("ID999")
                .build();

        List<MachineEntity> machines = Arrays.asList(testMachine, machine2, inactiveMachine);
        when(machineRepository.findAll()).thenReturn(machines);

        // When
        List<MachineResponse> responses = machineService.findAllMachines();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size()); // Should filter out inactive machine
        assertEquals("Test Machine", responses.get(0).name());
        assertEquals("Machine 2", responses.get(1).name());
        verify(machineRepository).findAll();
        verify(notificationService).notify("actions-updated");
    }

    @Test
    void findMachineById_ShouldReturnMachineResponse_WhenMachineExists() {
        // Given
        when(machineRepository.findById(1L)).thenReturn(Optional.of(testMachine));

        // When
        MachineResponse response = machineService.findMachineById(1L);

        // Then
        assertNotNull(response);
        assertEquals("Test Machine", response.name());
        assertEquals("Model X", response.model());
        assertEquals("Test Company", response.belongsTo());
        verify(machineRepository).findById(1L);
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("actions-updated");
    }

    @Test
    void findMachineById_ShouldThrowException_WhenMachineNotFound() {
        // Given
        when(machineRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> machineService.findMachineById(999L));
        verify(machineRepository).findById(999L);
    }

    @Test
    void findMachineById_ShouldThrowException_WhenMachineIsInactive() {
        // Given
        MachineEntity inactiveMachine = MachineEntity.builder()
                .id(1L)
                .name("Inactive Machine")
                .model("Model X")
                .belongsTo("Test Company")
                .soat(LocalDate.now().plusMonths(6))
                .brand("Test Brand")
                .runt(LocalDate.now().plusMonths(12))
                .status(false)
                .numEngine("ENG123")
                .numInterIdentification("ID123")
                .build();

        when(machineRepository.findById(1L)).thenReturn(Optional.of(inactiveMachine));

        // When & Then
        assertThrows(RuntimeException.class, () -> machineService.findMachineById(1L));
        verify(machineRepository).findById(1L);
        verify(saveActionUseCase, never()).save(anyString());
        verify(notificationService, never()).notify(anyString());
    }

    @Test
    void updateMachine_ShouldReturnUpdatedMachineResponse() {
        // Given
        MachineRequest request = new MachineRequest(
                "Updated Machine",
                "Updated Model",
                "Updated Company",
                LocalDate.now().plusMonths(8),
                "Updated Brand",
                LocalDate.now().plusMonths(14),
                "UPDENG",
                "UPDID"
        );

        MachineEntity updatedMachine = MachineEntity.builder()
                .id(1L)
                .name("Updated Machine")
                .model("Updated Model")
                .belongsTo("Updated Company")
                .soat(LocalDate.now().plusMonths(8))
                .brand("Updated Brand")
                .runt(LocalDate.now().plusMonths(14))
                .status(true)
                .numEngine("UPDENG")
                .numInterIdentification("UPDID")
                .build();

        when(machineRepository.findById(1L)).thenReturn(Optional.of(testMachine));
        when(machineRepository.save(any(MachineEntity.class))).thenReturn(updatedMachine);

        // When
        MachineResponse response = machineService.updateMachine(request, 1L);

        // Then
        assertNotNull(response);
        assertEquals("Updated Machine", response.name());
        assertEquals("Updated Model", response.model());
        assertEquals("Updated Company", response.belongsTo());

        verify(machineRepository).findById(1L);
        verify(machineRepository).save(any(MachineEntity.class));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("actions-updated");
        verify(notificationService).notify("machines-updated");
    }

    @Test
    void deleteMachine_ShouldSetStatusToFalse_WhenMachineExists() {
        // Given
        when(machineRepository.findById(1L)).thenReturn(Optional.of(testMachine));

        // When
        machineService.deleteMachine(1L);

        // Then
        verify(machineRepository).findById(1L);
        verify(machineRepository).save(argThat(machine -> {
            assertEquals(1L, machine.getId());
            assertFalse(machine.getStatus()); // Verify status is set to false
            assertEquals("Test Machine", machine.getName());
            return true;
        }));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("actions-updated");
        verify(notificationService).notify("machines-updated");
    }

    @Test
    void deleteMachine_ShouldThrowException_WhenMachineNotFound() {
        // Given
        when(machineRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> machineService.deleteMachine(999L));
        verify(machineRepository).findById(999L);
        verify(machineRepository, never()).save(any(MachineEntity.class));
    }

    @Test
    void deleteMachine_ShouldThrowException_WhenMachineIsInactive() {
        // Given
        MachineEntity inactiveMachine = MachineEntity.builder()
                .id(1L)
                .name("Inactive Machine")
                .model("Model X")
                .belongsTo("Test Company")
                .soat(LocalDate.now().plusMonths(6))
                .brand("Test Brand")
                .runt(LocalDate.now().plusMonths(12))
                .status(false)
                .numEngine("ENG123")
                .numInterIdentification("ID123")
                .build();

        when(machineRepository.findById(1L)).thenReturn(Optional.of(inactiveMachine));

        // When & Then
        assertThrows(RuntimeException.class, () -> machineService.deleteMachine(1L));
        verify(machineRepository).findById(1L);
        verify(machineRepository, never()).save(any(MachineEntity.class));
        verify(saveActionUseCase, never()).save(anyString());
        verify(notificationService, never()).notify(anyString());
    }
}
