package com.app.usochicamochabackend.order.web;

import com.app.usochicamochabackend.order.application.dto.AssignOrderRequest;
import com.app.usochicamochabackend.order.application.dto.AssignOrderResponse;
import com.app.usochicamochabackend.order.application.port.AssignOrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Endpoints for managing orders")
public class OrderController {

    private final AssignOrderUseCase assignOrderUseCase;

    @Operation(
            summary = "Assign a new order",
            description = "Creates a new order for a given inspection, assigning it to a specific user with an optional description."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order successfully created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AssignOrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Inspection, assigner user, or assigned user not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AssignOrderResponse assignOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order assignment data including inspectionId, assignerUserId, assignedUserId and description",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AssignOrderRequest.class))
            )
            @RequestBody AssignOrderRequest assignOrderRequest) {

        return assignOrderUseCase.assignOrder(assignOrderRequest);
    }
}