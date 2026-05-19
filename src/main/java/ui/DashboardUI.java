package ui;

import config.DBConnection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import service.AnalyticsService;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;

public class DashboardUI {
    private final AnalyticsService analyticsService;

    public DashboardUI(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    public BorderPane buildView() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ecf0f1;");
        root.setLeft(buildSidebar());
        root.setCenter(buildMainContent());
        return root;
    }

    private VBox buildSidebar() {
        Button profileBtn = menuButton("Profile Setup");
        Button categoryBtn = menuButton("Manage Categories");
        Button transactionBtn = menuButton("Transactions");
        Button tagBtn = menuButton("Manage Tags");
        Button recurringBtn = menuButton("Recurring Expenses");

        profileBtn.setOnAction(e -> System.out.println("Open Profile UI"));
        categoryBtn.setOnAction(e -> System.out.println("Open Category UI"));
        transactionBtn.setOnAction(e -> System.out.println("Open Transaction UI"));
        tagBtn.setOnAction(e -> System.out.println("Open Tag UI"));
        recurringBtn.setOnAction(e -> System.out.println("Open Recurring UI"));

        VBox sideMenu = new VBox(10,
                buildSidebarIcon(),
                profileBtn,
                categoryBtn,
                transactionBtn,
                tagBtn,
                recurringBtn
        );
        sideMenu.setPadding(new Insets(10));
        sideMenu.setStyle("-fx-background-color: #34495e; -fx-pref-width: 210;");
        sideMenu.setFillWidth(true);
        return sideMenu;
    }

    private VBox buildMainContent() {
        Label title = new Label("Fintrack Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label dbStatus = new Label(getDatabaseStatus());

        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(20);
        summaryGrid.setVgap(10);

        int row = 0;
        for (Map.Entry<String, String> entry : analyticsService.buildSummary().entrySet()) {
            Label key = new Label(entry.getKey() + ":");
            key.setStyle("-fx-font-weight: bold;");
            Label value = new Label(entry.getValue());
            summaryGrid.addRow(row++, key, value);
        }

        Label ownerNote = new Label("Feature-specific UIs are implemented by teammates. Dashboard provides integration/navigation only.");
        ownerNote.setWrapText(true);

        HBox moduleSlots = new HBox(10,
                moduleTag("Profile UI"),
                moduleTag("Category UI"),
                moduleTag("Transaction UI"),
                moduleTag("Tag UI"),
                moduleTag("Recurring UI")
        );

        VBox mainContent = new VBox(14, title, dbStatus, summaryGrid, ownerNote, moduleSlots);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_LEFT);
        return mainContent;
    }

    private HBox buildSidebarIcon() {
        InputStream iconStream = getClass().getResourceAsStream("/app-icon.png");
        if (iconStream == null) {
            Label fallbackIcon = new Label("📊");
            fallbackIcon.setStyle("-fx-font-size: 48px;");
            HBox box = new HBox(fallbackIcon);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(6, 0, 10, 0));
            return box;
        }

        ImageView logoView = new ImageView(new Image(iconStream));
        logoView.setFitWidth(64);
        logoView.setFitHeight(64);
        logoView.setPreserveRatio(true);

        HBox box = new HBox(logoView);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(6, 0, 10, 0));
        return box;
    }

    private Button menuButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(button, Priority.NEVER);
        return button;
    }

    private Label moduleTag(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-padding: 6 10; -fx-background-color: #e8edf5; -fx-background-radius: 8;");
        return label;
    }

    private String getDatabaseStatus() {
        try {
            DBConnection.getConnection();
            return "Database: Connected";
        } catch (SQLException e) {
            return "Database: Not connected (" + e.getMessage() + ")";
        }
    }
}
