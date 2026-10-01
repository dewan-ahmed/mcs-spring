package io.github.dewanahmed.notify.memory;

import io.github.dewanahmed.notify.Notification;
import io.github.dewanahmed.notify.NotificationQueue;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class InMemoryNotificationQueue implements NotificationQueue {

    private final LinkedBlockingQueue<Notification> items = new LinkedBlockingQueue<>();

    @Override
    public void enqueue(Notification notification) {
        Objects.requireNonNull(notification, "notification");
        items.add(notification);
    }

    @Override
    public Notification dequeue(Duration timeout) throws InterruptedException {
        Objects.requireNonNull(timeout, "timeout");
        return items.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }
}
