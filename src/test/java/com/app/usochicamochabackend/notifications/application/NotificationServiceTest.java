package com.app.usochicamochabackend.notifications.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    @Test
    void notify_ShouldAddNotificationToQueue() {
        // Given
        String testEvent = "Test notification event";
        BlockingQueue<String> notifications = notificationService.getNotifications();

        // When
        notificationService.notify(testEvent);

        // Then
        assertEquals(testEvent, notifications.poll());
    }

    @Test
    void notify_ShouldAddMultipleNotificationsToQueue() {
        // Given
        String event1 = "First notification";
        String event2 = "Second notification";
        String event3 = "Third notification";
        BlockingQueue<String> notifications = notificationService.getNotifications();

        // When
        notificationService.notify(event1);
        notificationService.notify(event2);
        notificationService.notify(event3);

        // Then
        assertEquals(event1, notifications.poll());
        assertEquals(event2, notifications.poll());
        assertEquals(event3, notifications.poll());
    }

    @Test
    void getNotifications_ShouldReturnQueue() {
        // When
        BlockingQueue<String> notifications = notificationService.getNotifications();

        // Then
        assertNotNull(notifications);
    }

    @Test
    void notify_ShouldHandleEmptyString() {
        // Given
        String emptyEvent = "";
        BlockingQueue<String> notifications = notificationService.getNotifications();

        // When
        notificationService.notify(emptyEvent);

        // Then
        assertEquals(emptyEvent, notifications.poll());
    }

    @Test
    void notify_ShouldHandleNullValue() {
        // Given
        BlockingQueue<String> notifications = notificationService.getNotifications();
        int initialSize = notifications.size();

        // When
        notificationService.notify(null);

        // Then - size should not change since null events are not added
        assertEquals(initialSize, notifications.size());
    }
}
