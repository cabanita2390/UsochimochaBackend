package com.app.usochicamochabackend.order.web;
/*
import com.app.usochicamochabackend.config.TestWebConfig;
import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.order.application.dto.*;
import com.app.usochicamochabackend.order.application.port.*;
import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;
import com.app.usochicamochabackend.user.application.dto.UserResponse;
import com.app.usochicamochabackend.utils.TestSecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestWebConfig.class)
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssignOrderUseCase assignOrderUseCase;

    @MockBean
    private GetAllOrdersByInspectionIdUseCase getAllOrdersByInspectionIdUseCase;

    @MockBean
    private GetAllOrdersUseCase getAllOrdersUseCase;

    @MockBean
    private GetAllOrdersByMachineIdUseCase getAllOrdersByMachineIdUseCase;

    @MockBean
    private GetOrderByIdUseCase getOrderByIdUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void assignOrder_ShouldReturnCreatedOrder() throws Exception {
        // Given
        AssignOrderRequest request = new AssignOrderRequest(1L, "Test order description");
        UserResponse userResponse = new UserResponse(1L, "testuser", "Test User", "test@example.com", "ADMIN");
        MachineResponse machineResponse = new MachineResponse(1L, "Test Machine", "Test Company", "Model X", LocalDate.now().plusMonths(6), "Test Brand", LocalDate.now().plusMonths(12), "ENG123", "ID123");
        InspectionFormResponse inspectionResponse = new InspectionFormResponse(
                1L, "test-uuid-123", false, LocalDateTime.now(), 100.0,
                "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD",
                "2024-12-31", "Applied", "All points greased", "Test observations",
                userResponse, machineResponse
        );
        OrderResponse response = new OrderResponse(1L, "PENDING", LocalDateTime.now(), "Test order description", inspectionResponse, userResponse);
        when(assignOrderUseCase.assignOrder(any(AssignOrderRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/order")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.description").value("Test order description"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllOrdersByInspectionId_ShouldReturnOrdersList() throws Exception {
        // Given
        UserResponse userResponse = new UserResponse(1L, "testuser", "Test User", "test@example.com", "ADMIN");
        MachineResponse machineResponse = new MachineResponse(1L, "Test Machine", "Test Company", "Model X", LocalDate.now().plusMonths(6), "Test Brand", LocalDate.now().plusMonths(12), "ENG123", "ID123");
        InspectionFormResponse inspectionResponse = new InspectionFormResponse(
                1L, "test-uuid-123", false, LocalDateTime.now(), 100.0,
                "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD",
                "2024-12-31", "Applied", "All points greased", "Test observations",
                userResponse, machineResponse
        );

        List<OrderResponse> orders = Arrays.asList(
                new OrderResponse(1L, "PENDING", LocalDateTime.now(), "Order 1", inspectionResponse, userResponse),
                new OrderResponse(2L, "COMPLETED", LocalDateTime.now(), "Order 2", inspectionResponse, userResponse)
        );
        GetAllOrdersByInspectionIdResponse response = new GetAllOrdersByInspectionIdResponse(inspectionResponse, orders);
        when(getAllOrdersByInspectionIdUseCase.getAllOrdersByInspectionId(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/order/all/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders.length()").value(2))
                .andExpect(jsonPath("$.orders[0].status").value("PENDING"))
                .andExpect(jsonPath("$.orders[1].status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_ShouldReturnOrdersWithMachines() throws Exception {
        // Given
        UserResponse userResponse = new UserResponse(1L, "testuser", "Test User", "test@example.com", "ADMIN");
        MachineResponse machineResponse1 = new MachineResponse(1L, "Machine 1", "Test Company", "Model X", LocalDate.now().plusMonths(6), "Test Brand", LocalDate.now().plusMonths(12), "ENG123", "ID123");
        MachineResponse machineResponse2 = new MachineResponse(2L, "Machine 2", "Test Company", "Model Y", LocalDate.now().plusMonths(6), "Test Brand", LocalDate.now().plusMonths(12), "ENG456", "ID456");

        OrderWithoutInspectionResponse order1 = new OrderWithoutInspectionResponse(1L, "PENDING", LocalDateTime.now(), "Order 1", userResponse);
        OrderWithoutInspectionResponse order2 = new OrderWithoutInspectionResponse(2L, "COMPLETED", LocalDateTime.now(), "Order 2", userResponse);

        List<OrderWithMachineDTO> orders = Arrays.asList(
                new OrderWithMachineDTO(order1, machineResponse1),
                new OrderWithMachineDTO(order2, machineResponse2)
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderWithMachineDTO> pageResponse = new PageImpl<>(orders, pageable, orders.size());
        when(getAllOrdersUseCase.getAllOrders(any(Pageable.class))).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/order/all")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].machine.name").value("Machine 1"))
                .andExpect(jsonPath("$.content[1].machine.name").value("Machine 2"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllOrdersByMachineId_ShouldReturnOrdersList() throws Exception {
        // Given
        UserResponse userResponse = new UserResponse(1L, "testuser", "Test User", "test@example.com", "ADMIN");
        MachineResponse machineResponse = new MachineResponse(1L, "Test Machine", "Test Company", "Model X", LocalDate.now().plusMonths(6), "Test Brand", LocalDate.now().plusMonths(12), "ENG123", "ID123");
        InspectionFormResponse inspectionResponse = new InspectionFormResponse(
                1L, "test-uuid-123", false, LocalDateTime.now(), 100.0,
                "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD",
                "2024-12-31", "Applied", "All points greased", "Test observations",
                userResponse, machineResponse
        );

        List<OrderWithoutInspectionResponse> ordersWithoutInspection = Arrays.asList(
                new OrderWithoutInspectionResponse(1L, "PENDING", LocalDateTime.now(), "Order 1", userResponse),
                new OrderWithoutInspectionResponse(2L, "IN_PROGRESS", LocalDateTime.now(), "Order 2", userResponse)
        );
        GetAllOrdersByMachineId response = new GetAllOrdersByMachineId(machineResponse, ordersWithoutInspection);
        when(getAllOrdersByMachineIdUseCase.getAllOrdersByMachineId(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/order/machine/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.orders.length()").value(2))
                .andExpect(jsonPath("$.orders[0].status").value("PENDING"))
                .andExpect(jsonPath("$.orders[1].status").value("IN_PROGRESS"));
    }
}
*/