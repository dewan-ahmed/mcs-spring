package io.github.dewanahmed.notify.worker;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "notify.worker")
public class WorkerProperties {

    private Duration syntheticInterval = Duration.ofSeconds(30);

    public Duration getSyntheticInterval() {
        return syntheticInterval;
    }

    public void setSyntheticInterval(Duration syntheticInterval) {
        this.syntheticInterval = syntheticInterval;
    }
}
