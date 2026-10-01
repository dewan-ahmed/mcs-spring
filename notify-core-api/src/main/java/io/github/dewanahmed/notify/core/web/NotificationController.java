package io.github.dewanahmed.notify.core.web;

import io.github.dewanahmed.notify.Notification;
import io.github.dewanahmed.notify.NotificationQueue;
import io.github.dewanahmed.notify.NotificationStatus;
import io.github.dewanahmed.notify.NotificationStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationStore store;
    private final NotificationQueue queue;

    public NotificationController(NotificationStore store, NotificationQueue queue) {
        this.store = store;
        this.queue = queue;
    }

    @PostMapping
    public ResponseEntity<Notification> create(@Valid @RequestBody CreateNotificationRequest request, Authentication authentication) {
        Notification notification = new Notification(
                UUID.randomUUID(),
                request.recipient().trim(),
                request.body().trim(),
                NotificationStatus.QUEUED,
                Instant.now());
        store.add(notification);
        queue.enqueue(notification);
        log.info("notification created id={} recipient={} actor={} status={}",
                notification.id(), notification.recipient(), authentication.getName(), notification.status());
        return ResponseEntity.created(URI.create("/api/notifications/" + notification.id())).body(notification);
    }

    @GetMapping("/{id}")
    public Notification get(@PathVariable UUID id) {
        return store.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Notification not found"));
    }

    public record CreateNotificationRequest(
            @NotBlank @Size(max = 320) String recipient,
            @NotBlank @Size(max = 4000) String body) {
    }
}
