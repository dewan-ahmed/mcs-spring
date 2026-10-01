package io.github.dewanahmed.notify.memory;

import io.github.dewanahmed.notify.Notification;
import io.github.dewanahmed.notify.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

/**
 * No-op sender that records a delivery and returns a fake provider identifier.
 */
public final class InMemoryNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(InMemoryNotificationSender.class);

    @Override
    public String send(Notification notification) {
        Objects.requireNonNull(notification, "notification");
        String providerId = "mem-" + UUID.randomUUID().toString().replace("-", "");
        log.info("delivered notification id={} recipient={} providerId={}",
                notification.id(), notification.recipient(), providerId);
        return providerId;
    }
}
