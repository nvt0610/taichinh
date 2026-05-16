package com.taichinh.app.repository;

import com.taichinh.app.entity.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByUserIdAndDeletedAtIsNull(UUID userId, Sort sort);

    List<Transaction> findByUserIdAndTransactionDateBetweenAndDeletedAtIsNull(
            UUID userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Sort sort);

    List<Transaction> findByWalletIdAndDeletedAtIsNull(UUID walletId, Sort sort);

    Optional<Transaction> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Optional<Transaction> findByReferenceTransactionIdAndUserIdAndDeletedAtIsNull(UUID referenceTransactionId, UUID userId);
}
