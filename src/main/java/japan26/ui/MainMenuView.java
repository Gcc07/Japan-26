package japan26.ui;

import japan26.engine.SceneManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Title / main menu screen.
 */
public class MainMenuView extends StackPane {

    public MainMenuView() {
        // ── Layer 1: Background photo ──────────────────────────────────────
        ImageView bg = new ImageView();
        bg.setFitWidth(1280);
        bg.setFitHeight(720);
        bg.setPreserveRatio(false);
        var bgUrl = getClass().getResource("/japan26/images/OutsideCherry.jpg");
        if (bgUrl != null) bg.setImage(new Image(bgUrl.toExternalForm()));

        // ── Layer 2: Dark gradient overlay so text is readable ─────────────
        Rectangle overlay = new Rectangle(1280, 720,
            new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0,  Color.color(0, 0, 0, 0.62)),
                new Stop(0.45, Color.color(0, 0, 0, 0.42)),
                new Stop(1.0,  Color.color(0, 0, 0, 0.68))
            )
        );

        // ── Layer 3: Cherry blossom animation ──────────────────────────────
        CherryBlossomAnimation blossoms = new CherryBlossomAnimation();

        // ── Layer 4: Content ───────────────────────────────────────────────
        Text titleMain = new Text("JAPAN ");
        titleMain.setFont(Font.font("Segoe UI", FontWeight.BOLD, 72));
        titleMain.setFill(Color.web("#f0cd70"));
        titleMain.setEffect(new DropShadow(28, Color.web("#f0cd70")));

        Text title26 = new Text("26");
        title26.setFont(Font.font("Segoe UI", FontWeight.BOLD, 72));
        title26.setFill(Color.web("#ffb7c5"));
        title26.setEffect(new DropShadow(28, Color.web("#ffb7c5")));

        TextFlow title = new TextFlow(titleMain, title26);
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label subtitle = new Label("A Story-Driven Journey  ·  Summer 2026");
        subtitle.getStyleClass().add("subtitle-label");

        // Thin decorative divider
        Rectangle divider = new Rectangle(200, 1);
        divider.setFill(Color.color(0.88, 0.72, 0.38, 0.65));

        Region spacer = new Region();
        spacer.setMinHeight(8);

        Button startBtn = new Button("Begin Journey");
        startBtn.getStyleClass().add("menu-button");
        startBtn.setOnAction(e -> SceneManager.startGame());

        Button testBtn = new Button("Test Story");
        testBtn.getStyleClass().add("menu-button-secondary");
        testBtn.setOnAction(e -> SceneManager.startTestStory());

        Button quitBtn = new Button("Quit");
        quitBtn.getStyleClass().add("menu-button");
        quitBtn.setOnAction(e -> System.exit(0));

        VBox content = new VBox(16, title, subtitle, divider, spacer, startBtn, testBtn, quitBtn);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        getChildren().addAll(bg, overlay, blossoms, content);
        setAlignment(content, Pos.CENTER);

        // ── Fade-in on load ────────────────────────────────────────────────
        content.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(900), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.millis(150));
        fadeIn.play();

        // ── Stylesheet ─────────────────────────────────────────────────────
        var css = getClass().getResource("/japan26/css/style.css");
        if (css != null) getStylesheets().add(css.toExternalForm());
    }
}
