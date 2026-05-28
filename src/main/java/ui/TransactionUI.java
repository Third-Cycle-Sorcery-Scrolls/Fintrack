package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Category;
import model.Currency;
import model.Profile;
import model.Transaction;
import model.TransactionType;
import service.CategoryService;
import service.ProfileService;
import service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.List;

public class TransactionUI {
    private final TransactionService transactionService;
    private final ProfileService profileService;
    private final CategoryService categoryService;

    private final ListView<Transaction> transactionList = new ListView<>();
    private final TextArea outputArea = new TextArea();

    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final TextField amountField = new TextField();
    private final ComboBox<TransactionType> typeBox = new ComboBox<>(FXCollections.observableArrayList(TransactionType.values()));
    private final ComboBox<Currency> currencyBox = new ComboBox<>(FXCollections.observableArrayList(Currency.values()));
    private final ComboBox<Category> categoryBox = new ComboBox<>();
    private final TextField descriptionField = new TextField();

    private final ComboBox<TransactionType> filterTypeBox = new ComboBox<>();
    private final ComboBox<Category> filterCategoryBox = new ComboBox<>();
    private final DatePicker fromDatePicker = new DatePicker();
    private final DatePicker toDatePicker = new DatePicker();
    private final TextField searchField = new TextField();

    public TransactionUI(TransactionService transactionService,
                         ProfileService profileService,
                         CategoryService categoryService) {
        this.transactionService = transactionService;
        this.profileService = profileService;
        this.categoryService = categoryService;
    }

    public ScrollPane buildView() {
        Label title = new Label("Transactions");
        title.getStyleClass().add("page-title");

        setupFormControls();
        setupFilterControls();
        setupTransactionList();

        Button createButton = new Button("Create");
        Button updateButton = new Button("Update Selected");
        Button deleteButton = new Button("Delete Selected");
        Button refreshButton = new Button("Refresh");
        Button filterButton = new Button("Apply Filters");
        Button clearFiltersButton = new Button("Clear Filters");

        transactionList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> populateForm(selected));

