package model;

import java.time.LocalDateTime;

public class Profile {
    private Integer id;
    private String name;
    private Currency defaultCurrency;
    private LocalDateTime createdAt;

    public Profile() {
    }

    public Profile(String name, Currency defaultCurrency) {
        this(name, defaultCurrency, LocalDateTime.now());
    }

    public Profile(String name, Currency defaultCurrency, LocalDateTime createdAt) {
        this.name = name;
        this.defaultCurrency = defaultCurrency;
        this.createdAt = createdAt;
    }

    public Profile(Integer id, String name, Currency defaultCurrency, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.defaultCurrency = defaultCurrency;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(Currency defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name + " (" + defaultCurrency + ")";
    }
}