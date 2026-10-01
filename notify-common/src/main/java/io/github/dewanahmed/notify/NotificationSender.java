package io.github.dewanahmed.notify;

/**
 * Delivers a notification to a recipient. In production this would be an SMS or email provider.
 */
public interface NotificationSender {

    String send(Notification notification);
}
