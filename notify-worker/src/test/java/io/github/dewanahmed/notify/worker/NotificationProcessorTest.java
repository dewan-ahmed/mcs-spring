package io.github.dewanahmed.notify.worker;

import io.github.dewanahmed.notify.Notification;
import io.github.dewanahmed.notify.NotificationSender;
import io.github.dewanahmed.notify.NotificationStatus;
import io.github.dewanahmed.notify.memory.InMemoryNotificationStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationProcessorTest {

    @Test
    void processMarksNotificationSent() {
        var store = new InMemoryNotificationStore();
        var notification = sample();
        store.add(notification);
        var processor = new NotificationProcessor(store, item -> "mem-test");

        processor.process(notification);

        assertEquals(NotificationStatus.SENT, store.findById(notification.id()).orElseThrow().status());
    }

    @Test
    void processMarksNotificationFailedWhenSenderThrows() {
        var store = new InMemoryNotificationStore();
        var notification = sample();
        store.add(notification);
        NotificationSender sender = item -> {
            throw new IllegalStateException("provider down");
        };
        var processor = new NotificationProcessor(store, sender);

        processor.process(notification);

        assertEquals(NotificationStatus.FAILED, store.findById(notification.id()).orElseThrow().status());
    }

    private static Notification sample() {
        return new Notification(UUID.randomUUID(), "user@example.com", "hello", NotificationStatus.QUEUED, Instant.EPOCH);
    }
}
