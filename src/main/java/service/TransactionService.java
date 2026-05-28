package service;

import model.Category;
import model.Currency;
import model.Profile;
import model.Transaction;
import model.TransactionType;
import repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ProfileService profileService;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository,
                              ProfileService profileService,
                              CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.profileService = profileService;
        this.categoryService = categoryService;
    }

    public Transaction createTransaction(int profileId,
                                         LocalDate date,
                                         BigDecimal amount,
                                         TransactionType type,
                                         Currency currency,
                                         Integer categoryId,
                                         String description) {
        Profile profile = requireProfile(profileId);
        validateCommonFields(date, amount, type);

        Currency resolvedCurrency = currency == null ? profile.getDefaultCurrency() : currency;
        Integer resolvedCategoryId = resolveCategory(profileId, categoryId);
        String normalizedDescription = normalizeDescription(description);

        Transaction transaction = new Transaction(
                profileId,
                date,
                amount,
                type,
                resolvedCurrency,
                resolvedCategoryId,
                normalizedDescription,
                LocalDateTime.now(),
                LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(int transactionId,
                                         int profileId,
                                         LocalDate date,
                                         BigDecimal amount,
                                         TransactionType type,
                                         Currency currency,
                                         Integer categoryId,
                                         String description) {
        Transaction existing = requireTransaction(transactionId);
        if (!Objects.equals(existing.getProfileId(), profileId)) {
            throw new IllegalArgumentException("This transaction does not belong to the selected profile.");
        }

        Profile profile = requireProfile(profileId);
        validateCommonFields(date, amount, type);

        existing.setProfileId(profileId);
        existing.setDate(date);
        existing.setAmount(amount);
        existing.setType(type);
        existing.setCurrency(currency == null ? profile.getDefaultCurrency() : currency);
        existing.setCategoryId(resolveCategory(profileId, categoryId));
        existing.setDescription(normalizeDescription(description));
        existing.setUpdatedAt(LocalDateTime.now());

        transactionRepository.update(existing);
        return existing;
    }

    public void deleteTransaction(int transactionId) {
        requireTransaction(transactionId);
        transactionRepository.deleteById(transactionId);
    }

    public List<Transaction> getTransactionsForProfile(int profileId) {
        requireProfile(profileId);
        return transactionRepository.findByProfileId(profileId);
    }

    public List<Transaction> filterTransactions(int profileId,
                                                TransactionType type,
                                                Integer categoryId,
                                                LocalDate fromDate,
                                                LocalDate toDate,
                                                String descriptionQuery) {
        requireProfile(profileId);
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date cannot be after to date.");
        }

        String searchText = descriptionQuery == null ? "" : descriptionQuery.trim().toLowerCase();
        return transactionRepository.findByProfileId(profileId).stream()
                .filter(transaction -> type == null || transaction.getType() == type)
                .filter(transaction -> categoryId == null || Objects.equals(transaction.getCategoryId(), categoryId))
                .filter(transaction -> fromDate == null || !transaction.getDate().isBefore(fromDate))
                .filter(transaction -> toDate == null || !transaction.getDate().isAfter(toDate))
                .filter(transaction -> searchText.isBlank()
                        || (transaction.getDescription() != null
                        && transaction.getDescription().toLowerCase().contains(searchText)))
                .toList();
    }

    public Optional<Transaction> findById(int transactionId) {
        return transactionRepository.findById(transactionId);
    }

    private Transaction requireTransaction(int transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("No transaction found with id: " + transactionId));
    }

    private Profile requireProfile(int profileId) {
        return profileService.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("No profile found with id: " + profileId));
    }

    private Integer resolveCategory(int profileId, Integer categoryId) {
        if (categoryId == null) {
            return null;
        }

        Category category = categoryService.getCategoryById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("No category found with id: " + categoryId);
        }
        if (category.getProfileId() != profileId) {
            throw new IllegalArgumentException("This category does not belong to the selected profile.");
        }
        return categoryId;
    }

    private void validateCommonFields(LocalDate date, BigDecimal amount, TransactionType type) {
        if (date == null) {
            throw new IllegalArgumentException("Transaction date is required.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Transaction amount is required.");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type is required.");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}