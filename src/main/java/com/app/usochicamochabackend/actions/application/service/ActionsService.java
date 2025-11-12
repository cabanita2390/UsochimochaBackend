package com.app.usochicamochabackend.actions.application.service;

import com.app.usochicamochabackend.actions.application.port.GetAllActionsByUserIdUseCase;
import com.app.usochicamochabackend.actions.application.port.GetAllActionsUseCase;
import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.actions.infrastructure.entity.ActionEntity;
import com.app.usochicamochabackend.actions.infrastructure.repository.ActionRepository;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActionsService implements SaveActionUseCase, GetAllActionsByUserIdUseCase, GetAllActionsUseCase {

    private final UserRepositoryJpa userRepositoryJpa;
    private final ActionRepository actionRepository;
    private final NotificationService notificationService;


    @Override
    public void save(String details) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            UserEntity user = userRepositoryJpa.getUserEntityById(userPrincipal.id());
            actionRepository.save(new ActionEntity(null, details, user));
        } else {
            // For system actions or when no user is authenticated, save without user
            actionRepository.save(new ActionEntity(null, details, null));
        }
    }

    @Override
    public Page<ActionEntity> getAllActions(Pageable pageable) {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        save("El usuario " + userPrincipal.username() + " ha observado todas las acciones");

        

        return actionRepository.findAll(pageable);
    }

    @Override
    public Page<ActionEntity> getAllActionsByUserId(Long userId, Pageable pageable) {
        UserEntity user = userRepositoryJpa.getUserEntityById(userId);

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        save("El usuario " + userPrincipal.username() + " ha observado todas las acciones realizadas por el usuario " + user.getUsername());

        

        return actionRepository.findByUserId(userId, pageable);
    }
}
