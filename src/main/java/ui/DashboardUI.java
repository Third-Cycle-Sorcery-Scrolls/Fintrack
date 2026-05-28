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
import model.Transaction;
import service.AnalyticsService;
import service.ProfileService;
import service.RecurringExpenseService;
import service.TransactionService;

import java.util.List;

public class DashboardUI extends BorderPane {
    private final AppContext appContext;
    private ProfileSetupUI profileSetupUI;
    private final CategoryUI categoryUI;
    private final RecurringExpenseUI recurringExpenseUI;
    private final AnalyticsService analyticsService;
    private final ProfileService profileService;
    private final RecurringExpenseService recurringExpenseService;
    private final TransactionService transactionService; // was missing

    public DashboardUI(AppContext appContext) {
        this.appContext = appContext;
        this.analyticsService        = appContext.getAnalyticsService();
        this.profileService          = appContext.getProfileService();
        this.recurringExpenseService = appContext.getRecurringExpenseService();
        this.transactionService      = appContext.getTransactionService(); // was missing

        this.profileSetupUI = new ProfileSetupUI(profileService, () -> {
            appContext.saveLastProfile();
            if (appContext.getProfileService().getActiveProfile().isEmpty()) {
                this.setCenter(profileSetupUI.buildView());
            } else {
                this.setCenter(createHomeView());
            }
        });
        this.categoryUI        = new CategoryUI(appContext.getCategoryService(), appContext.getProfileService());
        this.recurringExpenseUI = new RecurringExpenseUI(recurringExpenseService, profileService);
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

        Button btnHome       = createNavButton("📊 Dashboard");
        Button btnProfile    = createNavButton("👤 Profile Setup");
        Button btnTransaction = createNavButton("💳 Transactions");
        Button btnRecurring  = createNavButton("🔄 Recurring Expenses");
        Button btnCategory   = createNavButton("🗂 Categories");

        btnHome.setOnAction(e -> {if(requireActiveProfile()) this.setCenter(createHomeView());});
        btnProfile.setOnAction(e -> this.setCenter(profileSetupUI.buildView()));
        btnTransaction.setOnAction(e -> {
            TransactionUI transactionUI = new TransactionUI(
                    appContext.getTransactionService(), profileService, appContext.getCategoryService());
            if(requireActiveProfile()) this.setCenter(transactionUI.buildView());
        });
        btnCategory.setOnAction(e  -> {if(requireActiveProfile()) this.setCenter(categoryUI.buildView());});
        btnRecurring.setOnAction(e -> {if(requireActiveProfile()) this.setCenter(recurringExpenseUI.buildView());});

        sidebar.getChildren().addAll(appTitle, appSubtitle, spacer,
                btnHome, btnProfile, btnCategory, btnTransaction, btnRecurring);
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

            // Fetch real data — previously all three of these were hardcoded
            List<Transaction> transactions = transactionService.getTransactionsForProfile(profile.getId());
            int categoryCount              = recurringExpenseService.findCategoriesByProfileId(profile.getId()).size();
            int recurringExpenseCount      = recurringExpenseService.findByProfileId(profile.getId()).size();

            var summary = analyticsService.buildSummary(transactions, categoryCount, recurringExpenseCount);

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

    public void showProfileSetup() {
        this.setCenter(profileSetupUI.buildView());
    }
    private boolean requireActiveProfile() {
        if (profileService.getActiveProfile().isEmpty()) {
            this.setCenter(profileSetupUI.buildView());
            return false;
        }
        return true;
    }
}