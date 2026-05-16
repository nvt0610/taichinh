package com.taichinh.app.service;

import com.taichinh.app.dto.transaction.CreateExpenseTransactionRequest;
import com.taichinh.app.dto.transaction.CreateIncomeTransactionRequest;
import com.taichinh.app.dto.transaction.CreateTransferTransactionRequest;
import com.taichinh.app.dto.transaction.TransactionResponse;
import com.taichinh.app.dto.transaction.TransactionListQueryParams;
import com.taichinh.app.dto.transaction.TransferTransactionResponse;
import com.taichinh.app.entity.Category;
import com.taichinh.app.entity.Transaction;
import com.taichinh.app.entity.Wallet;
import com.taichinh.app.enums.CategoryType;
import com.taichinh.app.enums.TransactionType;
import com.taichinh.app.exception.BusinessException;
import com.taichinh.app.exception.ErrorCode;
import com.taichinh.app.repository.CategoryRepository;
import com.taichinh.app.repository.TransactionRepository;
import com.taichinh.app.repository.WalletRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("transactionDate", "amount", "title", "type", "createdAt", "updatedAt");

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            WalletRepository walletRepository,
            CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public TransactionResponse createIncome(UUID userId, CreateIncomeTransactionRequest request) {
        Wallet wallet = findActiveWallet(userId, request.walletId());
        Category category = findOptionalCategory(userId, request.categoryId(), CategoryType.INCOME);

        wallet.setBalance(wallet.getBalance().add(request.amount()));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction(
                userId,
                wallet.getId(),
                TransactionType.INCOME,
                request.amount(),
                request.title().trim(),
                request.transactionDate());
        transaction.setCategoryId(category == null ? null : category.getId());
        transaction.setNote(normalizeNullableText(request.note()));

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse createExpense(UUID userId, CreateExpenseTransactionRequest request) {
        Wallet wallet = findActiveWallet(userId, request.walletId());
        Category category = findOptionalCategory(userId, request.categoryId(), CategoryType.EXPENSE);
        validateSufficientBalance(wallet, request.amount());

        wallet.setBalance(wallet.getBalance().subtract(request.amount()));
        walletRepository.save(wallet);

        Transaction transaction = new Transaction(
                userId,
                wallet.getId(),
                TransactionType.EXPENSE,
                request.amount(),
                request.title().trim(),
                request.transactionDate());
        transaction.setCategoryId(category == null ? null : category.getId());
        transaction.setNote(normalizeNullableText(request.note()));

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransferTransactionResponse createTransfer(UUID userId, CreateTransferTransactionRequest request) {
        if (request.sourceWalletId().equals(request.destinationWalletId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Source and destination wallets must be different.");
        }

        Wallet sourceWallet = findActiveWallet(userId, request.sourceWalletId());
        Wallet destinationWallet = findActiveWallet(userId, request.destinationWalletId());
        validateSufficientBalance(sourceWallet, request.amount());

        sourceWallet.setBalance(sourceWallet.getBalance().subtract(request.amount()));
        destinationWallet.setBalance(destinationWallet.getBalance().add(request.amount()));
        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);

        Transaction sourceTransaction = new Transaction(
                userId,
                sourceWallet.getId(),
                TransactionType.TRANSFER,
                request.amount().negate(),
                request.title().trim(),
                request.transactionDate());
        sourceTransaction.setNote(normalizeNullableText(request.note()));

        Transaction destinationTransaction = new Transaction(
                userId,
                destinationWallet.getId(),
                TransactionType.TRANSFER,
                request.amount(),
                request.title().trim(),
                request.transactionDate());
        destinationTransaction.setNote(normalizeNullableText(request.note()));

        Transaction savedSource = transactionRepository.save(sourceTransaction);
        Transaction savedDestination = transactionRepository.save(destinationTransaction);

        savedSource.setReferenceTransactionId(savedDestination.getId());
        savedDestination.setReferenceTransactionId(savedSource.getId());

        savedSource = transactionRepository.save(savedSource);
        savedDestination = transactionRepository.save(savedDestination);

        return new TransferTransactionResponse(
                toResponse(savedSource),
                toResponse(savedDestination));
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> list(UUID userId, TransactionListQueryParams queryParams) {
        Pageable pageable = queryParams.toPageable(ALLOWED_SORT_FIELDS, "transactionDate", Sort.Direction.DESC);

        if (queryParams.getStartDate() != null
                && queryParams.getEndDate() != null
                && queryParams.getStartDate().isAfter(queryParams.getEndDate())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Start date must be before or equal to end date.");
        }

        List<Transaction> transactions = new ArrayList<>(
                transactionRepository.findByUserIdAndDeletedAtIsNull(userId, Sort.by(Sort.Direction.DESC, "transactionDate")));

        List<Transaction> filtered = transactions.stream()
                .filter(transaction -> queryParams.getWalletId() == null || queryParams.getWalletId().equals(transaction.getWalletId()))
                .filter(transaction -> queryParams.getType() == null || queryParams.getType() == transaction.getType())
                .filter(transaction -> queryParams.getStartDate() == null || !transaction.getTransactionDate().isBefore(queryParams.getStartDate()))
                .filter(transaction -> queryParams.getEndDate() == null || !transaction.getTransactionDate().isAfter(queryParams.getEndDate()))
                .filter(transaction -> matchesQuery(transaction, queryParams.getQ()))
                .sorted(buildComparator(pageable.getSort()))
                .toList();

        int total = filtered.size();
        int startIndex = Math.min((int) pageable.getOffset(), total);
        int endIndex = Math.min(startIndex + pageable.getPageSize(), total);

        List<TransactionResponse> content = filtered.subList(startIndex, endIndex).stream()
                .map(this::toResponse)
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(UUID userId, UUID transactionId) {
        return toResponse(findActiveTransaction(userId, transactionId));
    }

    @Transactional
    public TransactionResponse updateIncome(UUID userId, UUID transactionId, CreateIncomeTransactionRequest request) {
        Transaction transaction = findActiveTransaction(userId, transactionId);
        validateTransactionType(transaction, TransactionType.INCOME);

        Wallet previousWallet = findActiveWallet(userId, transaction.getWalletId());
        validateSufficientBalance(previousWallet, transaction.getAmount());
        previousWallet.setBalance(previousWallet.getBalance().subtract(transaction.getAmount()));
        walletRepository.save(previousWallet);

        Wallet targetWallet = findActiveWallet(userId, request.walletId());
        Category category = findOptionalCategory(userId, request.categoryId(), CategoryType.INCOME);
        targetWallet.setBalance(targetWallet.getBalance().add(request.amount()));
        walletRepository.save(targetWallet);

        transaction.setWalletId(targetWallet.getId());
        transaction.setCategoryId(category == null ? null : category.getId());
        transaction.setAmount(request.amount());
        transaction.setTitle(request.title().trim());
        transaction.setNote(normalizeNullableText(request.note()));
        transaction.setTransactionDate(request.transactionDate());

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse updateExpense(UUID userId, UUID transactionId, CreateExpenseTransactionRequest request) {
        Transaction transaction = findActiveTransaction(userId, transactionId);
        validateTransactionType(transaction, TransactionType.EXPENSE);

        Wallet previousWallet = findActiveWallet(userId, transaction.getWalletId());
        previousWallet.setBalance(previousWallet.getBalance().add(transaction.getAmount()));
        walletRepository.save(previousWallet);

        Wallet targetWallet = findActiveWallet(userId, request.walletId());
        Category category = findOptionalCategory(userId, request.categoryId(), CategoryType.EXPENSE);
        validateSufficientBalance(targetWallet, request.amount());
        targetWallet.setBalance(targetWallet.getBalance().subtract(request.amount()));
        walletRepository.save(targetWallet);

        transaction.setWalletId(targetWallet.getId());
        transaction.setCategoryId(category == null ? null : category.getId());
        transaction.setAmount(request.amount());
        transaction.setTitle(request.title().trim());
        transaction.setNote(normalizeNullableText(request.note()));
        transaction.setTransactionDate(request.transactionDate());

        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransferTransactionResponse updateTransfer(UUID userId, UUID transactionId, CreateTransferTransactionRequest request) {
        if (request.sourceWalletId().equals(request.destinationWalletId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Source and destination wallets must be different.");
        }

        Transaction transaction = findActiveTransaction(userId, transactionId);
        validateTransactionType(transaction, TransactionType.TRANSFER);
        Transaction counterpart = findTransferCounterpart(userId, transaction);
        TransferPair transferPair = resolveTransferPair(transaction, counterpart);

        rollbackTransferPair(userId, transferPair);

        Wallet sourceWallet = findActiveWallet(userId, request.sourceWalletId());
        Wallet destinationWallet = findActiveWallet(userId, request.destinationWalletId());
        validateSufficientBalance(sourceWallet, request.amount());
        applyTransfer(sourceWallet, destinationWallet, request.amount());

        transferPair.sourceTransaction.setWalletId(sourceWallet.getId());
        transferPair.sourceTransaction.setAmount(request.amount().negate());
        transferPair.sourceTransaction.setTitle(request.title().trim());
        transferPair.sourceTransaction.setNote(normalizeNullableText(request.note()));
        transferPair.sourceTransaction.setTransactionDate(request.transactionDate());

        transferPair.destinationTransaction.setWalletId(destinationWallet.getId());
        transferPair.destinationTransaction.setAmount(request.amount());
        transferPair.destinationTransaction.setTitle(request.title().trim());
        transferPair.destinationTransaction.setNote(normalizeNullableText(request.note()));
        transferPair.destinationTransaction.setTransactionDate(request.transactionDate());

        Transaction savedSource = transactionRepository.save(transferPair.sourceTransaction);
        Transaction savedDestination = transactionRepository.save(transferPair.destinationTransaction);

        return new TransferTransactionResponse(toResponse(savedSource), toResponse(savedDestination));
    }

    @Transactional
    public void softDelete(UUID userId, UUID transactionId) {
        Transaction transaction = findActiveTransaction(userId, transactionId);

        if (transaction.getType() == TransactionType.TRANSFER) {
            Transaction counterpart = findTransferCounterpart(userId, transaction);
            TransferPair transferPair = resolveTransferPair(transaction, counterpart);

            rollbackTransferPair(userId, transferPair);
            LocalDateTime deletedAt = LocalDateTime.now();
            transferPair.sourceTransaction.setDeletedAt(deletedAt);
            transferPair.destinationTransaction.setDeletedAt(deletedAt);
            transactionRepository.save(transferPair.sourceTransaction);
            transactionRepository.save(transferPair.destinationTransaction);
            return;
        }

        rollbackSingleTransaction(userId, transaction);
        transaction.setDeletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    private Wallet findActiveWallet(UUID userId, UUID walletId) {
        return walletRepository.findByIdAndUserIdAndDeletedAtIsNull(walletId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Wallet not found."));
    }

    private Transaction findActiveTransaction(UUID userId, UUID transactionId) {
        return transactionRepository.findByIdAndUserIdAndDeletedAtIsNull(transactionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Transaction not found."));
    }

    private Transaction findTransferCounterpart(UUID userId, Transaction transaction) {
        if (transaction.getReferenceTransactionId() == null) {
            throw new BusinessException(ErrorCode.CONFLICT, "Transfer transaction pair is incomplete.");
        }

        return transactionRepository.findByIdAndUserIdAndDeletedAtIsNull(transaction.getReferenceTransactionId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Transfer counterpart transaction not found."));
    }

    private Category findOptionalCategory(UUID userId, UUID categoryId, CategoryType expectedType) {
        if (categoryId == null) {
            return null;
        }

        Category category = categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Category not found."));

        if (category.getType() != expectedType) {
            throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    "Category type does not match transaction type.");
        }

        return category;
    }

    private void validateSufficientBalance(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE, "Wallet balance is not enough for this transaction.");
        }
    }

    private void validateTransactionType(Transaction transaction, TransactionType expectedType) {
        if (transaction.getType() != expectedType) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Transaction type does not match the requested operation.");
        }
    }

    private void rollbackSingleTransaction(UUID userId, Transaction transaction) {
        Wallet wallet = findActiveWallet(userId, transaction.getWalletId());

        if (transaction.getType() == TransactionType.INCOME) {
            validateSufficientBalance(wallet, transaction.getAmount());
            wallet.setBalance(wallet.getBalance().subtract(transaction.getAmount()));
        } else if (transaction.getType() == TransactionType.EXPENSE) {
            wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Transfer transaction must be rolled back as a pair.");
        }

        walletRepository.save(wallet);
    }

    private void rollbackTransferPair(UUID userId, TransferPair transferPair) {
        Wallet sourceWallet = findActiveWallet(userId, transferPair.sourceTransaction.getWalletId());
        Wallet destinationWallet = findActiveWallet(userId, transferPair.destinationTransaction.getWalletId());

        validateSufficientBalance(destinationWallet, transferPair.destinationTransaction.getAmount().abs());
        sourceWallet.setBalance(sourceWallet.getBalance().add(transferPair.sourceTransaction.getAmount().abs()));
        destinationWallet.setBalance(destinationWallet.getBalance().subtract(transferPair.destinationTransaction.getAmount().abs()));
        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);
    }

    private void applyTransfer(Wallet sourceWallet, Wallet destinationWallet, BigDecimal amount) {
        sourceWallet.setBalance(sourceWallet.getBalance().subtract(amount));
        destinationWallet.setBalance(destinationWallet.getBalance().add(amount));
        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);
    }

    private TransferPair resolveTransferPair(Transaction first, Transaction second) {
        if (first.getAmount().compareTo(BigDecimal.ZERO) < 0 && second.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            return new TransferPair(first, second);
        }
        if (second.getAmount().compareTo(BigDecimal.ZERO) < 0 && first.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            return new TransferPair(second, first);
        }
        return new TransferPair(first, second);
    }

    private boolean matchesQuery(Transaction transaction, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String normalizedQuery = query.trim().toLowerCase();
        return transaction.getTitle().toLowerCase().contains(normalizedQuery)
                || (transaction.getNote() != null && transaction.getNote().toLowerCase().contains(normalizedQuery));
    }

    private Comparator<Transaction> buildComparator(Sort sort) {
        Comparator<Transaction> comparator = null;

        for (Sort.Order order : sort) {
            Comparator<Transaction> current = switch (order.getProperty()) {
                case "amount" -> Comparator.comparing(Transaction::getAmount);
                case "title" -> Comparator.comparing(Transaction::getTitle, String.CASE_INSENSITIVE_ORDER);
                case "type" -> Comparator.comparing(transaction -> transaction.getType().name());
                case "createdAt" -> Comparator.comparing(Transaction::getCreatedAt);
                case "updatedAt" -> Comparator.comparing(Transaction::getUpdatedAt);
                case "transactionDate" -> Comparator.comparing(Transaction::getTransactionDate);
                default -> throw new BusinessException(ErrorCode.INVALID_SORT, "Sort field is not supported: " + order.getProperty());
            };

            if (order.isDescending()) {
                current = current.reversed();
            }

            comparator = comparator == null ? current : comparator.thenComparing(current);
        }

        return comparator == null ? Comparator.comparing(Transaction::getTransactionDate).reversed() : comparator;
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getWalletId(),
                transaction.getCategoryId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getTitle(),
                transaction.getNote(),
                transaction.getTransactionDate(),
                transaction.getReferenceTransactionId(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt());
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record TransferPair(Transaction sourceTransaction, Transaction destinationTransaction) {
    }
}
