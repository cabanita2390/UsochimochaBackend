package com.app.usochicamochabackend.actions.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.actions.infrastructure.entity.ActionEntity;
import com.app.usochicamochabackend.actions.infrastructure.repository.ActionRepository;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActionsService implements SaveActionUseCase {

    private final UserRepositoryJpa userRepositoryJpa;
    private final ActionRepository actionRepository;

    @Override
    public void save(String details) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserEntity user = userRepositoryJpa.getUserEntityById(userPrincipal.id());

        actionRepository.save(new ActionEntity(null, details, user));
    }
}
