package service;

import model.Category;
import model.Currency;
import model.Profile;
import model.Tag;
import model.Transaction;
import model.TransactionType;
import repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ProfileService profileService;
    private final CategoryService categoryService;
    private TagService tagService;

    public TransactionService(TransactionRepository transactionRepository,
                              ProfileService profileService,
                              CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.profileService = profileService;
        this.categoryService = categoryService;
    }

    /** Inject TagService after construction to avoid circular dependency. */
    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }

    // ── create ────────────────────────────────────────────────────────────────

    public Transaction createTransaction(int profileId,
                                         LocalDate date,
                                         BigDecimal amount,
                                         TransactionType type,
                                         Currency currency,
                                         Integer categoryId,
                                         String description) {
        return createTransaction(profileId, date, amount, type, currency, categoryId, description, Collections.emptyList());
    }

    public Transaction createTransaction(int profileId,
                                         LocalDate date,
                                         BigDecimal amount,
                                         TransactionType type,
                                         Currency currency,
                                         Integer categoryId,
                                         String description,
                                         List<Integer> tagIds) {
        Profile profile = requireProfile(profileId);
        validateCommonFields(date, amount, type);

        Currency resolvedCurrency = currency == null ? profile.getDefaultCurrency() : currency;
        Transaction transaction = new Transaction(
                profileId, date, amount, type, resolvedCurrency,
                resolveCategory(profileId, categoryId),
                normalizeDescription(description),
                LocalDateTime.now(), LocalDateTime.now());

        transactionRepository.save(transaction);
        syncTags(transaction.getId(), profileId, tagIds);
        return transaction;
    }

    // ── update ────────────────────────────────────────────────────────────────

    public Transaction updateTransaction(int transactionId,
                                         int profileId,
                                         LocalDate date,
                                         BigDecimal amount,
                                         TransactionType type,
                                         Currency currency,
                                         Integer categoryId,
                                         String description) {
        return updateTransaction(transactionId, profileId, date, amount, type, currency, categoryId, description, null);
    }

    public Transaction updateTransaction(int transactionId,
                                         int profileId,
                                         LocalDate date,
                                         BigDecimal amount,
                                         TransactionType type,
                                         Currency currency,
                                         Integer categoryId,
                                         String description,
                                         List<Integer> tagIds) {
        Transaction existing = requireTransaction(transactionId);
        if (!Objects.equals(existing.getProfileId(), profileId)) {
            throw new IllegalArgumentException("This transaction does not belong to the selected profile.");
        }

        Profile profile = requireProfile(profileId);
        validateCommonFields(date, amount, type);

        existing.setDate(date);
        existing.setAmount(amount);
        existing.setType(type);
        existing.setCurrency(currency == null ? profile.getDefaultCurrency() : currency);
        existing.setCategoryId(resolveCategory(profileId, categoryId));
        existing.setDescription(normalizeDescription(description));
        existing.setUpdatedAt(LocalDateTime.now());

        transactionRepository.update(existing);

        // null means "don't touch tags" (called from old code path)
        if (tagIds != null) {
            syncTags(transactionId, profileId, tagIds);
        }
        return existing;
    }

    // ── delete ────────────────────────────────────────────────────────────────

    public void deleteTransaction(int transactionId) {
        requireTransaction(transactionId);
        transactionRepository.deleteById(transactionId);
    }

    // ── queries ───────────────────────────────────────────────────────────────

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
                .filter(t -> type == null || t.getType() == type)
                .filter(t -> categoryId == null || Objects.equals(t.getCategoryId(), categoryId))
                .filter(t -> fromDate == null || !t.getDate().isBefore(fromDate))
                .filter(t -> toDate == null || !t.getDate().isAfter(toDate))
                .filter(t -> searchText.isBlank()
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchText)))
                .toList();
    }

    public Optional<Transaction> findById(int transactionId) {
        return transactionRepository.findById(transactionId);
    }

    /** Returns tags assigned to a transaction, or empty list if TagService not wired. */
    public List<Tag> getTagsForTransaction(int transactionId) {
        if (tagService == null) return Collections.emptyList();
        return tagService.getTagsForTransaction(transactionId);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    /**
     * Replaces all tag assignments for a transaction with the given tagIds.
     * Silently skips if TagService is not wired.
     */
    private void syncTags(int transactionId, int profileId, List<Integer> tagIds) {
        if (tagService == null) return;

        // Remove all existing assignments
        for (Tag existing : tagService.getTagsForTransaction(transactionId)) {
            tagService.removeTag(transactionId, existing.getId());
        }
        // Assign new ones (validateing profile ownership)
        for (int tagId : tagIds) {
            tagService.assignTag(transactionId, tagId, profileId);
        }
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
        if (categoryId == null) return null;
        Category category = categoryService.getCategoryById(categoryId);
        if (category == null) throw new IllegalArgumentException("No category found with id: " + categoryId);
        if (category.getProfileId() != profileId)
            throw new IllegalArgumentException("This category does not belong to the selected profile.");
        return categoryId;
    }

    private void validateCommonFields(LocalDate date, BigDecimal amount, TransactionType type) {
        if (date == null) throw new IllegalArgumentException("Transaction date is required.");
        if (amount == null) throw new IllegalArgumentException("Transaction amount is required.");
        if (amount.signum() <= 0) throw new IllegalArgumentException("Transaction amount must be greater than zero.");
        if (type == null) throw new IllegalArgumentException("Transaction type is required.");
    }

    private String normalizeDescription(String description) {
        if (description == null) return null;
        String normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
