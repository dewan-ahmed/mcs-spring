package io.github.dewanahmed.notify.publicapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyAuthFilter apiKeyAuthFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health", "/health/**", "/actuator/health", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(handling -> handling.authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                    response.getWriter().write("{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"A valid X-Api-Key header is required\"}");
                }));
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(username);
        };
    }

    @Bean
    ApiKeyAuthFilter apiKeyAuthFilter(PublicSecurityProperties properties) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("notify.security.api-key must be set");
        }
        return new ApiKeyAuthFilter(apiKey);
    }

    @Bean
    FilterRegistrationBean<ApiKeyAuthFilter> apiKeyAuthFilterRegistration(ApiKeyAuthFilter apiKeyAuthFilter) {
        FilterRegistrationBean<ApiKeyAuthFilter> registration = new FilterRegistrationBean<>(apiKeyAuthFilter);
        registration.setEnabled(false);
        return registration;
    }

    static final class ApiKeyAuthFilter extends OncePerRequestFilter {

        static final String HEADER = "X-Api-Key";

        private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

        private final byte[] configuredKey;

        ApiKeyAuthFilter(String configuredKey) {
            this.configuredKey = configuredKey.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String provided = request.getHeader(HEADER);
            if (provided != null && MessageDigest.isEqual(configuredKey, provided.getBytes(StandardCharsets.UTF_8))) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        "api-client",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else if (provided != null) {
                log.warn("api key rejected method={} path={}", request.getMethod(), request.getRequestURI());
            }
            filterChain.doFilter(request, response);
        }
    }
}
