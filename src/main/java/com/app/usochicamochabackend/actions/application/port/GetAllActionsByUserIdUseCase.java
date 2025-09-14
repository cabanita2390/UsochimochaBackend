package com.app.usochicamochabackend.actions.application.port;

import com.app.usochicamochabackend.actions.infrastructure.entity.ActionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetAllActionsByUserIdUseCase {
    Page<ActionEntity> getAllActionsByUserId(Long userId, Pageable pageable);
}