package com.app.usochicamochabackend.notifications.web;

import com.app.usochicamochabackend.notifications.application.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void streamNotifications_ShouldReturnServerSentEvents() throws Exception {
        // Given
        Flux<String> notifications = Flux.just("Test notification 1", "Test notification 2")
                .delayElements(Duration.ofMillis(100));
        when(notificationService.getNotifications()).thenReturn(notifications);

        // When & Then
        mockMvc.perform(get("/new-data/notifications/stream")
                .with(csrf())
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void streamNotifications_ShouldHandleEmptyStream() throws Exception {
        // Given
        Flux<String> emptyNotifications = Flux.empty();
        when(notificationService.getNotifications()).thenReturn(emptyNotifications);

        // When & Then
        mockMvc.perform(get("/new-data/notifications/stream")
                .with(csrf())
                .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE));
    }
}
