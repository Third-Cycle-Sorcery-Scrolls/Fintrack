package model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Transaction {
    private Integer id;
    private Integer profileId;
    private LocalDate date;
    private BigDecimal amount;
    private TransactionType type;
    private Currency currency;
    private Integer categoryId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Transaction() {
    }

    public Transaction(Integer id,
                       Integer profileId,
                       Integer categoryId,
                       BigDecimal amount,
                       TransactionType type,
                       Currency currency,
                       String description,
                       LocalDate date) {
        this(id, profileId, date, amount, type, currency, categoryId, description, LocalDateTime.now(), LocalDateTime.now());
    }

    public Transaction(Integer profileId,
                       Integer categoryId,
                       BigDecimal amount,
                       TransactionType type,
                       Currency currency,
                       String description,
                       LocalDate date) {
        this(null, profileId, date, amount, type, currency, categoryId, description, LocalDateTime.now(), LocalDateTime.now());
    }

    public Transaction(Integer id,
                       Integer profileId,
                       LocalDate date,
                       BigDecimal amount,
                       TransactionType type,
                       Currency currency,
                       Integer categoryId,
                       String description,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.profileId = profileId;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.currency = currency;
        this.categoryId = categoryId;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Transaction(Integer profileId,
                       LocalDate date,
                       BigDecimal amount,
                       TransactionType type,
                       Currency currency,
                       Integer categoryId,
                       String description,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this(null, profileId, date, amount, type, currency, categoryId, description, createdAt, updatedAt);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getTransactionDate() {
        return date;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.date = transactionDate;
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

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(date)
                .append(" - ")
                .append(type)
                .append(' ')
                .append(amount)
                .append(' ')
                .append(currency);

        if (categoryId != null) {
            builder.append(" - category #").append(categoryId);
        }

        if (description != null && !description.isBlank()) {
            builder.append(" - ").append(description.trim());
        }

        return builder.toString();
    }
}