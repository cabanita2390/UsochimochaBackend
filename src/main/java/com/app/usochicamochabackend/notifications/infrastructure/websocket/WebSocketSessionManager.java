package com.app.usochicamochabackend.notifications.infrastructure.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket Session Manager
 * 
 * This component manages WebSocket session lifecycle, tracking connected clients,
 * handling connection events, and providing session statistics.
 */
@Component
@Slf4j
public class WebSocketSessionManager {

    private final ConcurrentHashMap<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicInteger totalDisconnections = new AtomicInteger(0);
    
    /**
     * Track WebSocket session connections
     */
    @EventListener
    public void handleWebSocketConnection(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String username = extractUsername(headerAccessor);
        
        SessionInfo sessionInfo = new SessionInfo(
            sessionId,
            username,
            "unknown",
            System.currentTimeMillis()
        );
        
        activeSessions.put(sessionId, sessionInfo);
        totalConnections.incrementAndGet();
        
        log.info("WebSocket session connected - ID: {}, Username: {}, Total active: {}", 
                sessionId, username, activeSessions.size());
        
        // Send connection statistics update
        broadcastConnectionStats();
    }

    /**
     * Track WebSocket session disconnections
     */
    @EventListener
    public void handleWebSocketDisconnection(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        SessionInfo removedSession = activeSessions.remove(sessionId);
        totalDisconnections.incrementAndGet();
        
        log.info("WebSocket session disconnected - ID: {}, Username: {}, Total active: {}", 
                sessionId, removedSession != null ? removedSession.getUsername() : "unknown", activeSessions.size());
        
        // Send connection statistics update
        broadcastConnectionStats();
    }

    /**
     * Extract username from STOMP headers
     */
    private String extractUsername(StompHeaderAccessor headerAccessor) {
        return headerAccessor.getUser() != null ? 
                headerAccessor.getUser().getName() : "anonymous";
    }

    /**
     * Get all active session information
     */
    public ConcurrentHashMap<String, SessionInfo> getActiveSessions() {
        return new ConcurrentHashMap<>(activeSessions);
    }

    /**
     * Get connection statistics
     */
    public ConnectionStats getConnectionStats() {
        return new ConnectionStats(
            activeSessions.size(),
            totalConnections.get(),
            totalDisconnections.get(),
            System.currentTimeMillis()
        );
    }

    /**
     * Reset connection statistics
     */
    public void resetStats() {
        totalConnections.set(0);
        totalDisconnections.set(0);
        log.debug("WebSocket connection statistics reset");
    }

    /**
     * Send connection statistics to all connected clients
     */
    public void broadcastConnectionStats() {
        ConnectionStats stats = getConnectionStats();
        log.debug("Broadcasting connection stats: {}", stats);
    }

    /**
     * Check if a session is active
     */
    public boolean isSessionActive(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    /**
     * Remove a specific session
     */
    public void removeSession(String sessionId) {
        SessionInfo removedSession = activeSessions.remove(sessionId);
        if (removedSession != null) {
            log.info("Manually removed session: {}", sessionId);
            totalDisconnections.incrementAndGet();
        }
    }

    /**
     * Session information container
     */
    public static class SessionInfo {
        private final String sessionId;
        private final String username;
        private final String remoteAddress;
        private final long connectedAt;

        public SessionInfo(String sessionId, String username, String remoteAddress, long connectedAt) {
            this.sessionId = sessionId;
            this.username = username;
            this.remoteAddress = remoteAddress;
            this.connectedAt = connectedAt;
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public String getUsername() { return username; }
        public String getRemoteAddress() { return remoteAddress; }
        public long getConnectedAt() { return connectedAt; }
        
        public long getConnectionDuration() {
            return System.currentTimeMillis() - connectedAt;
        }
    }

    /**
     * Connection statistics container
     */
    public static class ConnectionStats {
        private final int activeConnections;
        private final int totalConnections;
        private final int totalDisconnections;
        private final long timestamp;

        public ConnectionStats(int activeConnections, int totalConnections, int totalDisconnections, long timestamp) {
            this.activeConnections = activeConnections;
            this.totalConnections = totalConnections;
            this.totalDisconnections = totalDisconnections;
            this.timestamp = timestamp;
        }

        // Getters
        public int getActiveConnections() { return activeConnections; }
        public int getTotalConnections() { return totalConnections; }
        public int getTotalDisconnections() { return totalDisconnections; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("Active: %d, Total Connected: %d, Total Disconnected: %d", 
                    activeConnections, totalConnections, totalDisconnections);
        }
    }
}