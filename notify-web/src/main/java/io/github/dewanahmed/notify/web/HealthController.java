package io.github.dewanahmed.notify.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Void> live() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health/ready")
    public ResponseEntity<Void> ready() {
        return ResponseEntity.ok().build();
    }
}
