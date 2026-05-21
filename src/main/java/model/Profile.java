package model;

public class Profile {
    private int id;
    private String name;
    private CurrencyType defaultCurrency;

    public Profile() {
    }

    public Profile(int id, String name, CurrencyType defaultCurrency) {
        this.id = id;
        this.name = name;
        this.defaultCurrency = defaultCurrency;
    }

    public Profile(String name, CurrencyType defaultCurrency) {
        this.name = name;
        this.defaultCurrency = defaultCurrency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CurrencyType getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(CurrencyType defaultCurrency) {
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
        return "Profile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", defaultCurrency=" + defaultCurrency +
                '}';
    }
}