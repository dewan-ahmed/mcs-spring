package io.github.dewanahmed.notify.core.config;

import io.github.dewanahmed.notify.NotificationQueue;
import io.github.dewanahmed.notify.NotificationStore;
import io.github.dewanahmed.notify.memory.InMemoryNotificationQueue;
import io.github.dewanahmed.notify.memory.InMemoryNotificationStore;
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
}
