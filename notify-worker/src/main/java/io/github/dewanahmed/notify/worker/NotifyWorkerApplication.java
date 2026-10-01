package io.github.dewanahmed.notify.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class NotifyWorkerApplication {

    private static final Logger log = LoggerFactory.getLogger(NotifyWorkerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(NotifyWorkerApplication.class, args);
    }

    @Bean
    CommandLineRunner logEnvironment(Environment environment) {
        return args -> log.info("Notify.Worker starting appEnvironment={}",
                environment.getProperty("notify.app-environment", "unknown"));
    }
}
