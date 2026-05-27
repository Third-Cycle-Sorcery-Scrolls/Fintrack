package main;

import app.AppContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import remote.CalculatorServer;
import ui.DashboardUI;

import java.io.InputStream;

public class Main extends Application {
    private final AppContext appContext = new AppContext();

    @Override
    public void start(Stage primaryStage) {
        CalculatorServer.startServerInBackground();

        DashboardUI dashboardUI = new DashboardUI(appContext);
        Scene scene = new Scene(dashboardUI, 1100, 720);

        String css = getClass().getResource("/theme.css").toExternalForm();
        scene.getStylesheets().add(css);

        InputStream iconStream = getClass().getResourceAsStream("/app-icon.png");
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        }

        primaryStage.setTitle("Fintrack");
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> appContext.shutdown());
        primaryStage.show();

        appContext.restoreLastProfile();
        dashboardUI.refreshHomeView(); // Ensure home view reflects the restored profile immediately
    }

    public static void main(String[] args) {
        launch(args);
    }
}