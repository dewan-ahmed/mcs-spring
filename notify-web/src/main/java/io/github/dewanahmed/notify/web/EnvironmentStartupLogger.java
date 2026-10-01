package io.github.dewanahmed.notify.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentStartupLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentStartupLogger.class);

    private final NotifyProperties properties;
    private final Environment environment;

    public EnvironmentStartupLogger(NotifyProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String name = environment.getProperty("spring.application.name", "notify");
        log.info("application={} starting appEnvironment={}", name, properties.getAppEnvironment());
    }
}
