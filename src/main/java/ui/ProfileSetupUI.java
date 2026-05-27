package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Currency;
import model.Profile;
import service.ProfileService;

import java.io.InputStream;

public class ProfileSetupUI {
    private final ProfileService profileService;
    private final Runnable profileChangedCallback;
    private final TextArea outputArea = new TextArea();

    public ProfileSetupUI(ProfileService profileService) {
        this(profileService, () -> {});
    }

    public ProfileSetupUI(ProfileService profileService, Runnable profileChangedCallback) {
        this.profileService = profileService;
        this.profileChangedCallback = profileChangedCallback;
    }

    public VBox buildView() {
        VBox root = new VBox(20);
        root.getStyleClass().add("primary-background");
        root.setPadding(new Insets(25));

        Label pageTitle = new Label("👤 Profile Setup");
        pageTitle.getStyleClass().add("page-title");

        // ── Existing profiles ─────────────────────────────────────────────────
        Label existingLabel = new Label("📋 Existing Profiles");
        existingLabel.getStyleClass().add("section-title");

        ComboBox<Profile> profileBox = new ComboBox<>();
        profileBox.setMaxWidth(Double.MAX_VALUE);
        refreshProfileList(profileBox);

        Button useProfileButton    = createButton("✓ Use Selected",    "button");
        Button editProfileButton   = createButton("✏ Edit Selected",   "button");
        Button deleteProfileButton = createButton("🗑 Delete Selected", "button-danger");

        useProfileButton.setOnAction(e -> {
            Profile selected = profileBox.getValue();
            if (selected == null) { showStatus("⚠️ Select a profile first.", false); return; }
            profileService.setActiveProfile(selected);
            showStatus("✅ Now using: " + selected.getName() + " (" + selected.getDefaultCurrency() + ")", true);
            profileChangedCallback.run();
        });

        editProfileButton.setOnAction(e -> {
            Profile selected = profileBox.getValue();
            if (selected == null) { showStatus("⚠️ Select a profile to edit.", false); return; }
            showEditDialog(selected, profileBox);
        });

        deleteProfileButton.setOnAction(e -> {
            Profile selected = profileBox.getValue();
            if (selected == null) { showStatus("⚠️ Select a profile to delete.", false); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete profile \"" + selected.getName() + "\"? This cannot be undone.",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText(null);
            applyIcon(confirm);
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    try {
                        profileService.deleteById(selected.getId());
                        refreshProfileList(profileBox);
                        refreshOutput(profileBox);
                        profileChangedCallback.run();
                    } catch (Exception ex) {
                        showStatus("❌ " + ex.getMessage(), false);
                    }
                }
            });
        });

        HBox actionButtons = new HBox(10, useProfileButton, editProfileButton, deleteProfileButton);

        VBox existingProfilesBox = new VBox(10, existingLabel, profileBox, actionButtons);
        existingProfilesBox.getStyleClass().add("card");

        // ── Create new profile ────────────────────────────────────────────────
        Label newProfileLabel = new Label("➕ Create New Profile");
        newProfileLabel.getStyleClass().add("section-title");

        TextField nameField = new TextField();
        nameField.setPromptText("Profile name");

        ComboBox<Currency> currencyBox = new ComboBox<>();
        currencyBox.getItems().setAll(Currency.values());
        currencyBox.setValue(Currency.ETB);
        currencyBox.setMaxWidth(Double.MAX_VALUE);

        Button saveButton = createButton("💾 Create Profile", "button");
        saveButton.setOnAction(e -> {
            try {
                Profile saved = profileService.createProfile(nameField.getText(), currencyBox.getValue());
                nameField.clear();
                refreshProfileList(profileBox);
                profileBox.setValue(saved);
                refreshOutput(profileBox);
                profileChangedCallback.run();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                showStatus("❌ " + ex.getMessage(), false);
            }
        });

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(12);
        formGrid.setPadding(new Insets(5));

        formGrid.add(new Label("Profile Name:"),     0, 0);
        formGrid.add(nameField,                      1, 0);
        formGrid.add(new Label("Default Currency:"), 0, 1);
        formGrid.add(currencyBox,                    1, 1);

        VBox createProfileBox = new VBox(10, newProfileLabel, formGrid, saveButton);
        createProfileBox.getStyleClass().add("card");

        // ── Status area ───────────────────────────────────────────────────────
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(3);
        refreshOutput(profileBox);

        Label statusLabel = new Label("📊 Status:");
        statusLabel.getStyleClass().add("section-subtitle");

        root.getChildren().addAll(pageTitle, existingProfilesBox, createProfileBox, statusLabel, outputArea);
        return root;
    }

    // ── Edit dialog ────────────────────────────────────────────────────────────

    private void showEditDialog(Profile profile, ComboBox<Profile> profileBox) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Profile");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        applyIcon(dialog);

        VBox content = new VBox(15);
        content.getStyleClass().add("primary-background");
        content.setPadding(new Insets(20));

        Label title = new Label("✏ Edit Profile");
        title.getStyleClass().add("section-title");

        TextField nameField = new TextField(profile.getName());

        ComboBox<Currency> currencyBox = new ComboBox<>();
        currencyBox.getItems().setAll(Currency.values());
        currencyBox.setValue(profile.getDefaultCurrency());
        currencyBox.setMaxWidth(Double.MAX_VALUE);

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);
        form.setPadding(new Insets(10));
        form.getStyleClass().add("card");

        form.add(new Label("Profile Name:"),     0, 0); form.add(nameField,   1, 0);
        form.add(new Label("Default Currency:"), 0, 1); form.add(currencyBox, 1, 1);

        Label feedback = new Label(" ");
        feedback.setMinHeight(20);
        feedback.setStyle("-fx-text-fill: #a83232;"); // danger red — no CSS class for this one

        Button saveBtn   = createButton("💾 Save", "button");
        Button cancelBtn = createButton("Cancel",  "button");
        HBox buttons = new HBox(10, saveBtn, cancelBtn);
        buttons.setPadding(new Insets(5, 0, 0, 0));

        saveBtn.setOnAction(e -> {
            try {
                profileService.updateProfile(profile.getId(), nameField.getText(), currencyBox.getValue());
                refreshProfileList(profileBox);
                profileService.findById(profile.getId()).ifPresent(profileBox::setValue);
                refreshOutput(profileBox);
                profileChangedCallback.run();
                dialog.close();
            } catch (Exception ex) {
                feedback.setText("❌ " + ex.getMessage());
            }
        });
        cancelBtn.setOnAction(e -> dialog.close());

        content.getChildren().addAll(title, form, feedback, buttons);

        javafx.scene.Scene dialogScene = new javafx.scene.Scene(content, 400, 280);
        // Apply the same stylesheet so CSS classes work inside the dialog too
        javafx.scene.Scene parentScene = outputArea.getScene();
        if (parentScene != null) {
            dialogScene.getStylesheets().addAll(parentScene.getStylesheets());
        }
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void applyIcon(Object target) {
        InputStream iconStream = getClass().getResourceAsStream("/app-icon.png");
        if (iconStream == null) return;
        Image icon = new Image(iconStream);
        if (target instanceof Stage s) {
            s.getIcons().add(icon);
        } else if (target instanceof Alert a) {
            Stage alertStage = (Stage) a.getDialogPane().getScene().getWindow();
            alertStage.getIcons().add(icon);
        }
    }

    private void showStatus(String message, boolean success) {
        // Only the success state overrides the textarea background; error uses the CSS default
        outputArea.setStyle(success ? "-fx-control-inner-background: #1e3a1e;" : "");
        outputArea.setText(message);
    }

    private Button createButton(String text, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        return btn;
    }

    private void refreshProfileList(ComboBox<Profile> profileBox) {
        try {
            Profile current = profileBox.getValue();
            profileBox.getItems().setAll(profileService.findAll());
            if (current != null) {
                profileBox.getItems().stream()
                        .filter(p -> p.getId().equals(current.getId()))
                        .findFirst()
                        .ifPresentOrElse(profileBox::setValue,
                                () -> profileService.getActiveProfile().ifPresent(profileBox::setValue));
            } else {
                profileService.getActiveProfile().ifPresent(profileBox::setValue);
            }
        } catch (IllegalStateException ex) {
            showStatus("❌ " + ex.getMessage(), false);
        }
    }

    private void refreshOutput(ComboBox<Profile> profileBox) {
        String activeText = profileService.getActiveProfile()
                .map(p -> "✅ Active profile: " + p.getName() + " (" + p.getDefaultCurrency() + ")")
                .orElse("⚠️ No active profile selected.");
        showStatus(activeText, profileService.getActiveProfile().isPresent());
        refreshProfileList(profileBox);
    }
}