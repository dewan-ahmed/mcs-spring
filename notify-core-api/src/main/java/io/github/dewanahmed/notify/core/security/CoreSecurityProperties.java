package io.github.dewanahmed.notify.core.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "notify.security")
public class CoreSecurityProperties {

    private Jwt jwt = new Jwt();
    private List<UserAccount> users = new ArrayList<>();

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public List<UserAccount> getUsers() {
        return users;
    }

    public void setUsers(List<UserAccount> users) {
        this.users = users;
    }

    public static class Jwt {
        private String issuer = "notify-core-api";
        private String secret = "";
        private Duration ttl = Duration.ofHours(1);

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }

    public static class UserAccount {
        private String username = "";
        private String password = "";
        private List<String> roles = new ArrayList<>(List.of("USER"));

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
