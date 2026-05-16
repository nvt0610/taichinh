package com.taichinh.app.controller;

import com.taichinh.app.dto.common.ApiResponse;
import com.taichinh.app.dto.common.ListQueryParams;
import com.taichinh.app.dto.common.PaginationResponse;
import com.taichinh.app.dto.wallet.CreateWalletRequest;
import com.taichinh.app.dto.wallet.UpdateWalletRequest;
import com.taichinh.app.dto.wallet.WalletResponse;
import com.taichinh.app.security.AuthenticatedUserProvider;
import com.taichinh.app.service.WalletService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public WalletController(WalletService walletService, AuthenticatedUserProvider authenticatedUserProvider) {
        this.walletService = walletService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> create(
            Authentication authentication,
            @Valid @RequestBody CreateWalletRequest request) {
        WalletResponse response = walletService.create(authenticatedUserProvider.getUserId(authentication), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Wallet created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletResponse>>> list(
            Authentication authentication,
            @Valid @ModelAttribute ListQueryParams queryParams) {
        Page<WalletResponse> page = walletService.list(authenticatedUserProvider.getUserId(authentication), queryParams);
        return ResponseEntity.ok(ApiResponse.success(
                "Wallets retrieved successfully.",
                page.getContent(),
                PaginationResponse.from(page)));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getById(
            Authentication authentication,
            @PathVariable UUID walletId) {
        WalletResponse response = walletService.getById(authenticatedUserProvider.getUserId(authentication), walletId);
        return ResponseEntity.ok(ApiResponse.success("Wallet retrieved successfully.", response));
    }

    @PutMapping("/{walletId}")
    public ResponseEntity<ApiResponse<WalletResponse>> update(
            Authentication authentication,
            @PathVariable UUID walletId,
            @Valid @RequestBody UpdateWalletRequest request) {
        WalletResponse response = walletService.update(
                authenticatedUserProvider.getUserId(authentication),
                walletId,
                request);
        return ResponseEntity.ok(ApiResponse.success("Wallet updated successfully.", response));
    }

    @DeleteMapping("/{walletId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication authentication,
            @PathVariable UUID walletId) {
        walletService.softDelete(authenticatedUserProvider.getUserId(authentication), walletId);
        return ResponseEntity.ok(ApiResponse.success("Wallet deleted successfully.", null));
    }
}
