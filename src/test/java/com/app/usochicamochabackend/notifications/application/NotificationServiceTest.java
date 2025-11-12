package com.app.usochicamochabackend.notifications.application;

import com.app.usochicamochabackend.notifications.infrastructure.websocket.NotificationWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private NotificationService notificationService;
    private NotificationWebSocketHandler webSocketHandler;

    @BeforeEach
    void setUp() {
        webSocketHandler = mock(NotificationWebSocketHandler.class);
        notificationService = new NotificationService(webSocketHandler);
    }

    @Test
    void notifyInspection_ShouldCallWebSocketHandler() {
        // Given
        String inspectionData = "{\"machineId\": 1, \"type\": \"inspection\"}";

        // When
        notificationService.notifyInspection(inspectionData);

        // Then
        verify(webSocketHandler).broadcastInspection(inspectionData);
        // Verify statistics are recorded
        var stats = notificationService.getNotificationStats();
        assertTrue(stats.containsKey("inspection"));
        assertEquals(1L, stats.get("inspection"));
    }

    @Test
    void notifyInspection_ShouldNotHandleNullData() {
        // When
        notificationService.notifyInspection(null);

        // Then
        verify(webSocketHandler, never()).broadcastInspection(anyString());
    }

    @Test
    void notifyOilChange_ShouldCallWebSocketHandler() {
        // Given
        String oilChangeData = "{\"machineId\": 1, \"type\": \"oil-change\"}";

        // When
        notificationService.notifyOilChange(oilChangeData);

        // Then
        verify(webSocketHandler).broadcastOilChange(oilChangeData);
        // Verify statistics are recorded
        var stats = notificationService.getNotificationStats();
        assertTrue(stats.containsKey("oil-change"));
        assertEquals(1L, stats.get("oil-change"));
    }

    @Test
    void notifyOilChange_ShouldNotHandleNullData() {
        // When
        notificationService.notifyOilChange(null);

        // Then
        verify(webSocketHandler, never()).broadcastOilChange(anyString());
    }

    @Test
    void notifyUser_ShouldCallWebSocketHandlerWithUsername() {
        // Given
        String username = "testuser";
        String notification = "Test notification";

        // When
        notificationService.notifyUser(username, notification);

        // Then
        verify(webSocketHandler).sendToUser(username, notification);
        // Verify statistics are recorded
        var stats = notificationService.getNotificationStats();
        assertTrue(stats.containsKey("user-specific"));
        assertEquals(1L, stats.get("user-specific"));
    }

    @Test
    void notifyUser_ShouldNotHandleNullParameters() {
        // When
        notificationService.notifyUser(null, "notification");
        notificationService.notifyUser("username", null);
        notificationService.notifyUser(null, null);

        // Then
        verify(webSocketHandler, never()).sendToUser(anyString(), anyString());
    }

    @Test
    void notifySoatRunt_ShouldCallWebSocketHandler() {
        // Given
        String soatRuntData = "{\"type\": \"SOAT\", \"machineId\": 1}";

        // When
        notificationService.notifySoatRunt(soatRuntData);

        // Then
        verify(webSocketHandler).broadcastSoatRuntNotification(soatRuntData);
        // Verify statistics are recorded
        var stats = notificationService.getNotificationStats();
        assertTrue(stats.containsKey("soat-runt"));
        assertEquals(1L, stats.get("soat-runt"));
    }

    @Test
    void notifySoatRuntStreamStatus_ShouldCallWebSocketHandler() {
        // Given
        String status = "stream_open";

        // When
        notificationService.notifySoatRuntStreamStatus(status);

        // Then
        verify(webSocketHandler).broadcastSoatRuntStreamStatus(status);
        // Verify statistics are recorded
        var stats = notificationService.getNotificationStats();
        assertTrue(stats.containsKey("soat-runt-stream"));
        assertEquals(1L, stats.get("soat-runt-stream"));
    }

    @Test
    void notifySoatRuntUser_ShouldCallWebSocketHandler() {
        // Given
        String username = "testuser";
        String soatRuntData = "{\"type\": \"SOAT\", \"machineId\": 1}";

        // When
        notificationService.notifySoatRuntUser(username, soatRuntData);

        // Then
        verify(webSocketHandler).sendSoatRuntToUser(username, soatRuntData);
        // Verify statistics are recorded
        var stats = notificationService.getNotificationStats();
        assertTrue(stats.containsKey("soat-runt-user"));
        assertEquals(1L, stats.get("soat-runt-user"));
    }

    @Test
    void getNotificationStats_ShouldReturnConcurrentHashMap() {
        // When
        var stats = notificationService.getNotificationStats();

        // Then
        assertNotNull(stats);
        assertTrue(stats instanceof ConcurrentHashMap);
        assertTrue(stats.isEmpty()); // Initially empty
    }

    @Test
    void resetNotificationStats_ShouldClearStatistics() {
        // Given - add some statistics
        notificationService.notifyInspection("test");
        notificationService.notifyOilChange("test");
        assertEquals(2, notificationService.getNotificationStats().size());

        // When
        notificationService.resetNotificationStats();

        // Then
        var stats = notificationService.getNotificationStats();
        assertEquals(0, stats.size());
    }

//    @Test
//    void multipleNotifications_ShouldAccumulateStatistics() {
//        // When
//        notificationService.notifyInspection("data1");
//        notificationService.notifyInspection("data2");
//        notificationService.notifyOilChange("data3");
//
//        // Then
//        var stats = notificationService.getNotificationStats();
//        assertEquals(3L, stats.get("inspection"));
//        assertEquals(1L, stats.get("oil-change"));
//    }
}
