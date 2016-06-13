package org.jboss.rhiot.game;

import java.util.concurrent.TimeUnit;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by starksm on 6/11/16.
 */
public class HintAnimation extends AnimationTimer {
    enum HintButtonType {LEFT, RIGHT, BOTH};

    private Canvas hintCanvas;
    private Canvas canvas;
    HintButtonType type = HintButtonType.BOTH;
    int offset = 0;
    boolean increment = true;
    long nextTime = 0;
    private Image rhiotTagImage;
    private Image leftArrow;
    private Image rightArrow;

    public HintAnimation(Canvas hintCanvas, Canvas canvas) {
        this.hintCanvas = hintCanvas;
        this.canvas = canvas;
        rhiotTagImage = new Image("/rhiottag.png");
        leftArrow = new Image("/leftArrow.png");
        rightArrow = new Image("/rightArrow.png");
        GraphicsContext hintGC = hintCanvas.getGraphicsContext2D();
        hintGC.setFont(Font.font("Courier", 18));
        hintGC.setFill(Color.BLUE);
    }

    @Override
    public void handle(long now) {
        if(now > nextTime) {
            doHandle();
            nextTime = now + TimeUnit.NANOSECONDS.convert(50, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void stop() {
        hintCanvas.getGraphicsContext2D().clearRect(0, 0, hintCanvas.getWidth(), hintCanvas.getHeight());
        super.stop();
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
            x = 0.5 * width - rightArrow.getWidth() - offset;
            gc.drawImage(rightArrow, x, 35);
            gc.fillText("Push", x+50, 50);
        }
        if(type == HintButtonType.RIGHT || type == HintButtonType.BOTH) {
            x = 0.5 * width  + 180 + offset;
            gc.drawImage(leftArrow, x, 35);
            gc.fillText("Push", x+25, 50);
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
