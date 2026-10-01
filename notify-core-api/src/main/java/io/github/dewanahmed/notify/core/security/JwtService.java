package io.github.dewanahmed.notify.core.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final CoreSecurityProperties properties;

    public JwtService(JwtEncoder jwtEncoder, CoreSecurityProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public TokenResponse issue(UserDetails user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.getJwt().getTtl());
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring("ROLE_".length()) : authority)
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.getJwt().getIssuer())
                .subject(user.getUsername())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("roles", roles)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, "Bearer", properties.getJwt().getTtl().toSeconds());
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
    }
}
