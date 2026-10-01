package io.github.dewanahmed.notify.core.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health", "/health/**", "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .exceptionHandling(handling -> handling.authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    response.getWriter().write("{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Authentication is required\"}");
                }));
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(CoreSecurityProperties properties, PasswordEncoder passwordEncoder) {
        if (properties.getUsers() == null || properties.getUsers().isEmpty()) {
            throw new IllegalStateException("notify.security.users must define at least one user");
        }
        var manager = new InMemoryUserDetailsManager();
        for (CoreSecurityProperties.UserAccount account : properties.getUsers()) {
            if (account.getUsername() == null || account.getUsername().isBlank()) {
                throw new IllegalStateException("notify.security.users entries require a username");
            }
            if (account.getPassword() == null || account.getPassword().isBlank()) {
                throw new IllegalStateException("password must be set for user " + account.getUsername());
            }
            String[] roles = account.getRoles() == null || account.getRoles().isEmpty()
                    ? new String[]{"USER"}
                    : account.getRoles().toArray(String[]::new);
            manager.createUser(User.builder()
                    .username(account.getUsername())
                    .password(passwordEncoder.encode(account.getPassword()))
                    .roles(roles)
                    .build());
        }
        return manager;
    }

    @Bean
    JwtEncoder jwtEncoder(CoreSecurityProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(properties)));
    }

    @Bean
    JwtDecoder jwtDecoder(CoreSecurityProperties properties) {
        return NimbusJwtDecoder.withSecretKey(secretKey(properties)).build();
    }

    static SecretKey secretKey(CoreSecurityProperties properties) {
        String secret = properties.getJwt().getSecret();
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("notify.security.jwt.secret must be at least 32 bytes");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
