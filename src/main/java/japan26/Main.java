package japan26;

import japan26.engine.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point.  JavaFX calls start() on the JavaFX Application Thread.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        SceneManager.init(stage);
        SceneManager.showMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
