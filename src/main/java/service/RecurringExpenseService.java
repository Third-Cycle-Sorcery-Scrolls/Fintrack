package service;

import model.RecurringExpense;
import model.Transaction;
import model.TransactionType;
import model.FrequencyType;
import repository.RecurringExpenseRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecurringExpenseService {

    private final RecurringExpenseRepository repository;

    public RecurringExpenseService(RecurringExpenseRepository repository) {
        this.repository = repository;
    }

    public RecurringExpense createRecurringExpense(RecurringExpense re) {
        validate(re);
        return repository.save(re);
    }

    public List<RecurringExpense> getAllRecurringExpenses() {
        return repository.findAll();
    }

    public List<RecurringExpense> getRecurringExpensesByProfile(int profileId) {
        return repository.findByProfileId(profileId);
    }

    public Optional<RecurringExpense> getRecurringExpenseById(int id) {
        return repository.findById(id);
    }

    public void updateRecurringExpense(RecurringExpense re) {
        validate(re);
        repository.update(re);
    }

    public void deleteRecurringExpense(int id) {
        repository.deleteById(id);
    }

    /**
     * Generates a list of due transactions for a profile based on active recurring expenses.
     * Note: This calculates occurrences between start_date and upToDate.
     * In a production environment, this should ideally use a 'last_processed_date' 
     * but we are constrained by the existing database schema.
     */
    public List<Transaction> generateDueTransactions(int profileId, LocalDate upToDate) {
        List<RecurringExpense> recurringExpenses = repository.findByProfileId(profileId);
        List<Transaction> dueTransactions = new ArrayList<>();

        for (RecurringExpense re : recurringExpenses) {
            if (!re.isActive()) continue;

            LocalDate currentOccurrence = re.getStartDate();
            
            while (!currentOccurrence.isAfter(upToDate)) {
                // If there's an end date and we've passed it, stop generating
                if (re.getEndDate() != null && currentOccurrence.isAfter(re.getEndDate())) {
                    break;
                }
                
                dueTransactions.add(new Transaction(
                        re.getProfileId(),
                        re.getCategoryId(),
                        re.getAmount(),
                        TransactionType.EXPENSE,
                        re.getCurrency(),
                        re.getDescription() + " (Recurring)",
                        currentOccurrence
                ));

                currentOccurrence = getNextOccurrence(currentOccurrence, re.getFrequency());
            }
        }

        return dueTransactions;
    }

    private LocalDate getNextOccurrence(LocalDate current, FrequencyType frequency) {
        return switch (frequency) {
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }

    private void validate(RecurringExpense re) {
        if (re.getAmount() == null || re.getAmount().doubleValue() <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (re.getProfileId() <= 0) {
            throw new IllegalArgumentException("Valid Profile ID is required.");
        }
        if (re.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Valid Category ID is required.");
        }
        if (re.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required.");
        }
        if (re.getEndDate() != null && re.getEndDate().isBefore(re.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
    }
}
