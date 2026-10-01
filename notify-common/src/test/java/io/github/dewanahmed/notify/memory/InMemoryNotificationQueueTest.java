package io.github.dewanahmed.notify.memory;

import io.github.dewanahmed.notify.Notification;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryNotificationQueueTest {

    @Test
    void dequeueReturnsEnqueuedItem() throws Exception {
        var queue = new InMemoryNotificationQueue();
        var notification = sample();

        queue.enqueue(notification);

        assertEquals(notification, queue.dequeue(Duration.ofSeconds(1)));
    }

    @Test
    void dequeueReturnsNullWhenEmptyAndTimeoutElapses() throws Exception {
        var queue = new InMemoryNotificationQueue();
        assertNull(queue.dequeue(Duration.ofMillis(20)));
    }

    private static Notification sample() {
        return new Notification(UUID.randomUUID(), "user@example.com", "hello", "queued", Instant.EPOCH);
    }
}

class InMemoryNotificationSenderTest {

    @Test
    void sendReturnsProviderId() {
        var sender = new InMemoryNotificationSender();
        String providerId = sender.send(sample());
        assertFalse(providerId.isBlank());
        assertTrue(providerId.startsWith("mem-"));
    }

    private static Notification sample() {
        return new Notification(UUID.randomUUID(), "user@example.com", "hello", "queued", Instant.EPOCH);
    }
}
