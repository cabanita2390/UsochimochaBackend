package com.app.usochicamochabackend.notifications.application;

import com.app.usochicamochabackend.notifications.infrastructure.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationWebSocketHandler webSocketHandler;
    private final ConcurrentHashMap<String, Long> notificationStats = new ConcurrentHashMap<>();

    /**
     * Send inspection-specific notification via WebSocket
     */
    public void notifyInspection(String inspectionData) {
        if (inspectionData != null) {
            log.debug("Sending inspection WebSocket notification: {}", inspectionData);
            webSocketHandler.broadcastInspection(inspectionData);
            recordNotificationStats("inspection");
        }
    }

    /**
     * Send oil change-specific notification via WebSocket
     */
    public void notifyOilChange(String oilChangeData) {
        if (oilChangeData != null) {
            log.debug("Sending oil change WebSocket notification: {}", oilChangeData);
            webSocketHandler.broadcastOilChange(oilChangeData);
            recordNotificationStats("oil-change");
        }
    }

    /**
     * Send notification to specific user via WebSocket
     */
    public void notifyUser(String username, String notification) {
        if (notification != null && username != null) {
            log.debug("Sending user-specific WebSocket notification to {}: {}", username, notification);
            webSocketHandler.sendToUser(username, notification);
            recordNotificationStats("user-specific");
        }
    }

    /**
     * Send connection status notification
     */
    public void notifyConnectionStatus(String status) {
        if (status != null) {
            log.debug("Sending connection status: {}", status);
            webSocketHandler.sendConnectionStatus(status);
            recordNotificationStats("connection-status");
        }
    }

    /**
     * Record notification statistics
     */
    private void recordNotificationStats(String type) {
        notificationStats.merge(type, 1L, Long::sum);
    }

    /**
     * Get notification statistics
     */
    public ConcurrentHashMap<String, Long> getNotificationStats() {
        return new ConcurrentHashMap<>(notificationStats);
    }

    /**
     * Reset notification statistics
     */
    public void resetNotificationStats() {
        notificationStats.clear();
        log.debug("Notification statistics reset");
    }

    /**
     * Send SOAT/RUNT notification via WebSocket
     */
    public void notifySoatRunt(String soatRuntData) {
        if (soatRuntData != null) {
            log.debug("Sending SOAT/RUNT WebSocket notification: {}", soatRuntData);
            webSocketHandler.broadcastSoatRuntNotification(soatRuntData);
            recordNotificationStats("soat-runt");
        }
    }

    /**
     * Send SOAT/RUNT stream status via WebSocket
     */
    public void notifySoatRuntStreamStatus(String status) {
        if (status != null) {
            log.debug("Sending SOAT/RUNT stream status WebSocket notification: {}", status);
            webSocketHandler.broadcastSoatRuntStreamStatus(status);
            recordNotificationStats("soat-runt-stream");
        }
    }

    /**
     * Send SOAT/RUNT notification to specific user via WebSocket
     */
    public void notifySoatRuntUser(String username, String soatRuntData) {
        if (soatRuntData != null && username != null) {
            log.debug("Sending SOAT/RUNT WebSocket notification to user {}: {}", username, soatRuntData);
            webSocketHandler.sendSoatRuntToUser(username, soatRuntData);
            recordNotificationStats("soat-runt-user");
        }
    }
}