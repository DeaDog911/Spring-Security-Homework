package org.deadog.springsecurityhomework.services;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deadog.springsecurityhomework.model.RefreshToken;
import org.deadog.springsecurityhomework.model.RoleType;
import org.deadog.springsecurityhomework.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.security.Key;
import java.sql.Ref;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TokenService {
    private static final String ROLE_CLAIM = "role";

    private static final String ID_CLAIM = "id";

    private final RefreshTokenRepository refreshTokenRepository;

    private SecretKey jwtAccessKey;

    private SecretKey jwtRefreshKey;

    private Duration tokenAccessExpiration;

    private Duration tokenRefreshExpiration;

    public TokenService(
            @Value("${jwt.secret.access}") String jwtAccessSecret,
            @Value("${jwt.secret.refresh}") String jwtRefreshSecret,
            @Value("${jwt.expiration.access}") String  tokenAccessExpiration,
            @Value("${jwt.expiration.refresh}") String tokenRefreshExpiration,
            RefreshTokenRepository refreshTokenRepository) {
        this.jwtAccessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.jwtRefreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
        this.tokenAccessExpiration = Duration.parse(tokenAccessExpiration);
        this.tokenRefreshExpiration = Duration.parse(tokenRefreshExpiration);
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessToken(String username, String id, Set<RoleType> roles) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + tokenAccessExpiration.toMillis()))
                .claim(ROLE_CLAIM, roles)
                .claim(ID_CLAIM, id)
                .signWith(jwtAccessKey)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + tokenRefreshExpiration.toMillis()))
                .signWith(jwtRefreshKey)
                .compact();
    }

    public boolean validateAccessToken(String accessToken) {
        return validateToken(accessToken, jwtAccessKey);
    }

    public boolean validateRefreshToken(String refreshToken) {
        return validateToken(refreshToken, jwtRefreshKey);
    }

    public Mono<Authentication> toAuthentication(String accessToken) {
        if (validateAccessToken(accessToken)) {
            Claims claims = getAccessClaims(accessToken);
            String username = claims.getSubject();
            List<String> roles = claims.get(ROLE_CLAIM, List.class);

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList());

            return Mono.just(auth);
        }
        return Mono.empty();
    }

    private Boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getAccessClaims(String token) {
        return getClaims(token, jwtAccessKey);
    }

    public Claims getRefreshClaims(String token) {
        return getClaims(token, jwtRefreshKey);
    }

    private Claims getClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token).getPayload();
    }

}
