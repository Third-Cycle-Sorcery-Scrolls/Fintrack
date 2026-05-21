package model;

public enum Currency {

    USD("US Dollar",          "$"),
    EUR("Euro",               "€"),
    GBP("British Pound",      "£"),
    ETB("Ethiopian Birr",     "Br");

    // Fields

    private final String fullName;
    private final String symbol;     

    //Constructor

    Currency(String fullName, String symbol) {
        this.fullName = fullName;
        this.symbol   = symbol;
    }

    // Getters

    public String getFullName() {
        return fullName;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return name() + " – " + fullName + " (" + symbol + ")";
        // Example output:  USD – US Dollar ($)
    }
}
