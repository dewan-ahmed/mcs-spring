package io.github.dewanahmed.notify.worker;

import io.github.dewanahmed.notify.Notification;
import io.github.dewanahmed.notify.NotificationQueue;
import io.github.dewanahmed.notify.NotificationStatus;
import io.github.dewanahmed.notify.NotificationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NotificationWorkerLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorkerLifecycle.class);

    private final NotificationStore store;
    private final NotificationQueue queue;
    private final NotificationProcessor processor;
    private final WorkerProperties properties;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger threadNumber = new AtomicInteger();
    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "notify-worker-" + threadNumber.incrementAndGet());
        thread.setDaemon(false);
        return thread;
    });

    private volatile Future<?> producer;
    private volatile Future<?> consumer;

    public NotificationWorkerLifecycle(
            NotificationStore store,
            NotificationQueue queue,
            NotificationProcessor processor,
            WorkerProperties properties) {
        this.store = store;
        this.queue = queue;
        this.processor = processor;
        this.properties = properties;
    }

    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        log.info("worker running; shutdown finishes the in-flight item, then exits");
        producer = executor.submit(this::produce);
        consumer = executor.submit(this::consume);
    }

    @Override
    public void stop() {
        stop(() -> {
        });
    }

    @Override
    public void stop(Runnable callback) {
        if (running.compareAndSet(true, false)) {
            log.info("shutdown requested; in-flight notification will finish before exit");
            producer.cancel(true);
            try {
                consumer.get(30, TimeUnit.SECONDS);
            } catch (Exception exception) {
                log.warn("consumer did not finish cleanly", exception);
                consumer.cancel(true);
            }
            executor.shutdownNow();
        }
        callback.run();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private void produce() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            Notification notification = new Notification(
                    UUID.randomUUID(),
                    "worker@local",
                    "synthetic heartbeat",
                    NotificationStatus.QUEUED,
                    Instant.now());
            store.add(notification);
            queue.enqueue(notification);
            log.info("enqueued synthetic notification id={}", notification.id());
            try {
                Thread.sleep(properties.getSyntheticInterval().toMillis());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void consume() {
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Notification item = queue.dequeue(Duration.ofSeconds(1));
                if (item != null) {
                    processor.process(item);
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
