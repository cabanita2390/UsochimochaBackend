package com.app.usochicamochabackend.context.application.port;

import com.app.usochicamochabackend.context.application.dto.MachineCurriculumDTO;

public interface GetMachineCurriculumUseCase {
    MachineCurriculumDTO getMachineCurriculum(Long machineId);
}