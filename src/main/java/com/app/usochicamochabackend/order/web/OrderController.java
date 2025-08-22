package com.app.usochicamochabackend.order.web;

import com.app.usochicamochabackend.order.application.service.OrderService;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints for managing orders")
public class OrderController {

    private final OrderService orderService;

    /* --- READ --- */
    @GetMapping("/{id}")
    @Operation(summary = "Get orders by ID")
    public ResponseEntity<OrderEntity> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findOrderById(id));
    }

    @GetMapping
    @Operation(summary = "Get all orders")
    public ResponseEntity<List<OrderEntity>> getInspections() {
        return ResponseEntity.ok(orderService.findAllOrder());
    }

    /* --- CREATE --- */
    /*
    @PostMapping
    @Operation(summary = "Create order")
    @ApiResponse(responseCode = "201", description = "Order created")
    public ResponseEntity<OrderEntity> createOrder(@RequestBody OrderEntity orderEntity)
            throws URISyntaxException {
        OrderEntity saved = orderService.createOrder(orderEntity);
        return ResponseEntity
                .created(new URI("/api/v1/order/" + saved.getId()))
                .body(saved);
    }
    */

    /*
    @PutMapping("/{id}")
    @Operation(summary = "Update Order")
    public ResponseEntity<OrderEntity> updateOrder(
            @PathVariable Long id,
            @RequestBody OrderEntity orderEntity) throws URISyntaxException {

        orderEntity.setId(id);
        OrderEntity updated = orderService.updateOrder(orderEntity);
        return ResponseEntity
                .created(new URI("/api/v1/order/" + updated.getId()))
                .body(updated);
    }
    */
/*
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Order")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order deleted")
    })
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

 */
}