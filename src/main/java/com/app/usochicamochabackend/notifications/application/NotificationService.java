package com.app.usochicamochabackend.notifications.application;

import org.springframework.stereotype.Service;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class NotificationService {

    private final BlockingQueue<String> notificationsQueue = new LinkedBlockingQueue<>();

    public void notify(String event) {
        if (event != null) {
            notificationsQueue.offer(event);
        }
    }

    public BlockingQueue<String> getNotifications() {
        return notificationsQueue;
    }
}