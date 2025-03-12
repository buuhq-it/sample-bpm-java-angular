package sample.bpm.be.security;

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

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.refresh-secret}") String refreshSecret,
            @Value("${app.jwt.expiration}") long accessTokenExpiration) {

        this.accessTokenKey = generateKey(secret);
        this.refreshTokenKey = generateKey(refreshSecret);
        this.accessTokenExpiration = accessTokenExpiration;
    }

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

    // ✅ Validate Token
    public boolean validateToken(String token, boolean isRefreshToken) {
        try {
            SecretKey key = isRefreshToken ? refreshTokenKey : accessTokenKey;
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(accessTokenKey)  // ✅ Use correct signing key
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException e) {
            return null; // ✅ Return null if token is invalid
        }
    }

}
