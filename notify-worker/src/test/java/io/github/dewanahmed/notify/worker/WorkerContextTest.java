package io.github.dewanahmed.notify.worker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = "notify.worker.synthetic-interval=1h")
class WorkerContextTest {

    @Autowired
    private NotificationProcessor processor;

    @Test
    void contextStarts() {
        assertNotNull(processor);
    }
}
