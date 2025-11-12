package com.app.usochicamochabackend.context.application.service;

import com.app.usochicamochabackend.context.application.dto.MachineCurriculumDTO;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.machine.application.port.FindMachineByIdUseCase;
import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurriculumServiceTest {

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private FindMachineByIdUseCase findMachineByIdUseCase;

    @Mock
    private SaveActionUseCase saveActionUseCase;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CurriculumService curriculumService;

    @BeforeEach
    void setUp() {
        TestSecurityUtils.setUpSecurityContext(1L, "testuser", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void getMachineCurriculum_ShouldReturnMachineCurriculumDTO_WhenMachineExists() {
        // Given
        Long machineId = 1L;
        MachineEntity testMachine = TestDataBuilder.createTestMachine();

        InspectionEntity inspection1 = TestDataBuilder.createTestInspection(testMachine, TestDataBuilder.createTestUser());
        inspection1.setId(1L);
        inspection1.setUUID("uuid-1");

        InspectionEntity inspection2 = TestDataBuilder.createTestInspection(testMachine, TestDataBuilder.createTestUser());
        inspection2.setId(2L);
        inspection2.setUUID("uuid-2");

        // Create orders with results
        OrderEntity order1 = TestDataBuilder.createTestOrder(inspection1, TestDataBuilder.createTestUser());
        ResultEntity result1 = TestDataBuilder.createTestResult(order1);
        order1.setResult(result1);

        OrderEntity order2 = TestDataBuilder.createTestOrder(inspection2, TestDataBuilder.createTestUser());
        ResultEntity result2 = TestDataBuilder.createTestResult(order2);
        order2.setResult(result2);

        inspection1.setOrders(Arrays.asList(order1));
        inspection2.setOrders(Arrays.asList(order2));

        List<InspectionEntity> inspections = Arrays.asList(inspection1, inspection2);

        when(machineRepository.findById(machineId)).thenReturn(java.util.Optional.of(testMachine));
        when(inspectionRepository.findByMachineId(machineId)).thenReturn(inspections);

        // When
        MachineCurriculumDTO result = curriculumService.getMachineCurriculum(machineId);

        // Then
        assertNotNull(result);
        assertEquals("Test Machine", result.machine().name());
        assertEquals("Model X", result.machine().model());
        assertEquals(2, result.results().size());

        verify(machineRepository).findById(machineId);
        verify(inspectionRepository).findByMachineId(machineId);
        verify(saveActionUseCase).save(anyString());

    }

    @Test
    void getMachineCurriculum_ShouldThrowException_WhenMachineNotFound() {
        // Given
        Long machineId = 999L;
        when(machineRepository.findById(machineId)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> curriculumService.getMachineCurriculum(machineId));
        verify(machineRepository).findById(machineId);
        verify(inspectionRepository, never()).findByMachineId(anyLong());
        verify(saveActionUseCase, never()).save(anyString());

    }

    @Test
    void getMachineCurriculum_ShouldThrowException_WhenNoInspectionsExist() {
        // Given
        Long machineId = 1L;
        MachineEntity testMachine = TestDataBuilder.createTestMachine();

        when(machineRepository.findById(machineId)).thenReturn(java.util.Optional.of(testMachine));
        when(inspectionRepository.findByMachineId(machineId)).thenReturn(Arrays.asList());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> curriculumService.getMachineCurriculum(machineId));

        verify(machineRepository).findById(machineId);
        verify(inspectionRepository).findByMachineId(machineId);
        verify(saveActionUseCase, never()).save(anyString());

    }
}
