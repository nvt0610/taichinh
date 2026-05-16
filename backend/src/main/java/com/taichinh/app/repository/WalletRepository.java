package com.taichinh.app.repository;

import com.taichinh.app.entity.Wallet;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    List<Wallet> findByUserIdAndDeletedAtIsNull(UUID userId, Sort sort);

    Page<Wallet> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Page<Wallet> findByUserIdAndDeletedAtIsNullAndNameContainingIgnoreCase(UUID userId, String name, Pageable pageable);

    Optional<Wallet> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    boolean existsByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}
