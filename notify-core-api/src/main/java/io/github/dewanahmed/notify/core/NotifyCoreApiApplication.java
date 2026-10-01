package io.github.dewanahmed.notify.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "io.github.dewanahmed.notify")
public class NotifyCoreApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotifyCoreApiApplication.class, args);
    }
}
