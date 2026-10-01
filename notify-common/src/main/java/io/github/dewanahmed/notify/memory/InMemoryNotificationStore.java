package io.github.dewanahmed.notify.memory;

import io.github.dewanahmed.notify.Notification;
import io.github.dewanahmed.notify.NotificationStore;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryNotificationStore implements NotificationStore {

    private final ConcurrentHashMap<UUID, Notification> items = new ConcurrentHashMap<>();

    @Override
    public void add(Notification notification) {
        Objects.requireNonNull(notification, "notification");
        Notification existing = items.putIfAbsent(notification.id(), notification);
        if (existing != null) {
            throw new IllegalStateException("Notification '" + notification.id() + "' already exists.");
        }
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public boolean updateStatus(UUID id, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status must not be blank");
        }
        return items.computeIfPresent(id, (key, current) -> current.withStatus(status)) != null;
    }
}
