package com.app.usochicamochabackend.order.application.service;

import com.app.usochicamochabackend.order.application.dto.*;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.utils.TestDataBuilder;
import com.app.usochicamochabackend.utils.TestSecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private UserRepositoryJpa userRepository;

    @Mock
    private SaveActionUseCase saveActionUseCase;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    private UserEntity testUser;
    private MachineEntity testMachine;
    private InspectionEntity testInspection;
    private OrderEntity testOrder;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        testMachine = TestDataBuilder.createTestMachine();
        testInspection = TestDataBuilder.createTestInspection(testMachine, testUser);
        testOrder = TestDataBuilder.createTestOrder(testInspection, testUser);
        TestSecurityUtils.setUpSecurityContext(1L, "testuser", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void assignOrder_ShouldReturnOrderResponse_WhenOrderIsAssigned() {
        // Given
        AssignOrderRequest request = new AssignOrderRequest(1L, "Test order description");
        OrderEntity savedOrder = TestDataBuilder.createTestOrder(testInspection, testUser);
        savedOrder.setId(2L);

        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(testInspection));
        when(userRepository.getUserEntityById(1L)).thenReturn(testUser);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);

        // When
        OrderResponse response = orderService.assignOrder(request);

        // Then
        assertNotNull(response);
        assertEquals("PENDING", response.status());
        assertEquals("Test order description", response.description());

        verify(inspectionRepository).findById(1L);
        verify(userRepository).getUserEntityById(1L);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify(anyString());
    }

    @Test
    void assignOrder_ShouldThrowException_WhenInspectionNotFound() {
        // Given
        AssignOrderRequest request = new AssignOrderRequest(999L, "Test order description");
        when(inspectionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.assignOrder(request));
        verify(inspectionRepository).findById(999L);
        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    void getAllOrdersByInspectionId_ShouldReturnOrdersList() {
        // Given
        OrderEntity order2 = TestDataBuilder.createTestOrder(testInspection, testUser);
        order2.setId(2L);
        order2.setDescription("Second order");

        List<OrderEntity> orders = Arrays.asList(testOrder, order2);
        when(orderRepository.getAllByInspectionId(1L)).thenReturn(orders);

        // When
        GetAllOrdersByInspectionIdResponse response = orderService.getAllOrdersByInspectionId(1L);

        // Then
        assertNotNull(response);
        assertEquals(2, response.orders().size());
        assertEquals("Test order description", response.orders().get(0).description());
        assertEquals("Second order", response.orders().get(1).description());
        verify(orderRepository).getAllByInspectionId(1L);
    }

    @Test
    void getOrderById_ShouldReturnOrderResponse_WhenOrderExists() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        OrderResponse response = orderService.getOrderById(1L);

        // Then
        assertNotNull(response);
        assertEquals("PENDING", response.status());
        assertEquals("Test order description", response.description());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_ShouldThrowException_WhenOrderNotFound() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.getOrderById(999L));
        verify(orderRepository).findById(999L);
    }

    @Test
    void getAllOrders_ShouldReturnPagedOrdersWithMachines() {
        // Given
        OrderEntity order2 = TestDataBuilder.createTestOrder(testInspection, testUser);
        order2.setId(2L);

        List<OrderEntity> orders = Arrays.asList(testOrder, order2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> orderPage = new PageImpl<>(orders, pageable, orders.size());
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // When
        Page<OrderWithMachineDTO> response = orderService.getAllOrders(pageable);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals("Test order description", response.getContent().get(0).order().description());
        assertEquals("Test order description", response.getContent().get(1).order().description());
        verify(orderRepository).findAll(pageable);
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("actions-updated");
    }

    @Test
    void getAllOrdersByMachineId_ShouldReturnOrdersList() {
        // Given
        List<InspectionEntity> inspections = Arrays.asList(testInspection);
        when(inspectionRepository.findByMachineId(1L)).thenReturn(inspections);

        OrderEntity order2 = TestDataBuilder.createTestOrder(testInspection, testUser);
        order2.setId(2L);
        List<OrderEntity> orders = Arrays.asList(testOrder, order2);
        when(orderRepository.getAllByInspectionId(1L)).thenReturn(orders);

        // When
        GetAllOrdersByMachineId response = orderService.getAllOrdersByMachineId(1L);

        // Then
        assertNotNull(response);
        assertEquals(2, response.orders().size());
        verify(inspectionRepository).findByMachineId(1L);
        verify(orderRepository).getAllByInspectionId(1L);
    }
}
