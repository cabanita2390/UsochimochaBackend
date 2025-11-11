package com.app.usochicamochabackend.notifications.web;

import com.app.usochicamochabackend.config.TestWebConfig;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestWebConfig.class)
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void streamNotifications_ShouldReturnServerSentEvents() throws Exception {
        // Given
        BlockingQueue<String> notifications = new LinkedBlockingQueue<>();
        notifications.offer("Test notification 1");
        notifications.offer("Test notification 2");
        when(notificationService.getNotifications()).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/new-data/notifications/stream")
                .with(csrf())
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void streamNotifications_ShouldHandleEmptyStream() throws Exception {
        // Given
        BlockingQueue<String> emptyNotifications = new LinkedBlockingQueue<>();
        when(notificationService.getNotifications()).thenReturn(emptyNotifications);

        // When & Then
        mockMvc.perform(get("/new-data/notifications/stream")
                .with(csrf())
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());
    }
}
