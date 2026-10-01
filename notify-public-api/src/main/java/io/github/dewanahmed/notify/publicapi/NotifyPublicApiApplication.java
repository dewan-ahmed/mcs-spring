package io.github.dewanahmed.notify.publicapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.github.dewanahmed.notify")
public class NotifyPublicApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifyPublicApiApplication.class, args);
    }
}
