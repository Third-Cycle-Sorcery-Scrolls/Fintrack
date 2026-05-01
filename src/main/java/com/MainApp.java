package com;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApp extends Application {
    
    private int clickCount = 0;
    private Label counterLabel;
    
    @Override
    public void start(Stage primaryStage) {
        // Title label
        Label titleLabel = new Label("Welcome to My JavaFX App");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Counter label
        counterLabel = new Label("Click Count: 0");
        counterLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
        
        // Button to increment counter
        Button clickButton = new Button("Click Me!");
        clickButton.setStyle("-fx-font-size: 16px; -fx-background-color: #3498db; -fx-text-fill: white;");
        clickButton.setOnAction(e -> {
            clickCount++;
            counterLabel.setText("Click Count: " + clickCount);
        });
        
        // Text field and button for custom text
        TextField textField = new TextField();
        textField.setPromptText("Enter custom message");
        textField.setPrefWidth(200);
        
        Button setTextButton = new Button("Update Label");
        setTextButton.setStyle("-fx-font-size: 14px;");
        setTextButton.setOnAction(e -> {
            String newText = textField.getText();
            if (!newText.isEmpty()) {
                counterLabel.setText(newText);
            }
        });
        
        // Reset button
        Button resetButton = new Button("Reset Counter");
        resetButton.setStyle("-fx-font-size: 14px; -fx-background-color: #e74c3c; -fx-text-fill: white;");
        resetButton.setOnAction(e -> {
            clickCount = 0;
            counterLabel.setText("Click Count: 0");
            textField.clear();
        });
        
        // Layout setup
        HBox inputBox = new HBox(10, textField, setTextButton, resetButton);
        inputBox.setAlignment(Pos.CENTER);
        
        VBox root = new VBox(20, titleLabel, counterLabel, clickButton, inputBox);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #ecf0f1;");
        
        // Scene and stage
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setTitle("My JavaFX Desktop App");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}