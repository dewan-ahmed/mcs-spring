package io.github.dewanahmed.notify.publicapi.web;

import io.github.dewanahmed.notify.NotificationStatus;
import io.github.dewanahmed.notify.NotificationStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class CallbackController {

    private static final Logger log = LoggerFactory.getLogger(CallbackController.class);

    private final NotificationStore store;

    public CallbackController(NotificationStore store) {
        this.store = store;
    }

    @PostMapping("/api/callbacks/status")
    public ResponseEntity<Void> updateStatus(@Valid @RequestBody StatusCallbackRequest request) {
        if (!NotificationStatus.isAllowed(request.status())) {
            throw new ResponseStatusException(BAD_REQUEST, "Unsupported status");
        }
        String status = NotificationStatus.normalize(request.status());
        if (!store.updateStatus(request.id(), status)) {
            throw new ResponseStatusException(NOT_FOUND, "Notification not found");
        }
        log.info("status callback id={} status={}", request.id(), status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/notifications/{id}/status")
    public StatusResponse status(@PathVariable UUID id) {
        return store.findById(id)
                .map(notification -> new StatusResponse(notification.id(), notification.status()))
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Notification not found"));
    }

    public record StatusCallbackRequest(@NotNull UUID id, @NotBlank @Size(max = 32) String status) {
    }

    public record StatusResponse(UUID id, String status) {
    }
}
