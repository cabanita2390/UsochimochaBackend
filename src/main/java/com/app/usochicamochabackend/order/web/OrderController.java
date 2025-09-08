package com.app.usochicamochabackend.order.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.order.application.dto.AssignOrderRequest;
import com.app.usochicamochabackend.order.application.dto.GetAllOrdersByInspectionIdResponse;
import com.app.usochicamochabackend.order.application.dto.OrderResponse;
import com.app.usochicamochabackend.order.application.port.AssignOrderUseCase;
import com.app.usochicamochabackend.order.application.port.GetAllOrdersByInspectionIdUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints for managing orders")
public class OrderController {

    private final AssignOrderUseCase assignOrderUseCase;
    private final GetAllOrdersByInspectionIdUseCase  getAllOrdersByInspectionIdUseCase;

    @Operation(
            summary = "Assign a new order",
            description = "Creates a new order for a given inspection, assigning it to a specific user with an optional description."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order successfully created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inspection, assigner user, or assigned user not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse assignOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order assignment data including inspectionId, assignerUserId, assignedUserId and description",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AssignOrderRequest.class))
            )
            @RequestBody AssignOrderRequest assignOrderRequest) {

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userPrincipal.id();
        System.out.println("userId = " + userId);
        
        return assignOrderUseCase.assignOrder(assignOrderRequest);
    }

    @GetMapping("/orders/{inspectionId}")
    public GetAllOrdersByInspectionIdResponse getAllOrdersByInspectionId(@PathVariable Long inspectionId) {
        return getAllOrdersByInspectionIdUseCase.getAllOrdersByInspectionId(inspectionId);
    }

}