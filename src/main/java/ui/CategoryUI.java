package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Category;
import service.CategoryService;

import java.util.List;

public class CategoryUI {

    private final CategoryService categoryService;
    private TableView<Category> tableView;
    private int currentProfileId;

    public CategoryUI() {
        this.categoryService = new CategoryService();
    }

    public CategoryUI(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void show(Stage owner, int profileId) {
        this.currentProfileId = profileId;

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Category Management");
        stage.setMinWidth(600);
        stage.setMinHeight(500);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f8;");
        root.setTop(buildHeader());
        root.setCenter(buildTableSection());
        root.setBottom(buildButtonBar(stage));

        Scene scene = new Scene(root, 620, 520);
        stage.setScene(scene);
        stage.show();
        refreshTable();
    }

    private VBox buildHeader() {
        Label title = new Label("Category Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Add, rename, or delete your spending categories");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.web("#dce3ea"));

        VBox header = new VBox(4, title, subtitle);
        header.setPadding(new Insets(20, 24, 20, 24));
        header.setStyle("-fx-background-color: #2c3e50;");
        return header;
    }

    private VBox buildTableSection() {
        TableColumn<Category, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Category, String> nameCol = new TableColumn<>("Category Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(400);

        tableView = new TableView<>();
        tableView.getColumns().addAll(idCol, nameCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("No categories yet. Click 'Add' to create one."));
        tableView.setStyle("-fx-background-color: white; -fx-border-color: #dde3ea; -fx-border-radius: 6;");

        VBox section = new VBox(tableView);
        section.setPadding(new Insets(16, 24, 8, 24));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return section;
    }

    private HBox buildButtonBar(Stage stage) {
        Button addBtn    = styledButton("+ Add",    "#27ae60", "#219150");
        Button renameBtn = styledButton("✎ Rename", "#2980b9", "#2471a3");
        Button deleteBtn = styledButton("✕ Delete", "#e74c3c", "#c0392b");
        Button closeBtn  = styledButton("Close",    "#7f8c8d", "#717d7e");

        addBtn.setOnAction(e -> handleAdd());
        renameBtn.setOnAction(e -> handleRename());
        deleteBtn.setOnAction(e -> handleDelete());
        closeBtn.setOnAction(e -> stage.close());

        HBox bar = new HBox(10, addBtn, renameBtn, deleteBtn, closeBtn);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(12, 24, 20, 24));
        bar.setStyle("-fx-background-color: #f4f6f8; -fx-border-color: #dde3ea; -fx-border-width: 1 0 0 0;");
        return bar;
    }

    private void handleAdd() {
        String name = showInputDialog("Add Category", "Enter category name:", "");
        if (name == null) return;

        Category saved = categoryService.addCategory(currentProfileId, name);
        if (saved != null) {
            refreshTable();
            showInfo("Category \"" + saved.getName() + "\" added successfully.");
        } else {
            showError("Could not add category.\nThe name may already exist or was left blank.");
        }
    }

    private void handleRename() {
        Category selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a category from the list first.");
            return;
        }

        String newName = showInputDialog(
            "Rename Category",
            "Enter new name for \"" + selected.getName() + "\":",
            selected.getName()
        );
        if (newName == null) return;

        Category updated = categoryService.renameCategory(selected.getId(), currentProfileId, newName);
        if (updated != null) {
            refreshTable();
            showInfo("Category renamed to \"" + updated.getName() + "\" successfully.");
        } else {
            showError("Could not rename category.\nThe name may already exist or was left blank.");
        }
    }

    private void handleDelete() {
        Category selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a category from the list first.");
            return;
        }

        boolean confirmed = showConfirm(
            "Delete Category",
            "Are you sure you want to delete \"" + selected.getName() + "\"?\nThis cannot be undone."
        );
        if (!confirmed) return;

        boolean deleted = categoryService.deleteCategory(selected.getId(), currentProfileId);
        if (deleted) {
            refreshTable();
            showInfo("Category deleted successfully.");
        } else {
            showError("Could not delete category.\nIt may still be used by existing transactions.");
        }
    }

    private void refreshTable() {
        List<Category> categories = categoryService.getCategoriesForProfile(currentProfileId);
        tableView.getItems().setAll(categories);
    }

    private String showInputDialog(String title, String prompt, String defaultValue) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        dialog.setResizable(false);

        Label promptLabel = new Label(prompt);
        promptLabel.setFont(Font.font("System", 13));

        TextField textField = new TextField(defaultValue);
        textField.setPrefWidth(300);
        textField.setStyle("-fx-font-size: 13; -fx-padding: 6 10 6 10;");

        final String[] result = {null};

        Button okBtn     = styledButton("OK",     "#2980b9", "#2471a3");
        Button cancelBtn = styledButton("Cancel", "#7f8c8d", "#717d7e");

        okBtn.setOnAction(e -> { result[0] = textField.getText().trim(); dialog.close(); });
        cancelBtn.setOnAction(e -> dialog.close());
        textField.setOnAction(e -> { result[0] = textField.getText().trim(); dialog.close(); });

        HBox buttons = new HBox(10, okBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(14, promptLabel, textField, buttons);
        layout.setPadding(new Insets(24));
        layout.setStyle("-fx-background-color: #f4f6f8;");

        dialog.setScene(new Scene(layout));
        dialog.showAndWait();
        return result[0];
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    private Button styledButton(String text, String color, String hoverColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font("System", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setPadding(new Insets(8, 18, 8, 18));
        btn.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + hoverColor + "; -fx-background-radius: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6; -fx-cursor: hand;"));
        return btn;
    }
}