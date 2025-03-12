package sample.bpm.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sample.bpm.be.entity.RefreshToken;
import sample.bpm.be.entity.User;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
