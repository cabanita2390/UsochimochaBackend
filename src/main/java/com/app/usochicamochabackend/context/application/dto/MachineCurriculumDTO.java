package com.app.usochicamochabackend.context.application.dto;

import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.performance.application.dto.ResultDTO;

import java.math.BigDecimal;
import java.util.List;

public record MachineCurriculumDTO(MachineResponse machine, List<ResultDTO> results) {}