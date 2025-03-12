package sample.bpm.be.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sample.bpm.be.entity.RefreshToken;
import sample.bpm.be.entity.User;
import sample.bpm.be.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpiration;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.refresh-expiration}") long refreshTokenExpiration) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // ✅ Create & Store a Refresh Token in Database
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())  // ✅ Unique Refresh Token
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // ✅ Validate Refresh Token (Check Expiry)
    public boolean validateRefreshToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        return refreshToken.isPresent() && refreshToken.get().getExpiryDate().isAfter(Instant.now());
    }

    // ✅ Find Refresh Token in DB
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // ✅ Delete Refresh Token (Logout)
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
