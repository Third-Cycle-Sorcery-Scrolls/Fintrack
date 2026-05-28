package ui.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Tag;
import model.Profile;
import service.ProfileService;
import service.TagService;

import java.util.List;

/**
 * UI controller for Tag Management.
 * Follows the same programmatic JavaFX pattern as CategoryUI.
 */
public class TagController {

    private final TagService tagService;
    private final ProfileService profileService;

    private final ListView<Tag> tagList = new ListView<>();
    @FXML
    private final TextArea outputArea = new TextArea();

    public TagController(TagService tagService, ProfileService profileService) {
        this.tagService = tagService;
        this.profileService = profileService;
    }

    public VBox buildView() {
        // ── Input fields ──────────────────────────────────────────────────────
        TextField nameField = new TextField();
        nameField.setPromptText("Tag name");
        nameField.getStyleClass().add("tag-input");

        TextField searchField = new TextField();
        searchField.setPromptText("Search tags...");
        searchField.getStyleClass().add("tag-input");

        // ── Transaction-tag assignment section ────────────────────────────────
        TextField transactionIdField = new TextField();
        transactionIdField.setPromptText("Transaction ID");
        transactionIdField.getStyleClass().add("tag-input");

        // ── Buttons ───────────────────────────────────────────────────────────
        Button addButton    = new Button("Add");
        Button updateButton = new Button("Update");
        Button deleteButton = new Button("Delete");
        Button refreshButton = new Button("Refresh");
        Button assignButton = new Button("Assign to Transaction");
        Button removeButton = new Button("Remove from Transaction");

        addButton.getStyleClass().add("tag-btn-primary");
        updateButton.getStyleClass().add("tag-btn-secondary");
        deleteButton.getStyleClass().add("tag-btn-danger");
        refreshButton.getStyleClass().add("tag-btn-secondary");
        assignButton.getStyleClass().add("tag-btn-primary");
        removeButton.getStyleClass().add("tag-btn-danger");

        // ── Search filter ─────────────────────────────────────────────────────
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterList(newVal));

        // ── Button actions ────────────────────────────────────────────────────
        refreshButton.setOnAction(e -> refresh());

        addButton.setOnAction(e -> {
            try {
                Profile profile = profileService.requireActiveProfile();
                Tag saved = tagService.createTag(profile.getId(), nameField.getText());
                nameField.clear();
                refresh();
                output("Tag \"" + saved.getName() + "\" created successfully.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                output(ex.getMessage());
            } catch (RuntimeException ex) {
                output("Database error: " + ex.getMessage());
            }
        });

        updateButton.setOnAction(e -> {
            try {
                Tag selected = requireSelected();
                Profile profile = profileService.requireActiveProfile();
                Tag updated = tagService.updateTag(selected.getId(), profile.getId(), nameField.getText());
                nameField.clear();
                refresh();
                output("Tag renamed to \"" + updated.getName() + "\" successfully.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                output(ex.getMessage());
            } catch (RuntimeException ex) {
                output("Database error: " + ex.getMessage());
            }
        });

        deleteButton.setOnAction(e -> {
            try {
                Tag selected = requireSelected();
                Profile profile = profileService.requireActiveProfile();
                tagService.deleteTag(selected.getId(), profile.getId());
                refresh();
                output("Tag \"" + selected.getName() + "\" deleted successfully.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                output(ex.getMessage());
            } catch (RuntimeException ex) {
                output("Database error: " + ex.getMessage());
            }
        });

        assignButton.setOnAction(e -> {
            try {
                Tag selected = requireSelected();
                Profile profile = profileService.requireActiveProfile();
                int txId = parseTransactionId(transactionIdField.getText());
                tagService.assignTag(txId, selected.getId(), profile.getId());
                output("Tag \"" + selected.getName() + "\" assigned to transaction #" + txId + ".");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                output(ex.getMessage());
            } catch (RuntimeException ex) {
                output("Database error: " + ex.getMessage());
            }
        });

        removeButton.setOnAction(e -> {
            try {
                Tag selected = requireSelected();
                int txId = parseTransactionId(transactionIdField.getText());
                tagService.removeTag(txId, selected.getId());
                output("Tag \"" + selected.getName() + "\" removed from transaction #" + txId + ".");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                output(ex.getMessage());
            } catch (RuntimeException ex) {
                output("Database error: " + ex.getMessage());
            }
        });

        // ── Output area ───────────────────────────────────────────────────────
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(3);
        outputArea.getStyleClass().add("tag-output");

        // ── Layout ────────────────────────────────────────────────────────────
        Label pageTitle = new Label("Tag Management");
        pageTitle.getStyleClass().add("tag-page-title");

        HBox tagButtons = new HBox(8, addButton, updateButton, deleteButton, refreshButton);

        VBox tagCard = new VBox(10,
            new Label("Tags"),
            searchField,
            tagList,
            new Label("Tag name"),
            nameField,
            tagButtons
        );
        tagCard.getStyleClass().add("tag-card");
        tagCard.setPadding(new Insets(16));

        HBox assignButtons = new HBox(8, assignButton, removeButton);

        VBox assignCard = new VBox(10,
            new Label("Assign / Remove Tag from Transaction"),
            new Label("Transaction ID"),
            transactionIdField,
            assignButtons
        );
        assignCard.getStyleClass().add("tag-card");
        assignCard.setPadding(new Insets(16));

        VBox root = new VBox(16, pageTitle, tagCard, assignCard, outputArea);
        root.setPadding(new Insets(24));
        root.getStyleClass().add("tag-root");

        refresh();
        return root;
    }

    // ── Refresh list ──────────────────────────────────────────────────────────

    private void refresh() {
        try {
            Profile profile = profileService.requireActiveProfile();
            tagList.getItems().setAll(tagService.getAllTagsForProfile(profile.getId()));
        } catch (IllegalStateException ex) {
            tagList.getItems().clear();
            output(ex.getMessage());
        } catch (RuntimeException ex) {
            tagList.getItems().clear();
            output("Failed to load tags: " + ex.getMessage());
        }
    }

    // ── Filter list by search text ────────────────────────────────────────────

    private void filterList(String query) {
        try {
            Profile profile = profileService.requireActiveProfile();
            List<Tag> all = tagService.getAllTagsForProfile(profile.getId());
            if (query == null || query.isBlank()) {
                tagList.getItems().setAll(all);
            } else {
                String lower = query.toLowerCase();
                tagList.getItems().setAll(
                    all.stream().filter(t -> t.getName().toLowerCase().contains(lower)).toList()
                );
            }
        } catch (RuntimeException ex) {
            output("Filter error: " + ex.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Tag requireSelected() {
        Tag selected = tagList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Select a tag from the list first.");
        }
        return selected;
    }

    private int parseTransactionId(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Transaction ID must be a valid number.");
        }
    }

    private void output(String message) {
        outputArea.setText(message);
    }
}
