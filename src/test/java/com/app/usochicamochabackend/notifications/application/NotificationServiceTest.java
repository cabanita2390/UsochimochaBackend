opackage com.app.usochicamochabackend.notifications.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
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
        Flux<String> notifications = notificationService.getNotifications();

        // When & Then
        StepVerifier.create(notifications)
                .then(() -> notificationService.notify(null))
                .expectNoEvent(Duration.ofMillis(100))
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }
}
