package sample.bpm.be.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.refresh-secret}") String refreshSecret,
            @Value("${app.jwt.expiration}") long accessTokenExpiration,
            @Value("${app.jwt.refresh-expiration}") long refreshTokenExpiration) {

        this.accessTokenKey = generateKey(secret);
        this.refreshTokenKey = generateKey(refreshSecret);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // ✅ Securely Generate Secret Keys
    private SecretKey generateKey(String secret) {
        byte[] keyBytes = Base64.getEncoder().encode(secret.getBytes(StandardCharsets.UTF_8));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ✅ Generate Access Token
    public String generateAccessToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(accessTokenKey, Jwts.SIG.HS256)
                .compact();
    }

    // ✅ Generate Refresh Token
    public String generateRefreshToken() {
        return Jwts.builder()
                .subject("refresh-token")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(refreshTokenKey, Jwts.SIG.HS256)
                .compact();
    }

    // ✅ Extract Username from Access Token
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(accessTokenKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // ✅ Validate Token
    public boolean validateToken(String token, boolean isRefreshToken) {
        try {
            SecretKey key = isRefreshToken ? refreshTokenKey : accessTokenKey;
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("JWT Token has expired");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Invalid JWT Token: " + e.getMessage());
            return false;
        }
    }
}
