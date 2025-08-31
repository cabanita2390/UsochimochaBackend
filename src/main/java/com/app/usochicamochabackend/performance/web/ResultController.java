package com.app.usochicamochabackend.performance.web;

import com.app.usochicamochabackend.performance.application.dto.ExecuteAnOrderRequest;
import com.app.usochicamochabackend.performance.application.dto.ExecuteAnOrderResponse;
import com.app.usochicamochabackend.performance.application.port.ExecuteAnOrderUseCase;
import com.app.usochicamochabackend.performance.application.service.ResultService;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/v1/results")
@RequiredArgsConstructor
@Tag(name = "Results", description = "Endpoints for managing results")
public class ResultController {

    private final ExecuteAnOrderUseCase executeAnOrderUseCase;

    @Operation(
            summary = "Execute an order",
            description = "Executes an order by creating a new result with labors and spare parts. " +
                    "If sameMecanic is true in a labor, the mechanic is automatically taken " +
                    "from the inspection's assigned user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order executed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExecuteAnOrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/execute")
    public ResponseEntity<ExecuteAnOrderResponse> executeOrder(
            @RequestBody ExecuteAnOrderRequest request) {

        ExecuteAnOrderResponse response = executeAnOrderUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
}