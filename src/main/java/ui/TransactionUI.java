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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Category;
import model.Currency;
import model.Profile;
import model.Tag;
import model.Transaction;
import model.TransactionType;
import service.CategoryService;
import service.ProfileService;
import service.TagService;
import service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransactionUI {
    private final TransactionService transactionService;
    private final ProfileService profileService;
    private final CategoryService categoryService;
    private final TagService tagService;

    private final ListView<Transaction> transactionList = new ListView<>();
    private final TextArea outputArea = new TextArea();

    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final TextField amountField = new TextField();
    private final ComboBox<TransactionType> typeBox = new ComboBox<>(FXCollections.observableArrayList(TransactionType.values()));
    private final ComboBox<Currency> currencyBox = new ComboBox<>(FXCollections.observableArrayList(Currency.values()));
    private final ComboBox<Category> categoryBox = new ComboBox<>();
    private final TextField descriptionField = new TextField();

    // Tag controls
    private final ListView<Tag> tagListView = new ListView<>();
    private final TextField newTagField = new TextField();

    private final ComboBox<TransactionType> filterTypeBox = new ComboBox<>();
    private final ComboBox<Category> filterCategoryBox = new ComboBox<>();
    private final DatePicker fromDatePicker = new DatePicker();
    private final DatePicker toDatePicker = new DatePicker();
    private final TextField searchField = new TextField();

    public TransactionUI(TransactionService transactionService,
                         ProfileService profileService,
                         CategoryService categoryService) {
        this(transactionService, profileService, categoryService, null);
    }

    public TransactionUI(TransactionService transactionService,
                         ProfileService profileService,
                         CategoryService categoryService,
                         TagService tagService) {
        this.transactionService = transactionService;
        this.profileService = profileService;
        this.categoryService = categoryService;
        this.tagService = tagService;
    }

    public ScrollPane buildView() {
        Label title = new Label("Transactions");
        title.getStyleClass().add("page-title");

        setupFormControls();
        setupFilterControls();
        setupTransactionList();
        setupTagControls();

        Button createButton = new Button("Create");
        Button updateButton = new Button("Update Selected");
        Button deleteButton = new Button("Delete Selected");
        Button refreshButton = new Button("Refresh");
        Button filterButton = new Button("Apply Filters");
        Button clearFiltersButton = new Button("Clear Filters");
        Button addTagButton = new Button("Add Tag");

        transactionList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, selected) -> populateForm(selected));

        createButton.setOnAction(e -> handleCreate());
        updateButton.setOnAction(e -> handleUpdate());
        deleteButton.setOnAction(e -> handleDelete());
        refreshButton.setOnAction(e -> refreshTransactions());
        filterButton.setOnAction(e -> applyFilters());
        clearFiltersButton.setOnAction(e -> clearFilters());
        addTagButton.setOnAction(e -> handleAddNewTag());

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

        // Tag section
        HBox newTagRow = new HBox(8, newTagField, addTagButton);
        VBox tagSection = new VBox(6,
                new Label("Tags (hold Ctrl/Cmd to select multiple)"),
                tagListView,
                new Label("Quick-create new tag:"),
                newTagRow);

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
                tagSection,
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
                setText((empty || item == null) ? null : formatTransaction(item));
            }
        });
    }

    private void setupTagControls() {
        tagListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tagListView.setPrefHeight(120);
        newTagField.setPromptText("New tag name...");
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleCreate() {
        try {
            Profile profile = profileService.requireActiveProfile();
            List<Integer> tagIds = selectedTagIds();
            Transaction saved = transactionService.createTransaction(
                    profile.getId(),
                    datePicker.getValue(),
                    parseAmount(amountField.getText()),
                    typeBox.getValue(),
                    currencyBox.getValue(),
                    selectedCategoryId(categoryBox.getValue()),
                    descriptionField.getText(),
                    tagIds);
            outputArea.setText("Transaction created: " + saved.getId()
                    + (tagIds.isEmpty() ? "" : " with " + tagIds.size() + " tag(s)."));
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
            List<Integer> tagIds = selectedTagIds();
            Transaction updated = transactionService.updateTransaction(
                    selected.getId(),
                    profile.getId(),
                    datePicker.getValue(),
                    parseAmount(amountField.getText()),
                    typeBox.getValue(),
                    currencyBox.getValue(),
                    selectedCategoryId(categoryBox.getValue()),
                    descriptionField.getText(),
                    tagIds);
            outputArea.setText("Transaction updated: " + updated.getId()
                    + " — tags synced (" + tagIds.size() + ").");
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

    /** Quick-create a new tag for the active profile and select it. */
    private void handleAddNewTag() {
        if (tagService == null) {
            outputArea.setText("Tag service not available.");
            return;
        }
        String name = newTagField.getText().trim();
        if (name.isEmpty()) {
            outputArea.setText("Enter a tag name first.");
            return;
        }
        try {
            Profile profile = profileService.requireActiveProfile();
            Tag created = tagService.createTag(profile.getId(), name);
            newTagField.clear();
            refreshTagList();
            // Auto-select the newly created tag
            tagListView.getSelectionModel().select(created);
            outputArea.setText("Tag \"" + created.getName() + "\" created and selected.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            outputArea.setText(ex.getMessage());
        }
    }

    // ── Filter / refresh ──────────────────────────────────────────────────────

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
            filterCategoryBox.getItems().setAll((Category) null);
            filterCategoryBox.getItems().addAll(categories);
            if (currencyBox.getValue() == null) {
                currencyBox.setValue(profile.getDefaultCurrency());
            }
            transactionList.getItems().setAll(transactionService.getTransactionsForProfile(profile.getId()));
            refreshTagList();
            outputArea.setText("Loaded " + transactionList.getItems().size() + " transaction(s).");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            transactionList.getItems().clear();
            categoryBox.getItems().clear();
            filterCategoryBox.getItems().clear();
            tagListView.getItems().clear();
            outputArea.setText(ex.getMessage());
        }
    }

    private void refreshTagList() {
        if (tagService == null) return;
        try {
            Profile profile = profileService.requireActiveProfile();
            tagListView.getItems().setAll(tagService.getAllTagsForProfile(profile.getId()));
        } catch (RuntimeException ex) {
            tagListView.getItems().clear();
        }
    }

    // ── Populate form on selection ────────────────────────────────────────────

    private void populateForm(Transaction selected) {
        if (selected == null) return;

        datePicker.setValue(selected.getDate());
        amountField.setText(selected.getAmount() == null ? "" : selected.getAmount().toPlainString());
        typeBox.setValue(selected.getType());
        currencyBox.setValue(selected.getCurrency());
        descriptionField.setText(selected.getDescription() == null ? "" : selected.getDescription());

        Category matchedCategory = null;
        if (selected.getCategoryId() != null) {
            for (Category c : categoryBox.getItems()) {
                if (Objects.equals(c.getId(), selected.getCategoryId())) {
                    matchedCategory = c;
                    break;
                }
            }
        }
        categoryBox.setValue(matchedCategory);

        // Load and select assigned tags
        tagListView.getSelectionModel().clearSelection();
        List<Tag> assignedTags = transactionService.getTagsForTransaction(selected.getId());
        for (Tag assigned : assignedTags) {
            for (Tag available : tagListView.getItems()) {
                if (available.getId() == assigned.getId()) {
                    tagListView.getSelectionModel().select(available);
                    break;
                }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<Integer> selectedTagIds() {
        return tagListView.getSelectionModel().getSelectedItems()
                .stream().map(Tag::getId).collect(Collectors.toList());
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
        if (selected == null) throw new IllegalArgumentException("Select a transaction first.");
        return selected;
    }

    private String formatTransaction(Transaction t) {
        String categoryName = "Uncategorized";
        if (t.getCategoryId() != null) {
            Category c = categoryService.getCategoryById(t.getCategoryId());
            if (c != null) categoryName = c.getName();
        }
        String desc = (t.getDescription() == null || t.getDescription().isBlank()) ? "No description" : t.getDescription();
        return t.getDate() + " | " + t.getType() + " | " + t.getAmount() + " " + t.getCurrency()
                + " | " + categoryName + " | " + desc;
    }

    private void clearForm() {
        datePicker.setValue(LocalDate.now());
        amountField.clear();
        typeBox.setValue(TransactionType.EXPENSE);
        descriptionField.clear();
        categoryBox.setValue(null);
        tagListView.getSelectionModel().clearSelection();
    }
}
