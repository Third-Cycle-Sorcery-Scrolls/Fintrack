package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import model.Currency;
import model.Profile;
import service.ProfileService;

public class ProfileSetupUI {
    private final ProfileService profileService;
    private final Runnable profileChangedCallback;
    private final TextArea outputArea = new TextArea();

    public ProfileSetupUI(ProfileService profileService) {
        this(profileService, () -> { });
    }

    public ProfileSetupUI(ProfileService profileService, Runnable profileChangedCallback) {
        this.profileService = profileService;
        this.profileChangedCallback = profileChangedCallback;
    }

    public VBox buildView() {
        ComboBox<Profile> profileBox = new ComboBox<>();
        profileBox.setMaxWidth(Double.MAX_VALUE);
        refreshProfileList(profileBox);

        Button useProfileButton = new Button("Use Selected Profile");
        useProfileButton.setOnAction(event -> {
            Profile selectedProfile = profileBox.getValue();
            if (selectedProfile == null) {
                outputArea.setText("Select a profile or create a new one.");
                return;
            }
            profileService.setActiveProfile(selectedProfile);
            refreshOutput(profileBox);
            profileChangedCallback.run();
        });

        TextField nameField = new TextField();
        nameField.setPromptText("Profile name");

        ComboBox<Currency> currencyBox = new ComboBox<>();
        currencyBox.getItems().setAll(Currency.values());
        currencyBox.setValue(Currency.ETB);
        currencyBox.setMaxWidth(Double.MAX_VALUE);

        Button saveButton = new Button("Create Profile");
        saveButton.setOnAction(event -> {
            try {
                Profile savedProfile = profileService.createProfile(nameField.getText(), currencyBox.getValue());
                nameField.clear();
                refreshProfileList(profileBox);
                profileBox.setValue(savedProfile);
                refreshOutput(profileBox);
                profileChangedCallback.run();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                outputArea.setText(ex.getMessage());
            }
        });

        outputArea.setEditable(false);
        refreshOutput(profileBox);

        VBox root = new VBox(10,
                new Label("Existing profiles"),
                profileBox,
                useProfileButton,
                new Label("New profile"),
                nameField,
                new Label("Default currency"),
                currencyBox,
                saveButton,
                outputArea);
        root.setPadding(new Insets(16));
        return root;
    }

    private void refreshProfileList(ComboBox<Profile> profileBox) {
        try {
            profileBox.getItems().setAll(profileService.findAll());
            profileService.getActiveProfile().ifPresent(profileBox::setValue);
        } catch (IllegalStateException ex) {
            outputArea.setText(ex.getMessage());
        }
    }

    private void refreshOutput(ComboBox<Profile> profileBox) {
        String activeProfileText = profileService.getActiveProfile()
                .map(profile -> "Active profile: " + profile.getName() + " (" + profile.getDefaultCurrency() + ")")
                .orElse("No active profile selected.");
        outputArea.setText(activeProfileText);
        refreshProfileList(profileBox);
    }
}