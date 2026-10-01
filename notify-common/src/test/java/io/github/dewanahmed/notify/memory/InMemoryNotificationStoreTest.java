package io.github.dewanahmed.notify.memory;

import io.github.dewanahmed.notify.Notification;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryNotificationStoreTest {

    @Test
    void addThenFindByIdReturnsTheSameNotification() {
        var store = new InMemoryNotificationStore();
        var notification = sample("queued");

        store.add(notification);

        assertEquals(notification, store.findById(notification.id()).orElseThrow());
    }

    @Test
    void findByIdUnknownIdIsEmpty() {
        var store = new InMemoryNotificationStore();
        assertTrue(store.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    void updateStatusReplacesStatusWhenPresent() {
        var store = new InMemoryNotificationStore();
        var notification = sample("queued");
        store.add(notification);

        assertTrue(store.updateStatus(notification.id(), "sent"));
        assertEquals("sent", store.findById(notification.id()).orElseThrow().status());
    }

    @Test
    void updateStatusUnknownIdReturnsFalse() {
        var store = new InMemoryNotificationStore();
        assertFalse(store.updateStatus(UUID.randomUUID(), "sent"));
    }

    @Test
    void addRejectsDuplicateId() {
        var store = new InMemoryNotificationStore();
        var notification = sample("queued");
        store.add(notification);
        assertThrows(IllegalStateException.class, () -> store.add(notification));
    }

    private static Notification sample(String status) {
        return new Notification(UUID.randomUUID(), "user@example.com", "hello", status, Instant.EPOCH);
    }
}
