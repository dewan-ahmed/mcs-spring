package io.github.dewanahmed.notify.worker;

import io.github.dewanahmed.notify.NotificationQueue;
import io.github.dewanahmed.notify.NotificationStore;
import io.github.dewanahmed.notify.memory.InMemoryNotificationQueue;
import io.github.dewanahmed.notify.memory.InMemoryNotificationStore;
import io.github.dewanahmed.notify.memory.InMemoryNotificationSender;
import io.github.dewanahmed.notify.NotificationSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationBeans {

    @Bean
    NotificationStore notificationStore() {
        return new InMemoryNotificationStore();
    }

    @Bean
    NotificationQueue notificationQueue() {
        return new InMemoryNotificationQueue();
    }

    @Bean
    NotificationSender notificationSender() {
        return new InMemoryNotificationSender();
    }
}
