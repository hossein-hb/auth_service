package com.traazu.auth_service.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private String secretKey;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secretKey, 
            @Value("${access.token.expiration}") long accessTokenExpiration,
            @Value("${refresh.token.expiration}") long refreshTokenExpiration) {
        
        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims allClaims = extractAllClaims(token);
        return claimsResolver.apply(allClaims);
    }

    public <T> T extractClaim(Claims allClaims, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(allClaims);
    }

    public UUID extractId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.getSubject());
        return userIdStr == null ? null : UUID.fromString(userIdStr);
    }

    public String generateAccessToken(CustomUserDetails customUserDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", customUserDetails.getRole().name());
        extraClaims.put("userId", customUserDetails.getId().toString());
        extraClaims.put("email", customUserDetails.getUsername());
        return buildToken(customUserDetails.getId().toString(), extraClaims, accessTokenExpiration);
    }

    public String extractJwtId(String token) {
        return extractClaim(token, claims -> claims.getId());
    }

    public String buildToken(String subject, Map<String, Object> extraClaims, long expiration) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                    .claims(extraClaims)
                    .subject(subject)
                    .id(jti)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(getSignInKey())
                    .compact();
    }

    public String generateRefreshToken(UUID userId) {
        return buildToken(userId.toString(), null, refreshTokenExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, String username) {
        try {
            Claims allClaims = extractAllClaims(token);
            return allClaims.getSubject().equals(username);
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
    }

    public TokenInfos getTokenInfos(String token) {
        Claims allClaims = extractAllClaims(token);
        String userIdStr = extractClaim(allClaims, claims -> claims.getSubject());
        UUID userId = userIdStr == null ? null : UUID.fromString(userIdStr);
        String username = extractClaim(allClaims, claims -> claims.get("email", String.class));
        String roles = extractClaim(allClaims, claims -> claims.get("role", String.class));
        return new TokenInfos(userId, username, roles);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
}
