package jfx;

import java.util.concurrent.TimeUnit;


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Created by starksm on 6/11/16.
 */
public class TestHintAnimation extends Application {
    @FXML
    private Canvas canvas;
    @FXML
    private Canvas hitCanvas;
    @FXML
    private Canvas hintCanvas;
    private PushHintTimer hintTimer;
    private Image redTarget;
    private Image yellowTarget;
    private Image greenTarget;
    private Image rhiotTagImage;
    private Image leftArrow;
    private Image rightArrow;

    @Override
    public void start(Stage primaryStage) throws Exception {
        initUI(primaryStage);
    }

    private void initUI(Stage stage) {

        StackPane root = new StackPane();
        canvas = new Canvas(800,800);
        hitCanvas = new Canvas(800,800);
        hintCanvas = new Canvas(800,800);
        GraphicsContext hintGC = hintCanvas.getGraphicsContext2D();
        hintGC.setFont(Font.font("Courier", 18));
        hintGC.setFill(Color.BLUE);
        root.getChildren().add(canvas);
        root.getChildren().add(hitCanvas);
        root.getChildren().add(hintCanvas);

        redTarget = new Image("/target-red_720.png");
        yellowTarget = new Image("/target-yellow_720.png");
        greenTarget = new Image("/target-green_720.png");
        rhiotTagImage = new Image("/rhiottag.png");
        leftArrow = new Image("/leftArrow.png");
        rightArrow = new Image("/rightArrow.png");

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setGlobalAlpha(0.5);
        gc.drawImage(greenTarget, 0, 0);
        System.out.printf("Set target to green\n");

        hintTimer = new PushHintTimer();
        hintTimer.start();

        Scene scene = new Scene(root, 820, 820);
        stage.setTitle("ArrowTimer");
        stage.setScene(scene);
        stage.show();
        scene.setOnKeyTyped(event -> handleKey(event));
    }

    private void handleKey(KeyEvent key){
        if(key.getCharacter().equals(".")) {
            System.out.printf("Stopping hint\n");
            hintTimer.stop();
            GraphicsContext gc = hintCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, hintCanvas.getWidth(), hintCanvas.getWidth());
        } else if(key.getCharacter().equals("l")) {
            hintTimer.setType(HintButtonType.LEFT);
        } else if(key.getCharacter().equals("r")) {
            hintTimer.setType(HintButtonType.RIGHT);
        } else if(key.getCharacter().equals("b")) {
            hintTimer.setType(HintButtonType.BOTH);
        }
    }

    enum HintButtonType {LEFT, RIGHT, BOTH};
    private class PushHintTimer extends AnimationTimer {
        HintButtonType type = HintButtonType.BOTH;
        int offset = 0;
        boolean increment = true;
        long nextTime = 0;

        @Override
        public void handle(long now) {
            if(now > nextTime) {
                doHandle();
                nextTime = now + TimeUnit.NANOSECONDS.convert(50, TimeUnit.MILLISECONDS);
            }
        }

        void setType(HintButtonType type) {
            this.type = type;
        };

        private void doHandle() {
            GraphicsContext gc = hintCanvas.getGraphicsContext2D();
            double width = hintCanvas.getWidth();
            double height = hintCanvas.getHeight();

            gc.clearRect(0, 0, width, height);
            double x = 0.5*width;
            gc.drawImage(rhiotTagImage, x, 10);
            if(type == HintButtonType.LEFT || type == HintButtonType.BOTH) {
                x += 180 + offset;
                gc.drawImage(leftArrow, x, 35);
                gc.fillText("Push", x+25, 50);
            }
            if(type == HintButtonType.RIGHT || type == HintButtonType.BOTH) {
                x = 0.5 * width - rightArrow.getWidth() - offset;
                gc.drawImage(rightArrow, x, 35);
                gc.fillText("Push", x+50, 50);

            }
            if (increment)
                offset++;
            else
                offset--;
            if (offset > 10)
                increment = false;
            else if (offset <= 0)
                increment = true;
        }
    }
}
