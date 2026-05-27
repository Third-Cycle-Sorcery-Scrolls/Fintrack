package service;

import model.Category;
import model.Currency;
import model.FrequencyType;
import model.RecurringExpense;
import model.Transaction;
import repository.RecurringExpenseRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecurringExpenseService {
    private final RecurringExpenseRepository recurringExpenseRepository;

    public RecurringExpenseService(RecurringExpenseRepository recurringExpenseRepository) {
        this.recurringExpenseRepository = recurringExpenseRepository;
    }

    public RecurringExpense createRecurringExpense(int profileId, BigDecimal amount, Currency currency, int categoryId,
            String description, FrequencyType frequency, LocalDate startDate, LocalDate endDate, boolean active) {
        RecurringExpense recurringExpense = new RecurringExpense(
                profileId,
                categoryId,
                validateAmount(amount),
                requireCurrency(currency),
                normalizeDescription(description),
                requireFrequency(frequency),
                requireStartDate(startDate),
                validateEndDate(startDate, endDate),
                active);
        validateProfileAndCategory(profileId, categoryId);
        return recurringExpenseRepository.save(recurringExpense);
    }

    public RecurringExpense save(RecurringExpense recurringExpense) {
        if (recurringExpense == null) {
            throw new IllegalArgumentException("Recurring expense is required.");
        }
        return createRecurringExpense(
                recurringExpense.getProfileId(),
                recurringExpense.getAmount(),
                recurringExpense.getCurrency(),
                recurringExpense.getCategoryId(),
                recurringExpense.getDescription(),
                recurringExpense.getFrequency(),
                recurringExpense.getStartDate(),
                recurringExpense.getEndDate(),
                recurringExpense.isActive());
    }

    public List<RecurringExpense> findAll() {
        return recurringExpenseRepository.findAll();
    }

    public List<RecurringExpense> findByProfileId(int profileId) {
        validateProfileId(profileId);
        return recurringExpenseRepository.findByProfileId(profileId);
    }

    public Optional<RecurringExpense> findById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Recurring expense id cannot be null.");
        }
        return recurringExpenseRepository.findById(id);
    }

    public RecurringExpense getRecurringExpenseById(Integer id) {
        return findById(id).orElse(null);
    }

    public void update(RecurringExpense recurringExpense) {
        if (recurringExpense == null) {
            throw new IllegalArgumentException("Recurring expense is required.");
        }
        updateRecurringExpense(
                recurringExpense.getId(),
                recurringExpense.getProfileId(),
                recurringExpense.getAmount(),
                recurringExpense.getCurrency(),
                recurringExpense.getCategoryId(),
                recurringExpense.getDescription(),
                recurringExpense.getFrequency(),
                recurringExpense.getStartDate(),
                recurringExpense.getEndDate(),
                recurringExpense.isActive());
    }

    public boolean updateRecurringExpense(int id, int profileId, BigDecimal amount, Currency currency, int categoryId,
            String description, FrequencyType frequency, LocalDate startDate, LocalDate endDate, boolean active) {
        if (id <= 0) {
            throw new IllegalArgumentException("Recurring expense id must be valid.");
        }
        recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No recurring expense found with id: " + id));

        validateProfileAndCategory(profileId, categoryId);
        RecurringExpense updated = new RecurringExpense(
                id,
                profileId,
                categoryId,
                validateAmount(amount),
                requireCurrency(currency),
                normalizeDescription(description),
                requireFrequency(frequency),
                requireStartDate(startDate),
                validateEndDate(startDate, endDate),
                active);
        recurringExpenseRepository.update(updated);
        return true;
    }

    public void deleteById(Integer id) {
        deleteRecurringExpense(id);
    }

    public boolean deleteRecurringExpense(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Recurring expense id must be valid.");
        }
        recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No recurring expense found with id: " + id));
        recurringExpenseRepository.deleteById(id);
        return true;
    }

    public boolean setActive(int id, boolean active) {
        recurringExpenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No recurring expense found with id: " + id));
        recurringExpenseRepository.setActive(id, active);
        return true;
    }

    public List<Transaction> generateDueRecurringExpenses(int profileId) {
        return generateDueRecurringExpenses(profileId, LocalDate.now());
    }

    public List<Transaction> generateDueRecurringExpenses(int profileId, LocalDate dueThrough) {
        validateProfileId(profileId);
        if (dueThrough == null) {
            throw new IllegalArgumentException("Due-through date is required.");
        }

        List<Transaction> generatedTransactions = new ArrayList<>();
        for (RecurringExpense recurringExpense : recurringExpenseRepository.findActiveByProfileId(profileId)) {
            for (LocalDate dueDate : getDueDates(recurringExpense, dueThrough)) {
                recurringExpenseRepository.saveGeneratedExpense(recurringExpense, dueDate)
                        .ifPresent(generatedTransactions::add);
            }
        }
        return generatedTransactions;
    }

    public List<LocalDate> getDueDates(RecurringExpense recurringExpense, LocalDate dueThrough) {
        if (recurringExpense == null) {
            throw new IllegalArgumentException("Recurring expense is required.");
        }
        if (dueThrough == null) {
            throw new IllegalArgumentException("Due-through date is required.");
        }

        List<LocalDate> dueDates = new ArrayList<>();
        if (!recurringExpense.isActive() || recurringExpense.getStartDate().isAfter(dueThrough)) {
            return dueDates;
        }

        LocalDate finalDueDate = recurringExpense.getEndDate() == null || recurringExpense.getEndDate().isAfter(dueThrough)
                ? dueThrough
                : recurringExpense.getEndDate();
        int occurrence = 0;
        LocalDate current = dueDateFor(recurringExpense.getStartDate(), recurringExpense.getFrequency(), occurrence);
        while (!current.isAfter(finalDueDate)) {
            dueDates.add(current);
            occurrence++;
            current = dueDateFor(recurringExpense.getStartDate(), recurringExpense.getFrequency(), occurrence);
        }
        return dueDates;
    }

    public LocalDate nextDueDate(LocalDate currentDate, FrequencyType frequency) {
        if (currentDate == null) {
            throw new IllegalArgumentException("Current date is required.");
        }
        return switch (requireFrequency(frequency)) {
            case WEEKLY -> currentDate.plusWeeks(1);
            case MONTHLY -> currentDate.plusMonths(1);
            case YEARLY -> currentDate.plusYears(1);
        };
    }

    public List<Category> findCategoriesByProfileId(int profileId) {
        validateProfileId(profileId);
        return recurringExpenseRepository.findCategoriesByProfileId(profileId);
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        return amount;
    }

    private Currency requireCurrency(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency must be selected.");
        }
        return currency;
    }

    private FrequencyType requireFrequency(FrequencyType frequency) {
        if (frequency == null) {
            throw new IllegalArgumentException("Frequency must be selected.");
        }
        return frequency;
    }

    private LocalDate requireStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date is required.");
        }
        return startDate;
    }

    private LocalDate validateEndDate(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        return endDate;
    }

    private String normalizeDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        return description.trim();
    }

    private void validateProfileAndCategory(int profileId, int categoryId) {
        validateProfileId(profileId);
        if (categoryId <= 0) {
            throw new IllegalArgumentException("Category must be selected.");
        }
    }

    private void validateProfileId(int profileId) {
        if (profileId <= 0) {
            throw new IllegalArgumentException("Profile id must be valid.");
        }
    }

    private LocalDate dueDateFor(LocalDate startDate, FrequencyType frequency, int occurrence) {
        return switch (requireFrequency(frequency)) {
            case WEEKLY -> startDate.plusWeeks(occurrence);
            case MONTHLY -> startDate.plusMonths(occurrence);
            case YEARLY -> startDate.plusYears(occurrence);
        };
    }
}
