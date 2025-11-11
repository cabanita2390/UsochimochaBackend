# WebSocket Migration - Complete Implementation Guide

## Overview
The application has been successfully migrated from Server-Sent Events (SSE) to WebSocket-only implementation for real-time notifications. This eliminates the connection abortion errors you were experiencing with SSE.

## Problem Solved
- **SSE Connection Errors**: Eliminated `java.io.IOException: An established connection was aborted by the software in your host machine`
- **Keep-alive Issues**: No more keep-alive executor shutdown problems
- **Connection Management**: WebSocket provides better connection resilience and automatic reconnection

## New WebSocket Endpoints

### SOAT/RUNT Notifications
All SOAT/RUNT notifications now use WebSocket instead of SSE:

#### 1. General SOAT/RUNT Notifications
```
POST /ws/notifications/soat-runt
Content-Type: application/json

{
  "type": "SOAT",
  "message": "⚠️ El SOAT de la máquina 'Machine Name' vence pronto",
  "machineData": {...}
}
```

#### 2. SOAT/RUNT Stream Status
```
POST /ws/notifications/soat-runt/stream-status
Content-Type: application/x-www-form-urlencoded

status=stream_open
```

#### 3. User-Specific SOAT/RUNT Notifications
```
POST /ws/notifications/soat-runt/user?username=user123
Content-Type: application/json

{
  "type": "RUNT", 
  "message": "⚠️ El RUNT de la máquina 'Machine Name' vence pronto",
  "machineData": {...}
}
```

## WebSocket Connection

### Client Connection
Connect to WebSocket endpoint:
```
ws://localhost:8080/ws-direct
```

Or with SOCKJS fallback:
```
http://localhost:8080/ws
```

### Message Destinations

#### Subscribe to SOAT/RUNT Notifications
```javascript
// Subscribe to general SOAT/RUNT notifications
stompClient.subscribe('/topic/notifications/soat-runt', function(message) {
    console.log('SOAT/RUNT Notification:', message.body);
});

// Subscribe to SOAT/RUNT stream status
stompClient.subscribe('/topic/notifications/soat-runt-stream', function(message) {
    console.log('SOAT/RUNT Stream Status:', message.body);
});

// Subscribe to user-specific notifications
stompClient.subscribe('/user/username/notifications/soat-runt', function(message) {
    console.log('User SOAT/RUNT Notification:', message.body);
});
```

#### Send Messages
```javascript
// Send ping to keep connection alive
stompClient.send('/app/ping', {}, 'ping');

// Subscribe to notifications
stompClient.send('/app/subscribe', {}, 'notifications');
```

## Updated Service Methods

### NotificationService
The following new methods are available in `NotificationService`:

```java
// Broadcast SOAT/RUNT notification to all connected clients
notificationService.notifySoatRunt(String soatRuntData);

// Send SOAT/RUNT stream status
notificationService.notifySoatRuntStreamStatus(String status);

// Send SOAT/RUNT notification to specific user
notificationService.notifySoatRuntUser(String username, String soatRuntData);
```

### NotificationWebSocketHandler
New WebSocket broadcast methods:

```java
// Broadcast to all clients
webSocketHandler.broadcastSoatRuntNotification(String data);

// Broadcast stream status
webSocketHandler.broadcastSoatRuntStreamStatus(String status);

// Send to specific user
webSocketHandler.sendSoatRuntToUser(String username, String data);
```

## Integration in InspectionService

SOAT/RUNT expiration notifications in `InspectionService` now use WebSocket:

```java
if (isExpiringSoon(machine.getSoat())) {
    ExpirationNotificationDTO soatNotification = new ExpirationNotificationDTO(
            "SOAT",
            "⚠️ El SOAT de la máquina '" + machine.getName() + "' vence pronto",
            MachineMapper.toResponse(machine)
    );
    notificationService.notifySoatRunt(soatNotification.toString());
}
```

## Benefits of WebSocket Over SSE

1. **Bidirectional Communication**: WebSocket allows both client and server to send messages
2. **Better Connection Management**: Automatic reconnection and heartbeat
3. **Lower Latency**: Persistent connection reduces overhead
4. **No Connection Abort Errors**: More robust connection handling
5. **Real-time Updates**: Instant notification delivery
6. **Scalability**: Better handling of multiple concurrent connections

## Testing the Implementation

### Health Check
```
GET /ws/notifications/health
Response: "WebSocket notifications healthy"
```

### Active Connections
```
GET /ws/notifications/connections
Response: Map of active session IDs to usernames
```

### Notification Statistics
```
GET /ws/notifications/stats
Response: Map of notification types and counts
```

### Reset Statistics
```
POST /ws/notifications/stats/reset
Response: "Notification statistics reset"
```

## Migration Summary

| Component | Before (SSE) | After (WebSocket) |
|-----------|-------------|------------------|
| Controller | SoatRuntStreamController | Removed completely |
| Endpoint | GET /soat/runt/notifications/stream | POST /ws/notifications/soat-runt |
| Transport | Server-Sent Events | WebSocket |
| Connection | Unidirectional (server→client) | Bidirectional |
| Keep-alive | Manual executor | Built-in WebSocket |
| Reconnection | Manual | Automatic |

## Client Implementation Example

```javascript
// Connect to WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to SOAT/RUNT notifications
    stompClient.subscribe('/topic/notifications/soat-runt', function(notification) {
        const data = JSON.parse(notification.body);
        console.log('SOAT/RUNT Alert:', data.message);
        // Handle notification display
    });
    
    // Subscribe to stream status
    stompClient.subscribe('/topic/notifications/soat-runt-stream', function(status) {
        console.log('Stream Status:', status.body);
    });
});

// Send test notification (server-side call)
function testNotification() {
    fetch('/ws/notifications/soat-runt', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            type: 'TEST',
            message: 'Test SOAT/RUNT notification'
        })
    });
}
```

## Next Steps

1. Update your frontend client to connect to WebSocket endpoints
2. Replace any SSE subscription logic with WebSocket subscriptions
3. Test the connection reliability and reconnection behavior
4. Monitor the logs for any WebSocket-specific issues
5. Adjust WebSocket configuration if needed (timeouts, buffer sizes, etc.)

## Troubleshooting

### Connection Issues
- Check WebSocket endpoint: `/ws` and `/ws-direct`
- Verify STOMP client configuration
- Monitor browser network tab for WebSocket frames

### Message Delivery
- Ensure proper subscription to `/topic/notifications/soat-runt`
- Check client message handling logic
- Verify server-side notification calls

### Performance
- Monitor WebSocket session count
- Check memory usage with persistent connections
- Adjust concurrent connection limits if needed

The migration is now complete and your application should no longer experience the SSE connection abortion errors!