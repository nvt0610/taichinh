package com.taichinh.app.service;

import com.taichinh.app.dto.dashboard.DashboardSummaryQueryParams;
import com.taichinh.app.dto.dashboard.DashboardSummaryResponse;
import com.taichinh.app.dto.dashboard.DashboardRecentTransactionResponse;
import com.taichinh.app.dto.dashboard.DashboardRecentTransactionsQueryParams;
import com.taichinh.app.dto.dashboard.DashboardTopSpendingCategoryResponse;
import com.taichinh.app.dto.dashboard.DashboardMonthlyStatisticResponse;
import com.taichinh.app.dto.dashboard.DashboardMonthlyStatisticsQueryParams;
import com.taichinh.app.entity.Category;
import com.taichinh.app.entity.Transaction;
import com.taichinh.app.entity.Wallet;
import com.taichinh.app.enums.TransactionType;
import com.taichinh.app.enums.CategoryType;
import com.taichinh.app.exception.BusinessException;
import com.taichinh.app.exception.ErrorCode;
import com.taichinh.app.repository.CategoryRepository;
import com.taichinh.app.repository.TransactionRepository;
import com.taichinh.app.repository.WalletRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public DashboardService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(UUID userId, DashboardSummaryQueryParams queryParams) {
        DateRange dateRange = resolveDateRange(queryParams);

        List<Wallet> wallets = walletRepository.findByUserIdAndDeletedAtIsNull(userId, Sort.unsorted());
        BigDecimal totalBalance = wallets.stream()
                .map(Wallet::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetweenAndDeletedAtIsNull(
                userId,
                dateRange.start(),
                dateRange.end(),
                Sort.unsorted());

        BigDecimal totalIncome = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardSummaryResponse(
                totalBalance,
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense),
                dateRange.start(),
                dateRange.end());
    }

    @Transactional(readOnly = true)
    public List<DashboardRecentTransactionResponse> getRecentTransactions(
            UUID userId,
            DashboardRecentTransactionsQueryParams queryParams) {
        List<Transaction> transactions = transactionRepository.findByUserIdAndDeletedAtIsNull(
                userId,
                Sort.by(Sort.Direction.DESC, "transactionDate"));

        Map<UUID, Wallet> walletsById = walletRepository.findByUserIdAndDeletedAtIsNull(userId, Sort.unsorted()).stream()
                .collect(Collectors.toMap(Wallet::getId, Function.identity()));
        Map<UUID, Category> categoriesById = categoryRepository.findByUserIdAndDeletedAtIsNull(userId, Sort.unsorted()).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        return transactions.stream()
                .limit(queryParams.getLimit())
                .map(transaction -> toRecentTransactionResponse(transaction, walletsById, categoriesById))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardTopSpendingCategoryResponse> getTopSpendingCategories(
            UUID userId,
            DashboardSummaryQueryParams queryParams) {
        DateRange dateRange = resolveDateRange(queryParams);
        Map<UUID, Category> categoriesById = categoryRepository.findByUserIdAndTypeAndDeletedAtIsNull(
                        userId,
                        CategoryType.EXPENSE,
                        Sort.unsorted())
                .stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        Map<UUID, List<Transaction>> groupedExpenses = transactionRepository.findByUserIdAndTransactionDateBetweenAndDeletedAtIsNull(
                        userId,
                        dateRange.start(),
                        dateRange.end(),
                        Sort.unsorted())
                .stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .filter(transaction -> transaction.getCategoryId() != null)
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        return groupedExpenses.entrySet().stream()
                .map(entry -> toTopSpendingCategoryResponse(entry.getKey(), entry.getValue(), categoriesById))
                .filter(response -> response != null)
                .sorted(Comparator.comparing(DashboardTopSpendingCategoryResponse::totalAmount).reversed()
                        .thenComparing(DashboardTopSpendingCategoryResponse::categoryName, String.CASE_INSENSITIVE_ORDER))
                .limit(5)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DashboardMonthlyStatisticResponse> getMonthlyStatistics(
            UUID userId,
            DashboardMonthlyStatisticsQueryParams queryParams) {
        int months = queryParams.getMonths();
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(months - 1L);
        LocalDateTime startDate = firstMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = LocalDateTime.of(currentMonth.atEndOfMonth(), LocalTime.MAX);

        Map<YearMonth, MonthlyTotals> totalsByMonth = transactionRepository.findByUserIdAndTransactionDateBetweenAndDeletedAtIsNull(
                        userId,
                        startDate,
                        endDate,
                        Sort.unsorted())
                .stream()
                .collect(Collectors.groupingBy(
                        transaction -> YearMonth.from(transaction.getTransactionDate()),
                        Collectors.collectingAndThen(Collectors.toList(), this::toMonthlyTotals)));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<YearMonth, DashboardMonthlyStatisticResponse> ordered = new LinkedHashMap<>();
        for (int offset = 0; offset < months; offset++) {
            YearMonth month = firstMonth.plusMonths(offset);
            MonthlyTotals totals = totalsByMonth.getOrDefault(month, MonthlyTotals.zero());
            ordered.put(
                    month,
                    new DashboardMonthlyStatisticResponse(
                            month.format(formatter),
                            totals.totalIncome(),
                            totals.totalExpense(),
                            totals.totalIncome().subtract(totals.totalExpense())));
        }

        return ordered.values().stream().toList();
    }

    private DateRange resolveDateRange(DashboardSummaryQueryParams queryParams) {
        LocalDateTime startDate = queryParams.getStartDate();
        LocalDateTime endDate = queryParams.getEndDate();

        if (startDate == null && endDate == null) {
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfMonth = today.withDayOfMonth(1);
            LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
            return new DateRange(
                    firstDayOfMonth.atStartOfDay(),
                    LocalDateTime.of(lastDayOfMonth, LocalTime.MAX));
        }

        if (startDate == null || endDate == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Both startDate and endDate must be provided together.");
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Start date must be before or equal to end date.");
        }

        return new DateRange(startDate, endDate);
    }

    private DashboardRecentTransactionResponse toRecentTransactionResponse(
            Transaction transaction,
            Map<UUID, Wallet> walletsById,
            Map<UUID, Category> categoriesById) {
        Wallet wallet = walletsById.get(transaction.getWalletId());
        Category category = transaction.getCategoryId() == null ? null : categoriesById.get(transaction.getCategoryId());

        return new DashboardRecentTransactionResponse(
                transaction.getId(),
                transaction.getWalletId(),
                wallet == null ? null : wallet.getName(),
                transaction.getCategoryId(),
                category == null ? null : category.getName(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getTitle(),
                transaction.getTransactionDate());
    }

    private DashboardTopSpendingCategoryResponse toTopSpendingCategoryResponse(
            UUID categoryId,
            List<Transaction> transactions,
            Map<UUID, Category> categoriesById) {
        Category category = categoriesById.get(categoryId);
        if (category == null) {
            return null;
        }

        BigDecimal totalAmount = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardTopSpendingCategoryResponse(
                categoryId,
                category.getName(),
                category.getIcon(),
                category.getColor(),
                totalAmount,
                transactions.size());
    }

    private MonthlyTotals toMonthlyTotals(List<Transaction> transactions) {
        BigDecimal totalIncome = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = transactions.stream()
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MonthlyTotals(totalIncome, totalExpense);
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {
    }

    private record MonthlyTotals(BigDecimal totalIncome, BigDecimal totalExpense) {

        private static MonthlyTotals zero() {
            return new MonthlyTotals(BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }
}
