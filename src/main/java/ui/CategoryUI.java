package ui;
 
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Category;
import model.Profile;
import service.CategoryService;
import service.ProfileService;
 
public class CategoryUI {
    private final CategoryService categoryService;
    private final ProfileService profileService;
    private final ListView<Category> categoryList = new ListView<>();
    private final TextArea outputArea = new TextArea();
 
    public CategoryUI(CategoryService categoryService, ProfileService profileService) {
        this.categoryService = categoryService;
        this.profileService = profileService;
    }
 
    public VBox buildView() {
 
        // ── Heading ───────────────────────────────────────────────────────────
        Label heading = new Label("Categories");
        heading.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
 
        Label subheading = new Label("Manage your spending categories");
        subheading.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
 
        VBox headingBox = new VBox(2, heading, subheading);
        headingBox.setPadding(new Insets(0, 0, 4, 0));
 
        // ── Category list — fixed height so it doesn't stretch ────────────────
        categoryList.setPrefHeight(180);
        categoryList.setMaxHeight(180);
 
        // ── Section label ─────────────────────────────────────────────────────
        Label listLabel = new Label("Existing Categories");
        listLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
 
        VBox listSection = new VBox(6, listLabel, categoryList);
 
        // ── Separator ─────────────────────────────────────────────────────────
        Separator separator = new Separator();
 
        // ── Form section ──────────────────────────────────────────────────────
        Label formLabel = new Label("Category Name");
        formLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
 
        TextField nameField = new TextField();
        nameField.setPromptText("Enter category name");
        nameField.setMaxWidth(Double.MAX_VALUE);
 
        // ── Buttons with colors ───────────────────────────────────────────────
        Button addButton     = styledButton("Add",             "#2980b9", "#2471a3"); // blue
        Button renameButton  = styledButton("Rename Selected", "#27ae60", "#219150"); // green
        Button deleteButton  = styledButton("Delete Selected", "#e74c3c", "#c0392b"); // red
        Button refreshButton = styledButton("Refresh",         "#7f8c8d", "#717d7e"); // grey
 
        addButton.setOnAction(event -> {
            try {
                Profile profile = profileService.requireActiveProfile();
                Category saved = categoryService.addCategory(profile.getId(), nameField.getText());
                nameField.clear();
                refresh();
                outputArea.setText("Category \"" + saved.getName() + "\" added successfully.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                outputArea.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                outputArea.setText("Database error: " + ex.getMessage());
            }
        });
 
        renameButton.setOnAction(event -> {
            try {
                Category selected = requireSelected();
                Profile profile = profileService.requireActiveProfile();
                Category updated = categoryService.renameCategory(
                        selected.getId(), profile.getId(), nameField.getText());
                nameField.clear();
                refresh();
                outputArea.setText("Category renamed to \"" + updated.getName() + "\" successfully.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                outputArea.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                outputArea.setText("Database error: " + ex.getMessage());
            }
        });
 
        deleteButton.setOnAction(event -> {
            try {
                Category selected = requireSelected();
                Profile profile = profileService.requireActiveProfile();
                categoryService.deleteCategory(selected.getId(), profile.getId());
                refresh();
                outputArea.setText("Category \"" + selected.getName() + "\" deleted successfully.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                outputArea.setText(ex.getMessage());
            } catch (RuntimeException ex) {
                outputArea.setText("Database error: " + ex.getMessage());
            }
        });
 
        refreshButton.setOnAction(event -> refresh());
 
        HBox buttons = new HBox(8, addButton, renameButton, deleteButton, refreshButton);
        buttons.setAlignment(Pos.CENTER_LEFT);
 
        VBox formSection = new VBox(8, formLabel, nameField, buttons);
 
        // ── Separator ─────────────────────────────────────────────────────────
        Separator separator2 = new Separator();
 
        // ── Output area ───────────────────────────────────────────────────────
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(3);
        outputArea.setStyle(
            "-fx-font-family: monospace;" +
            "-fx-font-size: 12px;" +
            "-fx-control-inner-background: #1e1e1e;" +
            "-fx-text-fill: #d4d4d4;"
        );
 
        // ── Root layout ───────────────────────────────────────────────────────
        VBox root = new VBox(12,
                headingBox,
                separator,
                listSection,
                separator2,
                formSection,
                outputArea);
 
        root.setPadding(new Insets(16));
        HBox.setHgrow(root, Priority.ALWAYS);
 
        refresh();
        return root;
    }
 
    // ── Refresh ───────────────────────────────────────────────────────────────
 
    private void refresh() {
        try {
            Profile profile = profileService.requireActiveProfile();
            categoryList.getItems().setAll(categoryService.getCategoriesForProfile(profile.getId()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            categoryList.getItems().clear();
            outputArea.setText(ex.getMessage());
        } catch (RuntimeException ex) {
            categoryList.getItems().clear();
            outputArea.setText("Failed to load categories: " + ex.getMessage());
        }
    }
 
    // ── Require selection ─────────────────────────────────────────────────────
 
    private Category requireSelected() {
        Category selected = categoryList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Select a category from the list first.");
        }
        return selected;
    }
 
    // ── Button style helper ───────────────────────────────────────────────────
 
    private Button styledButton(String text, String color, String hover) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + hover + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }
}