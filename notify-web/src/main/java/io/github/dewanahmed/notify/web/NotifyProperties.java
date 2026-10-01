package io.github.dewanahmed.notify.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "notify")
public class NotifyProperties {

    private String appEnvironment = "unknown";

    public String getAppEnvironment() {
        return appEnvironment;
    }

    public void setAppEnvironment(String appEnvironment) {
        this.appEnvironment = appEnvironment;
    }
}
