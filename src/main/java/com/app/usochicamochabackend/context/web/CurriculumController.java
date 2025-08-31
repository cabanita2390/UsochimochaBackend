package com.app.usochicamochabackend.context.web;

import com.app.usochicamochabackend.context.application.dto.MachineCurriculumDTO;
import com.app.usochicamochabackend.context.application.port.GetMachineCurriculumUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/curriculum")
@RequiredArgsConstructor
@Tag(name = "Curriculum", description = "Endpoints for managing curriculum")
public class CurriculumController {

    private final GetMachineCurriculumUseCase getMachineCurriculumUseCase;

    @Operation(summary = "Get machine curriculum", description = "Returns the curriculum of a machine with inspections, orders and results")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Curriculum found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MachineCurriculumDTO.class))),
            @ApiResponse(responseCode = "404", description = "Machine not found",
                    content = @Content)
    })
    @GetMapping("/{machineId}")
    public ResponseEntity<MachineCurriculumDTO> getMachineCurriculum(
            @PathVariable Long machineId) {
        MachineCurriculumDTO curriculum = getMachineCurriculumUseCase.getMachineCurriculum(machineId);
        return ResponseEntity.ok(curriculum);
    }

}
