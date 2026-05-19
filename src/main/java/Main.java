
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;
import service.AnalyticsService;
import ui.DashboardUI;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        DashboardUI dashboardUI = new DashboardUI(new AnalyticsService());
        Scene scene = new Scene(dashboardUI.buildView(), 900, 600);

        InputStream iconStream = getClass().getResourceAsStream("/app-icon.png");
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        }

        primaryStage.setTitle("Fintrack");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
