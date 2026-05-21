package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RecurringExpense {
    private int id;
    private int profileId;
    private int categoryId;
    private BigDecimal amount;
    private CurrencyType currency;
    private String description;
    private FrequencyType frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    public RecurringExpense() {
    }

    public RecurringExpense(int id, int profileId, int categoryId, BigDecimal amount, CurrencyType currency, String description, FrequencyType frequency, LocalDate startDate, LocalDate endDate, boolean active) {
        this.id = id;
        this.profileId = profileId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    public RecurringExpense(int profileId, int categoryId, BigDecimal amount, CurrencyType currency, String description, FrequencyType frequency, LocalDate startDate, LocalDate endDate, boolean active) {
        this.profileId = profileId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
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

    public FrequencyType getFrequency() {
        return frequency;
    }

    public void setFrequency(FrequencyType frequency) {
        this.frequency = frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "RecurringExpense{" +
                "id=" + id +
                ", profileId=" + profileId +
                ", categoryId=" + categoryId +
                ", amount=" + amount +
                ", currency=" + currency +
                ", description='" + description + '\'' +
                ", frequency=" + frequency +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", active=" + active +
                '}';
    }
}