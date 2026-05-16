package com.taichinh.app.service;

import com.taichinh.app.dto.common.ListQueryParams;
import com.taichinh.app.dto.wallet.CreateWalletRequest;
import com.taichinh.app.dto.wallet.UpdateWalletRequest;
import com.taichinh.app.dto.wallet.WalletResponse;
import com.taichinh.app.entity.Wallet;
import com.taichinh.app.exception.BusinessException;
import com.taichinh.app.exception.ErrorCode;
import com.taichinh.app.repository.WalletRepository;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "type", "balance", "createdAt", "updatedAt");

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public WalletResponse create(UUID userId, CreateWalletRequest request) {
        Wallet wallet = new Wallet(userId, request.name().trim(), request.type());
        wallet.setBalance(request.balance());
        wallet.setDescription(normalizeNullableText(request.description()));
        return toResponse(walletRepository.save(wallet));
    }

    @Transactional(readOnly = true)
    public Page<WalletResponse> list(UUID userId, ListQueryParams queryParams) {
        String query = normalizeSearchQuery(queryParams.getQ());
        var pageable = queryParams.toPageable(ALLOWED_SORT_FIELDS, "createdAt", Sort.Direction.DESC);
        Page<Wallet> page = query == null
                ? walletRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
                : walletRepository.findByUserIdAndDeletedAtIsNullAndNameContainingIgnoreCase(userId, query, pageable);
        return page
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public WalletResponse getById(UUID userId, UUID walletId) {
        return toResponse(findActiveWallet(userId, walletId));
    }

    @Transactional
    public WalletResponse update(UUID userId, UUID walletId, UpdateWalletRequest request) {
        Wallet wallet = findActiveWallet(userId, walletId);
        wallet.setName(request.name().trim());
        wallet.setType(request.type());
        wallet.setDescription(normalizeNullableText(request.description()));
        return toResponse(walletRepository.save(wallet));
    }

    @Transactional
    public void softDelete(UUID userId, UUID walletId) {
        Wallet wallet = findActiveWallet(userId, walletId);
        wallet.setDeletedAt(LocalDateTime.now());
        walletRepository.save(wallet);
    }

    private Wallet findActiveWallet(UUID userId, UUID walletId) {
        return walletRepository.findByIdAndUserIdAndDeletedAtIsNull(walletId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Wallet not found."));
    }

    private WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getName(),
                wallet.getType(),
                wallet.getBalance(),
                wallet.getDescription(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt());
    }

    private String normalizeSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.trim();
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
