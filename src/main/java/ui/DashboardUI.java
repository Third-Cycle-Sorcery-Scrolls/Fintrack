package ui;

import app.AppContext;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Profile;
import service.AnalyticsService;
import service.ProfileService;
import service.RecurringExpenseService;
import ui.controllers.TagController;

public class DashboardUI extends BorderPane {
    private final AppContext appContext;
    private final ProfileSetupUI profileSetupUI;
    private final CategoryUI categoryUI;
    private final RecurringExpenseUI recurringExpenseUI;
    private final TagController tagController;
    private final AnalyticsService analyticsService;
    private final ProfileService profileService;
    private final RecurringExpenseService recurringExpenseService;

    public DashboardUI(AppContext appContext) {
        this.appContext = appContext;
        this.analyticsService = appContext.getAnalyticsService();
        this.profileService = appContext.getProfileService();
        this.recurringExpenseService = appContext.getRecurringExpenseService();

        this.profileSetupUI = new ProfileSetupUI(profileService, () -> {
            appContext.saveLastProfile();
            this.setCenter(createHomeView());
        });
        this.categoryUI = new CategoryUI(appContext.getCategoryService(), appContext.getProfileService());
        this.recurringExpenseUI = new RecurringExpenseUI(recurringExpenseService, profileService);
        this.tagController = new TagController(appContext.getTagService(), profileService);
        buildView();
    }

    private void buildView() {
        this.setLeft(createSidebar());
        this.setCenter(createHomeView());
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(12);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(200);

        Label appTitle = new Label("Fintrack");
        appTitle.getStyleClass().add("sidebar-title");

        Label appSubtitle = new Label("Financial Manager");
        appSubtitle.getStyleClass().add("sidebar-subtitle");

        VBox spacer = new VBox();
        spacer.setPrefHeight(15);

        Button btnHome        = createNavButton("📊 Dashboard");
        Button btnProfile     = createNavButton("👤 Profile Setup");
        Button btnCategory    = createNavButton("🗂 Categories");
        Button btnTransaction = createNavButton("💳 Transactions");
        Button btnRecurring   = createNavButton("🔄 Recurring Expenses");
        Button btnTag         = createNavButton("🏷 Tag Management");

        btnHome.setOnAction(e        -> this.setCenter(createHomeView()));
        btnProfile.setOnAction(e     -> this.setCenter(profileSetupUI.buildView()));
        btnCategory.setOnAction(e    -> this.setCenter(categoryUI.buildView()));
        btnTransaction.setOnAction(e -> {
            TransactionUI transactionUI = new TransactionUI(
                appContext.getTransactionService(), profileService,
                appContext.getCategoryService(), appContext.getTagService());
            this.setCenter(transactionUI.buildView());
        });
        btnRecurring.setOnAction(e   -> this.setCenter(recurringExpenseUI.buildView()));
        btnTag.setOnAction(e         -> this.setCenter(tagController.buildView()));

        sidebar.getChildren().addAll(appTitle, appSubtitle, spacer,
            btnHome, btnProfile, btnCategory, btnTransaction, btnRecurring, btnTag);
        return sidebar;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-button");
        return btn;
    }

    public VBox createHomeView() {
        VBox homeView = new VBox(20);
        homeView.getStyleClass().add("primary-background");
        homeView.setPadding(new Insets(30, 40, 30, 40));

        Label title = new Label("Financial Overview");
        title.getStyleClass().add("page-title");

        homeView.getChildren().addAll(title, createSummaryCards());
        return homeView;
    }

    private VBox createSummaryCards() {
        VBox container = new VBox(15);

        try {
            Profile profile = profileService.requireActiveProfile();

            int categoryCount         = recurringExpenseService.findCategoriesByProfileId(profile.getId()).size();
            int recurringExpenseCount = recurringExpenseService.findByProfileId(profile.getId()).size();
            String netBalance         = "0.00";

            var summary = analyticsService.buildSummary(0, categoryCount, recurringExpenseCount, netBalance);

            GridPane statsGrid = new GridPane();
            statsGrid.setHgap(20);
            statsGrid.setVgap(20);

            int col = 0, row = 0;
            for (var entry : summary.entrySet()) {
                statsGrid.add(createStatCard(entry.getKey(), entry.getValue()), col, row);
                if (++col > 2) { col = 0; row++; }
            }

            HBox profileInfo = new HBox(15);
            profileInfo.getStyleClass().add("card");

            Label profileLabel = new Label("Active Profile: " + profile.getName());
            profileLabel.getStyleClass().add("section-title");

            Label currencyLabel = new Label("Currency: " + profile.getDefaultCurrency());
            currencyLabel.getStyleClass().add("section-subtitle");

            profileInfo.getChildren().addAll(profileLabel, currencyLabel);
            container.getChildren().addAll(statsGrid, profileInfo);

        } catch (IllegalStateException ex) {
            Label noProfile = new Label("⚠️ No active profile. Go to Profile Setup to create or select one.");
            noProfile.getStyleClass().add("section-subtitle");
            container.getChildren().add(noProfile);
        }

        return container;
    }

    private HBox createStatCard(String label, String value) {
        HBox card = new HBox(10);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(200);

        VBox content = new VBox(5);

        Label labelText = new Label(label);
        labelText.getStyleClass().add("stat-label");

        Label valueText = new Label(value);
        valueText.getStyleClass().add("stat-value");

        content.getChildren().addAll(labelText, valueText);
        card.getChildren().add(content);
        return card;
    }

    public void refreshHomeView() {
        this.setCenter(createHomeView());
    }
}
