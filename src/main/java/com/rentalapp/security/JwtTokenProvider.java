package com.rentalapp.security;

import com.rentalapp.exception.AuthenticationException;
import com.rentalapp.module.auth.entity.Role;
import com.rentalapp.module.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        return generateToken(user, jwtProperties.accessTokenExpirationMs(), "ACCESS", UUID.randomUUID().toString());
    }

    public String generateRefreshToken(User user, String tokenId) {
        return generateToken(user, jwtProperties.refreshTokenExpirationMs(), "REFRESH", tokenId);
    }

    public AuthUserPrincipal parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return AuthUserPrincipal.builder()
                    .id(claims.getSubject())
                    .email(claims.get("email", String.class))
                    .role(Role.valueOf(claims.get("role", String.class)))
                    .tokenId(claims.getId())
                    .tokenType(claims.get("tokenType", String.class))
                    .build();
        } catch (Exception exception) {
            throw AuthenticationException.invalidToken();
        }
    }

    private String generateToken(User user, long expiryMs, String tokenType, String tokenId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getId())
                .id(tokenId)
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("tokenType", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiryMs)))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
    }
}
