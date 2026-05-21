package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private int id;
    private int profileId;
    private int categoryId;
    private BigDecimal amount;
    private TransactionType type;
    private CurrencyType currency;
    private String description;
    private LocalDate transactionDate;

    public Transaction() {
    }

    public Transaction(int id, int profileId, int categoryId, BigDecimal amount, TransactionType type, CurrencyType currency, String description, LocalDate transactionDate) {
        this.id = id;
        this.profileId = profileId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.currency = currency;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    public Transaction(int profileId, int categoryId, BigDecimal amount, TransactionType type, CurrencyType currency, String description, LocalDate transactionDate) {
        this.profileId = profileId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.currency = currency;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyType currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", profileId=" + profileId +
                ", categoryId=" + categoryId +
                ", amount=" + amount +
                ", type=" + type +
                ", currency=" + currency +
                ", description='" + description + '\'' +
                ", transactionDate=" + transactionDate +
                '}';
    }
}