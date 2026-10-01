package io.github.dewanahmed.notify;

import java.util.Optional;
import java.util.UUID;

/**
 * Persists notifications. In production this would be a SQL database.
 */
public interface NotificationStore {

    void add(Notification notification);

    Optional<Notification> findById(UUID id);

    boolean updateStatus(UUID id, String status);
}
