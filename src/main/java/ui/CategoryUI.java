package ui;
 
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
        // ── Input fields ──────────────────────────────────────────────────────
        TextField nameField = new TextField();
        nameField.setPromptText("Category name");
 
        // ── Buttons ───────────────────────────────────────────────────────────
        Button refreshButton = new Button("Refresh");
        Button addButton     = new Button("Add");
        Button renameButton  = new Button("Rename Selected");
        Button deleteButton  = new Button("Delete Selected");
 
        // ── Button actions ────────────────────────────────────────────────────
        refreshButton.setOnAction(event -> refresh());
 
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
 
        // ── Output area ───────────────────────────────────────────────────────
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(4);
 
        // ── Button bar ────────────────────────────────────────────────────────
        HBox buttons = new HBox(8, addButton, renameButton, deleteButton, refreshButton);
 
        // ── Root layout ───────────────────────────────────────────────────────
        VBox root = new VBox(10,
                new Label("Categories"),
                categoryList,
                new Label("Category name"),
                nameField,
                buttons,
                outputArea);
        root.setPadding(new Insets(16));
 
        refresh();
        return root;
    }
 
    // ── Refresh list ──────────────────────────────────────────────────────────
 
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
}