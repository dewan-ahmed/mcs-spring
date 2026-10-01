package io.github.dewanahmed.notify;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Notification(
        UUID id,
        String recipient,
        String body,
        String status,
        Instant createdAt) {

    public Notification {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(recipient, "recipient");
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public Notification withStatus(String status) {
        return new Notification(id, recipient, body, status, createdAt);
    }
}
