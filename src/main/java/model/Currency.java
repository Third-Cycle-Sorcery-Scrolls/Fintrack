package model;

public enum Currency {
    ETB("Ethiopian Birr", "Br"),
    USD("US Dollar", "$"),
    EUR("Euro", "EUR"),
    GBP("British Pound", "GBP"),
    JPY("Japanese Yen", "JPY"),
    CHF("Swiss Franc", "CHF"),
    CAD("Canadian Dollar", "CAD"),
    AUD("Australian Dollar", "AUD"),
    CNY("Chinese Yuan", "CNY"),
    INR("Indian Rupee", "INR"),
    BRL("Brazilian Real", "BRL");

    private final String fullName;
    private final String symbol;

    Currency(String fullName, String symbol) {
        this.fullName = fullName;
        this.symbol = symbol;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return name() + " - " + fullName + " (" + symbol + ")";
    }
}