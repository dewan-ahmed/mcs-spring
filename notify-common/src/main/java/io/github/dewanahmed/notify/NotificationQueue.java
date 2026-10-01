package io.github.dewanahmed.notify;

import java.time.Duration;

/**
 * Hands work to a consumer. In production this would be a message broker.
 */
public interface NotificationQueue {

    void enqueue(Notification notification);

    /**
     * @return the next notification, or {@code null} when {@code timeout} elapses
     */
    Notification dequeue(Duration timeout) throws InterruptedException;
}
