package io.github.dewanahmed.notify.worker;

import io.github.dewanahmed.notify.Notification;
import io.github.dewanahmed.notify.NotificationSender;
import io.github.dewanahmed.notify.NotificationStatus;
import io.github.dewanahmed.notify.NotificationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationProcessor.class);

    private final NotificationStore store;
    private final NotificationSender sender;

    public NotificationProcessor(NotificationStore store, NotificationSender sender) {
        this.store = store;
        this.sender = sender;
    }

    public void process(Notification item) {
        try {
            String providerId = sender.send(item);
            store.updateStatus(item.id(), NotificationStatus.SENT);
            log.info("sent notification id={} recipient={} providerId={} status={}",
                    item.id(), item.recipient(), providerId, NotificationStatus.SENT);
        } catch (RuntimeException exception) {
            store.updateStatus(item.id(), NotificationStatus.FAILED);
            log.error("failed to send notification id={}", item.id(), exception);
        }
    }
}
