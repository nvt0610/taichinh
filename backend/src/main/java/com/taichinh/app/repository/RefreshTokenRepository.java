package com.taichinh.app.repository;

import com.taichinh.app.entity.RefreshToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserIdAndRevokedFalseAndExpiredAtAfter(UUID userId, LocalDateTime now);

    void deleteByUserId(UUID userId);
}
