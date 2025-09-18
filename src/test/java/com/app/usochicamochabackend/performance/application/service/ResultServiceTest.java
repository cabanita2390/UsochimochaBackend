package com.app.usochicamochabackend.performance.application.service;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.performance.application.dto.*;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;
import com.app.usochicamochabackend.performance.infrastructure.repository.ResultRepository;
import com.app.usochicamochabackend.order.application.port.GetOrderByIdUseCase;
import com.app.usochicamochabackend.order.application.dto.OrderResponse;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.user.application.dto.UserResponse;
import com.app.usochicamochabackend.utils.TestDataBuilder;
import com.app.usochicamochabackend.utils.TestSecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private GetOrderByIdUseCase getOrderByIdUseCase;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepositoryJpa userRepository;

    @Mock
    private SaveActionUseCase saveActionUseCase;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ResultService resultService;

    private UserEntity testUser;
    private UserEntity testMechanic;
    private MachineEntity testMachine;
    private InspectionEntity testInspection;
    private OrderEntity testOrder;
    private ResultEntity testResult;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        testMechanic = TestDataBuilder.createTestMechanic();
        testMachine = TestDataBuilder.createTestMachine();
        testInspection = TestDataBuilder.createTestInspection(testMachine, testUser);
        testOrder = TestDataBuilder.createTestOrder(testInspection, testUser);
        testResult = TestDataBuilder.createTestResult(testOrder);
        TestSecurityUtils.setUpSecurityContext(1L, "testuser", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void executeAnOrder_ShouldReturnResultDTO_WhenOrderIsExecuted() {
        // Given
        LaborRequest laborRequest = new LaborRequest(
                new BigDecimal("200.00"),
                true,
                "Test Contractor",
                "Labor completed successfully"
        );

        SparePartRequest sparePartRequest = new SparePartRequest(
                "SP002",
                "Test Spare Part 2",
                3,
                new BigDecimal("75.00")
        );

        ExecuteAnOrderRequest request = new ExecuteAnOrderRequest(
                1L,
                "3 hours",
                "Result description",
                laborRequest,
                sparePartRequest
        );

        UserResponse userResponse = new UserResponse(1L, "testuser", "Test User", "test@example.com", "ADMIN");
        MachineResponse machineResponse = new MachineResponse(1L, "Test Machine", "Test Company", "Model X", LocalDate.now().plusMonths(6), "Test Brand", LocalDate.now().plusMonths(12), "ENG123", "ID123");
        InspectionFormResponse inspectionResponse = new InspectionFormResponse(
                1L, "test-uuid-123", false, LocalDateTime.now(), 100.0,
                "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD",
                "2024-12-31", "Applied", "All points greased", "Test observations",
                userResponse, machineResponse
        );
        OrderResponse orderResponse = new OrderResponse(
                1L,
                "PENDING",
                LocalDateTime.now(),
                "Test order description",
                inspectionResponse,
                userResponse
        );

        ResultEntity savedResult = TestDataBuilder.createTestResult(testOrder);
        savedResult.setId(2L);
        savedResult.setDescription("Result description");
        savedResult.setTimeSpent("3 hours");

        when(getOrderByIdUseCase.getOrderById(1L)).thenReturn(orderResponse);
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(testOrder));
        when(resultRepository.save(any(ResultEntity.class))).thenReturn(savedResult);

        // When
        ExecuteDTO response = resultService.execute(request);

        // Then
        assertNotNull(response);
        assertEquals("Result description", response.description());
        assertEquals("3 hours", response.timeSpent());

        verify(getOrderByIdUseCase).getOrderById(1L);
        verify(orderRepository).findById(1L);
        verify(resultRepository).save(any(ResultEntity.class));
        verify(orderRepository).save(any(OrderEntity.class));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("actions-updated");
        verify(notificationService).notify("results-updated");
    }

    @Test
    void execute_ShouldThrowException_WhenOrderNotFound() {
        // Given
        ExecuteAnOrderRequest request = new ExecuteAnOrderRequest(
                999L,
                "3 hours",
                "Result description",
                null,
                null
        );

        when(getOrderByIdUseCase.getOrderById(999L)).thenThrow(new RuntimeException("Order not found"));

        // When & Then
        assertThrows(RuntimeException.class, () -> resultService.execute(request));
        verify(getOrderByIdUseCase).getOrderById(999L);
        verify(orderRepository, never()).findById(anyLong());
        verify(resultRepository, never()).save(any(ResultEntity.class));
        verify(orderRepository, never()).save(any(OrderEntity.class));
        verify(saveActionUseCase, never()).save(anyString());
        verify(notificationService, never()).notify(anyString());
    }

    @Test
    void execute_ShouldThrowException_WhenMechanicNotFound() {
        // Given
        LaborRequest laborRequest = new LaborRequest(
                new BigDecimal("200.00"),
                true,
                "Test Contractor",
                "Labor completed successfully"
        );

        ExecuteAnOrderRequest request = new ExecuteAnOrderRequest(
                1L,
                "3 hours",
                "Result description",
                laborRequest,
                null
        );

        UserResponse userResponse = new UserResponse(1L, "testuser", "Test User", "test@example.com", "ADMIN");
        MachineResponse machineResponse = new MachineResponse(1L, "Test Machine", "Test Company", "Model X", LocalDate.now().plusMonths(6), "Test Brand", LocalDate.now().plusMonths(12), "ENG123", "ID123");
        InspectionFormResponse inspectionResponse = new InspectionFormResponse(
                1L, "test-uuid-123", false, LocalDateTime.now(), 100.0,
                "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD",
                "2024-12-31", "Applied", "All points greased", "Test observations",
                userResponse, machineResponse
        );
        OrderResponse orderResponse = new OrderResponse(
                1L,
                "PENDING",
                LocalDateTime.now(),
                "Test order description",
                inspectionResponse,
                userResponse
        );

        when(getOrderByIdUseCase.getOrderById(1L)).thenReturn(orderResponse);
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(testOrder));
        when(resultRepository.save(any(ResultEntity.class))).thenReturn(TestDataBuilder.createTestResult(testOrder));

        // When
        resultService.execute(request);

        // Then
        verify(getOrderByIdUseCase).getOrderById(1L);
        verify(orderRepository).findById(1L);
        verify(resultRepository).save(any(ResultEntity.class));
        verify(orderRepository).save(any(OrderEntity.class));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("actions-updated");
        verify(notificationService).notify("results-updated");
    }

    @Test
    void execute_ShouldHandleNullLaborAndSparePart() {
        // Given
        ExecuteAnOrderRequest request = new ExecuteAnOrderRequest(
                1L,
                "2 hours",
                "Result description",
                null,
                null
        );

        UserResponse userResponse = new UserResponse(1L, "testuser", "Test User", "test@example.com", "ADMIN");
        MachineResponse machineResponse = new MachineResponse(1L, "Test Machine", "Test Company", "Model X", LocalDate.now().plusMonths(6), "Test Brand", LocalDate.now().plusMonths(12), "ENG123", "ID123");
        InspectionFormResponse inspectionResponse = new InspectionFormResponse(
                1L, "test-uuid-123", false, LocalDateTime.now(), 100.0,
                "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD",
                "2024-12-31", "Applied", "All points greased", "Test observations",
                userResponse, machineResponse
        );
        OrderResponse orderResponse = new OrderResponse(
                1L,
                "PENDING",
                LocalDateTime.now(),
                "Test order description",
                inspectionResponse,
                userResponse
        );

        ResultEntity savedResult = ResultEntity.builder()
                .id(2L)
                .description("Result description")
                .timeSpent("2 hours")
                .date(LocalDateTime.now())
                .order(testOrder)
                .laborForce(null)
                .sparePart(null)
                .build();

        when(getOrderByIdUseCase.getOrderById(1L)).thenReturn(orderResponse);
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(testOrder));
        when(resultRepository.save(any(ResultEntity.class))).thenReturn(savedResult);

        // When
        ExecuteDTO response = resultService.execute(request);

        // Then
        assertNotNull(response);
        assertEquals("Result description", response.description());
        assertEquals("2 hours", response.timeSpent());
        assertNull(response.labor());
        assertNull(response.sparePart());

        verify(getOrderByIdUseCase).getOrderById(1L);
        verify(orderRepository).findById(1L);
        verify(resultRepository).save(any(ResultEntity.class));
        verify(orderRepository).save(any(OrderEntity.class));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("actions-updated");
        verify(notificationService).notify("results-updated");
    }
}
