package io.github.dewanahmed.notify.core.web;

import io.github.dewanahmed.notify.core.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            UserDetails user = userDetailsService.loadUserByUsername(request.username());
            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                throw new BadCredentialsException("Invalid credentials");
            }
            log.info("login succeeded username={}", user.getUsername());
            return ResponseEntity.ok(jwtService.issue(user));
        } catch (UsernameNotFoundException | BadCredentialsException exception) {
            log.warn("login failed username={}", request.username());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
            problem.setTitle("Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
        }
    }

    public record LoginRequest(
            @NotBlank @Size(max = 128) String username,
            @NotBlank @Size(max = 256) String password) {
    }
}