        createButton.setOnAction(event -> handleCreate());
        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> handleDelete());
        refreshButton.setOnAction(event -> refreshTransactions());
        filterButton.setOnAction(event -> applyFilters());
        clearFiltersButton.setOnAction(event -> clearFilters());

        GridPane filterGrid = new GridPane();
        filterGrid.setHgap(12);
        filterGrid.setVgap(10);
        filterGrid.addRow(0, new Label("Type"), filterTypeBox);
        filterGrid.addRow(1, new Label("Category"), filterCategoryBox);
        filterGrid.addRow(2, new Label("From Date"), fromDatePicker);
        filterGrid.addRow(3, new Label("To Date"), toDatePicker);
        filterGrid.addRow(4, new Label("Search"), searchField);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(12);
        formGrid.setVgap(10);
        formGrid.addRow(0, new Label("Date"), datePicker);
        formGrid.addRow(1, new Label("Amount"), amountField);
        formGrid.addRow(2, new Label("Type"), typeBox);
        formGrid.addRow(3, new Label("Currency"), currencyBox);
        formGrid.addRow(4, new Label("Category"), categoryBox);
        formGrid.addRow(5, new Label("Description"), descriptionField);

        HBox filterButtons = new HBox(10, filterButton, clearFiltersButton, refreshButton);
        HBox crudButtons = new HBox(10, createButton, updateButton, deleteButton);

        outputArea.setEditable(false);
        outputArea.setPrefRowCount(4);

        VBox content = new VBox(15,
                title,
                new Label("Filters"),
                filterGrid,
                filterButtons,
                transactionList,
                new Label("Add or Edit Transaction"),
                formGrid,
                crudButtons,
                outputArea);
        content.setPadding(new Insets(18));

        ScrollPane root = new ScrollPane(content);
        root.setFitToWidth(true);
        root.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        refreshFromActiveProfile();
        return root;
    }

    private void setupFormControls() {
        amountField.setPromptText("Amount");
        descriptionField.setPromptText("Description");
        typeBox.getItems().setAll(TransactionType.values());
        typeBox.setValue(TransactionType.EXPENSE);
        currencyBox.setMaxWidth(Double.MAX_VALUE);
        categoryBox.setMaxWidth(Double.MAX_VALUE);
    }

    private void setupFilterControls() {
        filterTypeBox.getItems().add(null);
        filterTypeBox.getItems().addAll(TransactionType.values());
        filterTypeBox.setPromptText("All Types");
        filterCategoryBox.setPromptText("All Categories");
        searchField.setPromptText("Description contains...");
    }

    private void setupTransactionList() {
        transactionList.setPrefHeight(260);
        transactionList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatTransaction(item));
                }
            }
        });
    }

    private void handleCreate() {
        try {
            Profile profile = profileService.requireActiveProfile();
            Transaction saved = transactionService.createTransaction(
                    profile.getId(),
                    datePicker.getValue(),
                    parseAmount(amountField.getText()),
                    typeBox.getValue(),
                    currencyBox.getValue(),
                    selectedCategoryId(categoryBox.getValue()),
                    descriptionField.getText());
            outputArea.setText("Transaction created: " + saved.getId());
            clearForm();
            applyFilters();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            outputArea.setText(ex.getMessage());
        }
    }

    private void handleUpdate() {
        try {
            Transaction selected = requireSelectedTransaction();
            Profile profile = profileService.requireActiveProfile();
            Transaction updated = transactionService.updateTransaction(
                    selected.getId(),
                    profile.getId(),
                    datePicker.getValue(),
                    parseAmount(amountField.getText()),
                    typeBox.getValue(),
                    currencyBox.getValue(),
                    selectedCategoryId(categoryBox.getValue()),
                    descriptionField.getText());
            outputArea.setText("Transaction updated: " + updated.getId());
            applyFilters();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            outputArea.setText(ex.getMessage());
        }
    }

    private void handleDelete() {
        try {
            Transaction selected = requireSelectedTransaction();
            transactionService.deleteTransaction(selected.getId());
            outputArea.setText("Transaction deleted.");
            clearForm();
            applyFilters();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            outputArea.setText(ex.getMessage());
        }
    }

    private void applyFilters() {
        try {
            Profile profile = profileService.requireActiveProfile();
            Integer categoryId = selectedCategoryId(filterCategoryBox.getValue());
            List<Transaction> filtered = transactionService.filterTransactions(
                    profile.getId(),
                    filterTypeBox.getValue(),
                    categoryId,
                    fromDatePicker.getValue(),
                    toDatePicker.getValue(),
                    searchField.getText());
            transactionList.getItems().setAll(filtered);
            outputArea.setText("Showing " + filtered.size() + " transaction(s).");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            outputArea.setText(ex.getMessage());
        }
    }

    private void clearFilters() {
        filterTypeBox.setValue(null);
        filterCategoryBox.setValue(null);
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        searchField.clear();
        refreshFromActiveProfile();
    }

    private void refreshTransactions() {
        refreshFromActiveProfile();
    }

    private void refreshFromActiveProfile() {
        try {
            Profile profile = profileService.requireActiveProfile();
            List<Category> categories = categoryService.getCategoriesForProfile(profile.getId());
            categoryBox.getItems().setAll(categories);
            // Allow "All Categories" selection (null) while still listing categories
            filterCategoryBox.getItems().setAll((Category) null);
            filterCategoryBox.getItems().addAll(categories);
            if (currencyBox.getValue() == null) {
                currencyBox.setValue(profile.getDefaultCurrency());
            }
            transactionList.getItems().setAll(transactionService.getTransactionsForProfile(profile.getId()));
            outputArea.setText("Loaded " + transactionList.getItems().size() + " transaction(s).");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            transactionList.getItems().clear();
            categoryBox.getItems().clear();
            filterCategoryBox.getItems().clear();
            outputArea.setText(ex.getMessage());
        }
    }

    private void populateForm(Transaction selected) {
        if (selected == null) {
            return;
        }

        datePicker.setValue(selected.getDate());
        amountField.setText(selected.getAmount() == null ? "" : selected.getAmount().toPlainString());
        typeBox.setValue(selected.getType());
        currencyBox.setValue(selected.getCurrency());
        descriptionField.setText(selected.getDescription() == null ? "" : selected.getDescription());

        Category matchedCategory = null;
        if (selected.getCategoryId() != null) {
            for (Category category : categoryBox.getItems()) {
                if (Objects.equals(category.getId(), selected.getCategoryId())) {
                    matchedCategory = category;
                    break;
                }
            }
        }
        categoryBox.setValue(matchedCategory);
    }

    private BigDecimal parseAmount(String amountText) {
        try {
            return new BigDecimal(amountText.trim());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Amount must be a valid number.");
        }
    }

    private Integer selectedCategoryId(Category category) {
        return category == null ? null : category.getId();
    }

    private Transaction requireSelectedTransaction() {
        Transaction selected = transactionList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Select a transaction first.");
        }
        return selected;
    }

    private String formatTransaction(Transaction transaction) {
        String categoryName = "Uncategorized";
        if (transaction.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(transaction.getCategoryId());
            if (category != null) {
                categoryName = category.getName();
            }
        }

        String description = transaction.getDescription();
        if (description == null || description.isBlank()) {
            description = "No description";
        }

        return transaction.getDate()
                + " | "
                + transaction.getType()
                + " | "
                + transaction.getAmount()
                + " "
                + transaction.getCurrency()
                + " | "
                + categoryName
                + " | "
                + description;
    }

    private void clearForm() {
        datePicker.setValue(LocalDate.now());
        amountField.clear();
        typeBox.setValue(TransactionType.EXPENSE);
        descriptionField.clear();
        categoryBox.setValue(null);
    }
}