package ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Category;
import model.Currency;
import model.FrequencyType;
import model.Profile;
import model.RecurringExpense;
import model.Transaction;
import service.ProfileService;
import service.RecurringExpenseService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RecurringExpenseUI {
    private final RecurringExpenseService recurringExpenseService;
    private final ProfileService profileService;
    private final ListView<RecurringExpense> recurringExpenseList = new ListView<>();
    private final TextArea outputArea = new TextArea();

    public RecurringExpenseUI(RecurringExpenseService recurringExpenseService, ProfileService profileService) {
        this.recurringExpenseService = recurringExpenseService;
        this.profileService = profileService;
    }

    public VBox buildView() {
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        ComboBox<Currency> currencyBox = new ComboBox<>(FXCollections.observableArrayList(Currency.values()));
        currencyBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.setMaxWidth(Double.MAX_VALUE);

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        ComboBox<FrequencyType> frequencyBox = new ComboBox<>(FXCollections.observableArrayList(FrequencyType.values()));
        frequencyBox.setValue(FrequencyType.MONTHLY);
        frequencyBox.setMaxWidth(Double.MAX_VALUE);

        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        DatePicker endDatePicker = new DatePicker();
        CheckBox activeCheckBox = new CheckBox("Active");
        activeCheckBox.setSelected(true);

        Button refreshButton = new Button("Refresh");
        Button saveButton = new Button("Create");
        Button updateButton = new Button("Update Selected");
        Button deleteButton = new Button("Delete Selected");
        Button activateButton = new Button("Activate");
        Button deactivateButton = new Button("Deactivate");
        Button generateButton = new Button("Generate Due");

        recurringExpenseList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            amountField.setText(selected.getAmount().toPlainString());
            currencyBox.setValue(selected.getCurrency());
            selectCategory(categoryBox, selected.getCategoryId());
            descriptionField.setText(selected.getDescription() == null ? "" : selected.getDescription());
            frequencyBox.setValue(selected.getFrequency());
            startDatePicker.setValue(selected.getStartDate());
            endDatePicker.setValue(selected.getEndDate());
            activeCheckBox.setSelected(selected.isActive());
        });

        refreshButton.setOnAction(event -> refresh(categoryBox, currencyBox));
        saveButton.setOnAction(event -> {
            try {
                Profile profile = profileService.requireActiveProfile();
                Category category = requireCategory(categoryBox);
                recurringExpenseService.createRecurringExpense(
                        profile.getId(),
                        parseAmount(amountField.getText()),
                        currencyBox.getValue(),
                        category.getId(),
                        descriptionField.getText(),
                        frequencyBox.getValue(),
                        startDatePicker.getValue(),
                        endDatePicker.getValue(),
                        activeCheckBox.isSelected());
                clearForm(amountField, descriptionField, endDatePicker, activeCheckBox);
                refresh(categoryBox, currencyBox);
                outputArea.setText("Recurring expense created.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                outputArea.setText(ex.getMessage());
            }
        });
        updateButton.setOnAction(event -> {
            try {
                RecurringExpense selected = requireSelected();
                Profile profile = profileService.requireActiveProfile();
                Category category = requireCategory(categoryBox);
                recurringExpenseService.updateRecurringExpense(
                        selected.getId(),
                        profile.getId(),
                        parseAmount(amountField.getText()),
                        currencyBox.getValue(),
                        category.getId(),
                        descriptionField.getText(),
                        frequencyBox.getValue(),
                        startDatePicker.getValue(),
                        endDatePicker.getValue(),
                        activeCheckBox.isSelected());
                refresh(categoryBox, currencyBox);
                outputArea.setText("Recurring expense updated.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                outputArea.setText(ex.getMessage());
            }
        });
        deleteButton.setOnAction(event -> {
            try {
                RecurringExpense selected = requireSelected();
                recurringExpenseService.deleteRecurringExpense(selected.getId());
                refresh(categoryBox, currencyBox);
                outputArea.setText("Recurring expense deleted.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                outputArea.setText(ex.getMessage());
            }
        });
        activateButton.setOnAction(event -> setSelectedActive(true, categoryBox, currencyBox));
        deactivateButton.setOnAction(event -> setSelectedActive(false, categoryBox, currencyBox));
        generateButton.setOnAction(event -> {
            try {
                Profile profile = profileService.requireActiveProfile();
                List<Transaction> generated = recurringExpenseService.generateDueRecurringExpenses(profile.getId());
                outputArea.setText("Generated " + generated.size() + " due recurring expense transaction(s).");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                outputArea.setText(ex.getMessage());
            }
        });

        outputArea.setEditable(false);
        outputArea.setPrefRowCount(4);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.addRow(0, new Label("Amount"), amountField);
        form.addRow(1, new Label("Currency"), currencyBox);
        form.addRow(2, new Label("Category"), categoryBox);
        form.addRow(3, new Label("Description"), descriptionField);
        form.addRow(4, new Label("Frequency"), frequencyBox);
        form.addRow(5, new Label("Start date"), startDatePicker);
        form.addRow(6, new Label("End date"), endDatePicker);
        form.addRow(7, new Label("Status"), activeCheckBox);

        HBox crudButtons = new HBox(8, saveButton, updateButton, deleteButton, refreshButton);
        HBox scheduleButtons = new HBox(8, activateButton, deactivateButton, generateButton);

        VBox root = new VBox(10,
                new Label("Recurring Expenses"),
                recurringExpenseList,
                form,
                crudButtons,
                scheduleButtons,
                outputArea);
        root.setPadding(new Insets(16));
        refresh(categoryBox, currencyBox);
        return root;
    }

    private void refresh(ComboBox<Category> categoryBox, ComboBox<Currency> currencyBox) {
        try {
            Profile profile = profileService.requireActiveProfile();
            if (currencyBox.getValue() == null) {
                currencyBox.setValue(profile.getDefaultCurrency());
            }
            categoryBox.getItems().setAll(recurringExpenseService.findCategoriesByProfileId(profile.getId()));
            recurringExpenseList.getItems().setAll(recurringExpenseService.findByProfileId(profile.getId()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            recurringExpenseList.getItems().clear();
            categoryBox.getItems().clear();
            outputArea.setText(ex.getMessage());
        }
    }

    private void setSelectedActive(boolean active, ComboBox<Category> categoryBox, ComboBox<Currency> currencyBox) {
        try {
            RecurringExpense selected = requireSelected();
            recurringExpenseService.setActive(selected.getId(), active);
            refresh(categoryBox, currencyBox);
            outputArea.setText(active ? "Recurring expense activated." : "Recurring expense deactivated.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            outputArea.setText(ex.getMessage());
        }
    }

    private BigDecimal parseAmount(String amountText) {
        try {
            return new BigDecimal(amountText.trim());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Amount must be a valid number.");
        }
    }

    private Category requireCategory(ComboBox<Category> categoryBox) {
        Category category = categoryBox.getValue();
        if (category == null) {
            throw new IllegalArgumentException("Category must be selected.");
        }
        return category;
    }

    private RecurringExpense requireSelected() {
        RecurringExpense selected = recurringExpenseList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Select a recurring expense first.");
        }
        return selected;
    }

    private void selectCategory(ComboBox<Category> categoryBox, int categoryId) {
        for (Category category : categoryBox.getItems()) {
            if (category.getId() == categoryId) {
                categoryBox.setValue(category);
                return;
            }
        }
    }

    private void clearForm(TextField amountField, TextField descriptionField, DatePicker endDatePicker, CheckBox activeCheckBox) {
        amountField.clear();
        descriptionField.clear();
        endDatePicker.setValue(null);
        activeCheckBox.setSelected(true);
    }
}
