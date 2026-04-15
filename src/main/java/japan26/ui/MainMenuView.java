package japan26.ui;

import japan26.engine.SceneManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * The title / main menu screen shown when the app launches and after the story ends.
 */
public class MainMenuView extends StackPane {

    public MainMenuView() {
        getStyleClass().add("main-menu");

        Label title    = new Label("Japan 26");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("A Story-Driven Journey");
        subtitle.getStyleClass().add("subtitle-label");

        Button startBtn = new Button("Begin Journey");
        startBtn.getStyleClass().add("menu-button");
        startBtn.setOnAction(e -> SceneManager.startGame());

        Button quitBtn  = new Button("Quit");
        quitBtn.getStyleClass().add("menu-button");
        quitBtn.setOnAction(e -> System.exit(0));

        VBox menu = new VBox(20, title, subtitle, startBtn, quitBtn);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(40));

        getChildren().add(menu);
        setAlignment(menu, Pos.CENTER);

        // Load the shared stylesheet
        var css = getClass().getResource("/japan26/css/style.css");
        if (css != null) getStylesheets().add(css.toExternalForm());
    }
}
