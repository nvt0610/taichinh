package com.taichinh.app.controller;

import com.taichinh.app.dto.common.ApiResponse;
import com.taichinh.app.dto.common.PaginationResponse;
import com.taichinh.app.dto.transaction.CreateExpenseTransactionRequest;
import com.taichinh.app.dto.transaction.CreateIncomeTransactionRequest;
import com.taichinh.app.dto.transaction.CreateTransferTransactionRequest;
import com.taichinh.app.dto.transaction.TransactionListQueryParams;
import com.taichinh.app.dto.transaction.TransactionResponse;
import com.taichinh.app.dto.transaction.TransferTransactionResponse;
import com.taichinh.app.security.AuthenticatedUserProvider;
import com.taichinh.app.service.TransactionService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public TransactionController(
            TransactionService transactionService,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.transactionService = transactionService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping("/income")
    public ResponseEntity<ApiResponse<TransactionResponse>> createIncome(
            Authentication authentication,
            @Valid @RequestBody CreateIncomeTransactionRequest request) {
        TransactionResponse response = transactionService.createIncome(
                authenticatedUserProvider.getUserId(authentication),
                request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Income transaction created successfully.", response));
    }

    @PostMapping("/expense")
    public ResponseEntity<ApiResponse<TransactionResponse>> createExpense(
            Authentication authentication,
            @Valid @RequestBody CreateExpenseTransactionRequest request) {
        TransactionResponse response = transactionService.createExpense(
                authenticatedUserProvider.getUserId(authentication),
                request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense transaction created successfully.", response));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferTransactionResponse>> createTransfer(
            Authentication authentication,
            @Valid @RequestBody CreateTransferTransactionRequest request) {
        TransferTransactionResponse response = transactionService.createTransfer(
                authenticatedUserProvider.getUserId(authentication),
                request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer transaction created successfully.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> list(
            Authentication authentication,
            @Valid @ModelAttribute TransactionListQueryParams queryParams) {
        Page<TransactionResponse> page = transactionService.list(
                authenticatedUserProvider.getUserId(authentication),
                queryParams);
        return ResponseEntity.ok(ApiResponse.success(
                "Transactions retrieved successfully.",
                page.getContent(),
                PaginationResponse.from(page)));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(
            Authentication authentication,
            @PathVariable UUID transactionId) {
        TransactionResponse response = transactionService.getById(
                authenticatedUserProvider.getUserId(authentication),
                transactionId);
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved successfully.", response));
    }

    @PutMapping("/{transactionId}/income")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateIncome(
            Authentication authentication,
            @PathVariable UUID transactionId,
            @Valid @RequestBody CreateIncomeTransactionRequest request) {
        TransactionResponse response = transactionService.updateIncome(
                authenticatedUserProvider.getUserId(authentication),
                transactionId,
                request);
        return ResponseEntity.ok(ApiResponse.success("Income transaction updated successfully.", response));
    }

    @PutMapping("/{transactionId}/expense")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateExpense(
            Authentication authentication,
            @PathVariable UUID transactionId,
            @Valid @RequestBody CreateExpenseTransactionRequest request) {
        TransactionResponse response = transactionService.updateExpense(
                authenticatedUserProvider.getUserId(authentication),
                transactionId,
                request);
        return ResponseEntity.ok(ApiResponse.success("Expense transaction updated successfully.", response));
    }

    @PutMapping("/{transactionId}/transfer")
    public ResponseEntity<ApiResponse<TransferTransactionResponse>> updateTransfer(
            Authentication authentication,
            @PathVariable UUID transactionId,
            @Valid @RequestBody CreateTransferTransactionRequest request) {
        TransferTransactionResponse response = transactionService.updateTransfer(
                authenticatedUserProvider.getUserId(authentication),
                transactionId,
                request);
        return ResponseEntity.ok(ApiResponse.success("Transfer transaction updated successfully.", response));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            Authentication authentication,
            @PathVariable UUID transactionId) {
        transactionService.softDelete(authenticatedUserProvider.getUserId(authentication), transactionId);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully.", null));
    }
}
