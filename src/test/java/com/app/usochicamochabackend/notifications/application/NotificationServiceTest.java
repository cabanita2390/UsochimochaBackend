package com.app.usochicamochabackend.notifications.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    @Test
    void notify_ShouldEmitNotification() {
        // Given
        String testEvent = "Test notification event";
        Flux<String> notifications = notificationService.getNotifications();

        // When & Then
        StepVerifier.create(notifications)
                .then(() -> notificationService.notify(testEvent))
                .expectNext(testEvent)
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void notify_ShouldEmitMultipleNotifications() {
        // Given
        String event1 = "First notification";
        String event2 = "Second notification";
        String event3 = "Third notification";
        Flux<String> notifications = notificationService.getNotifications();

        // When & Then
        StepVerifier.create(notifications)
                .then(() -> {
                    notificationService.notify(event1);
                    notificationService.notify(event2);
                    notificationService.notify(event3);
                })
                .expectNext(event1)
                .expectNext(event2)
                .expectNext(event3)
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void getNotifications_ShouldReturnFlux() {
        // When
        Flux<String> notifications = notificationService.getNotifications();

        // Then
        assertNotNull(notifications);
    }

    @Test
    void notify_ShouldHandleEmptyString() {
        // Given
        String emptyEvent = "";
        Flux<String> notifications = notificationService.getNotifications();

        // When & Then
        StepVerifier.create(notifications)
                .then(() -> notificationService.notify(emptyEvent))
                .expectNext(emptyEvent)
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void notify_ShouldHandleNullValue() {
        // Given
        Flux<String> notifications = notificationService.getNotifications();

        // When & Then
        StepVerifier.create(notifications)
                .then(() -> notificationService.notify(null))
                .expectNext((String) null)
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }
}
