package com.app.usochicamochabackend.notifications.web;

import com.app.usochicamochabackend.config.TestWebConfig;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.notifications.infrastructure.websocket.NotificationWebSocketHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestWebConfig.class)
@WebMvcTest(NotificationWebSocketController.class)
class NotificationWebSocketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationWebSocketHandler webSocketHandler;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendInspection_ShouldReturnSuccess() throws Exception {
        // Given
        String inspectionData = "{\"machineId\": 1, \"type\": \"inspection\"}";

        // When & Then
        mockMvc.perform(post("/ws/notifications/inspection")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(inspectionData))
                .andExpect(status().isOk())
                .andExpect(content().string("WebSocket inspection notification sent"));

        verify(notificationService).notifyInspection(inspectionData);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendOilChange_ShouldReturnSuccess() throws Exception {
        // Given
        String oilChangeData = "{\"machineId\": 1, \"type\": \"oil-change\"}";

        // When & Then
        mockMvc.perform(post("/ws/notifications/oil-change")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(oilChangeData))
                .andExpect(status().isOk())
                .andExpect(content().string("WebSocket oil change notification sent"));

        verify(notificationService).notifyOilChange(oilChangeData);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendUserNotification_ShouldReturnSuccess() throws Exception {
        // Given
        String username = "testuser";
        String notification = "Test notification";

        // When & Then
        mockMvc.perform(post("/ws/notifications/user")
                .with(csrf())
                .param("username", username)
                .param("notification", notification))
                .andExpect(status().isOk())
                .andExpect(content().string("WebSocket user notification sent to " + username));

        verify(notificationService).notifyUser(username, notification);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendSoatRunt_ShouldReturnSuccess() throws Exception {
        // Given
        String soatRuntData = "{\"type\": \"SOAT\", \"machineId\": 1}";

        // When & Then
        mockMvc.perform(post("/ws/notifications/soat-runt")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(soatRuntData))
                .andExpect(status().isOk())
                .andExpect(content().string("SOAT/RUNT WebSocket notification sent"));

        verify(notificationService).notifySoatRunt(soatRuntData);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendSoatRuntStreamStatus_ShouldReturnSuccess() throws Exception {
        // Given
        String status = "stream_open";

        // When & Then
        mockMvc.perform(post("/ws/notifications/soat-runt/stream-status")
                .with(csrf())
                .param("status", status))
                .andExpect(status().isOk())
                .andExpect(content().string("SOAT/RUNT stream status WebSocket notification sent"));

        verify(notificationService).notifySoatRuntStreamStatus(status);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void sendSoatRuntUser_ShouldReturnSuccess() throws Exception {
        // Given
        String username = "testuser";
        String soatRuntData = "{\"type\": \"SOAT\", \"machineId\": 1}";

        // When & Then
        mockMvc.perform(post("/ws/notifications/soat-runt/user")
                .with(csrf())
                .param("username", username)
                .contentType(MediaType.APPLICATION_JSON)
                .content(soatRuntData))
                .andExpect(status().isOk())
                .andExpect(content().string("SOAT/RUNT WebSocket user notification sent to " + username));

        verify(notificationService).notifySoatRuntUser(username, soatRuntData);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void health_ShouldReturnHealthy() throws Exception {
        // When & Then
        mockMvc.perform(get("/ws/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("WebSocket notifications healthy"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getActiveConnections_ShouldReturnConnections() throws Exception {
        // When & Then
        mockMvc.perform(get("/ws/notifications/connections"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void getNotificationStats_ShouldReturnStats() throws Exception {
//        // When & Then
//        mockMvc.perform(get("/ws/notifications/stats"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetStats_ShouldReturnSuccess() throws Exception {
        // When & Then
        mockMvc.perform(post("/ws/notifications/stats/reset")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification statistics reset"));
    }
}
