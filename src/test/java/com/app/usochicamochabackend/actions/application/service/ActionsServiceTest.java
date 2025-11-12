package com.app.usochicamochabackend.actions.application.service;

import com.app.usochicamochabackend.actions.infrastructure.entity.ActionEntity;
import com.app.usochicamochabackend.actions.infrastructure.repository.ActionRepository;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.utils.TestDataBuilder;
import com.app.usochicamochabackend.utils.TestSecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionsServiceTest {

    @Mock
    private UserRepositoryJpa userRepositoryJpa;

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ActionsService actionsService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        TestSecurityUtils.setUpSecurityContext(1L, "testuser", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void save_ShouldSaveAction() {
        // Given
        String details = "Test action performed";
        when(userRepositoryJpa.getUserEntityById(1L)).thenReturn(testUser);

        // When
        actionsService.save(details);

        // Then
        verify(userRepositoryJpa).getUserEntityById(1L);
        verify(actionRepository).save(argThat(action -> {
            assertNull(action.getId());
            assertEquals(details, action.getDetails());
            assertEquals(testUser, action.getUser());
            return true;
        }));
    }

    @Test
    void getAllActionsByUserId_ShouldReturnPagedActions() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<ActionEntity> actions = Arrays.asList(
                new ActionEntity(1L, "Action 1", testUser),
                new ActionEntity(2L, "Action 2", testUser)
        );
        Page<ActionEntity> actionPage = new PageImpl<>(actions, pageable, actions.size());
        when(userRepositoryJpa.getUserEntityById(userId)).thenReturn(testUser);
        when(actionRepository.findByUserId(userId, pageable)).thenReturn(actionPage);

        // When
        Page<ActionEntity> result = actionsService.getAllActionsByUserId(userId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Action 1", result.getContent().get(0).getDetails());
        assertEquals("Action 2", result.getContent().get(1).getDetails());
        verify(userRepositoryJpa, times(2)).getUserEntityById(userId);
        verify(actionRepository).findByUserId(userId, pageable);
        verify(actionRepository).save(any(ActionEntity.class)); // Action logging

    }

    @Test
    void getAllActions_ShouldReturnPagedActions() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<ActionEntity> actions = Arrays.asList(
                new ActionEntity(1L, "Action 1", testUser),
                new ActionEntity(2L, "Action 2", testUser)
        );
        Page<ActionEntity> actionPage = new PageImpl<>(actions, pageable, actions.size());
        when(actionRepository.findAll(pageable)).thenReturn(actionPage);

        // When
        Page<ActionEntity> result = actionsService.getAllActions(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(actionRepository).findAll(pageable);
        verify(actionRepository).save(any(ActionEntity.class)); // Action logging

    }
}
