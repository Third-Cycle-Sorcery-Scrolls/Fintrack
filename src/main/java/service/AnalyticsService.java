package service;

import model.Transaction;
import model.TransactionType;
import remote.CalculatorClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AnalyticsService {

    public Map<String, String> buildSummary(List<Transaction> transactions, int categoryCount, int recurringExpenseCount) {
        BigDecimal income  = totalByType(transactions, TransactionType.INCOME);
        BigDecimal expense = totalByType(transactions, TransactionType.EXPENSE);
        BigDecimal net     = income.subtract(expense);

        Map<String, String> summary = new LinkedHashMap<>();
        summary.put("Total Transactions",  String.valueOf(transactions.size()));
        summary.put("Total Categories",    String.valueOf(categoryCount));
        summary.put("Recurring Expenses",  String.valueOf(recurringExpenseCount));
        summary.put("Net Balance",         net.toPlainString());
        return summary;
    }

    public BigDecimal totalByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public CompletableFuture<Double> calculateRemoteBalance(List<Transaction> transactions,
                                                            CalculatorClient calculatorClient) {
        BigDecimal income  = totalByType(transactions, TransactionType.INCOME);
        BigDecimal expense = totalByType(transactions, TransactionType.EXPENSE);
        return calculatorClient.calculateBalanceAsync(income.doubleValue(), expense.doubleValue());
    }
}