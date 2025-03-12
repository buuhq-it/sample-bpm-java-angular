package sample.bpm.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sample.bpm.be.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
