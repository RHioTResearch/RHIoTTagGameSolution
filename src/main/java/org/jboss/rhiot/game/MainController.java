package org.jboss.rhiot.game;


import java.time.Duration;
import java.util.Date;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import org.jboss.rhiot.ble.bluez.RHIoTTag;
import org.jboss.rhiot.services.fsm.GameStateMachine;

/**
 * Created by starksm on 6/9/16.
 */
public class MainController implements ICloudListener {
    private static final int RING_WIDTH = 40;

    @FXML
    private Pane mainPane;
    @FXML
    private Canvas canvas;
    @FXML
    private Canvas hitCanvas;
    @FXML
    private Label shotsLeftLabel;
    @FXML
    private Label shotingTimeLeftLabel;
    @FXML
    private Label gameTimeLeftLabel;
    @FXML
    private Label gameScoreLabel;
    @FXML
    private Label stateLabel;
    @FXML
    private Label prevStateLabel;
    @FXML
    private Label eventLabel;
    @FXML
    private ChoiceBox<RHIoTTag.KeyState> keyStateChoiceBox;
    @FXML
    private Label luxLabel;
    @FXML
    private Label timeLeftLabel;

    private CloudClient cloudClient;
    private Image redTarget;
    private Image yellowTarget;
    private Image greenTarget;
    private Image[] bulletHoles;
    private int hitCount;
    private boolean running;


    @Override
    public void stateChange(GameStateMachine.GameState prevState, GameStateMachine.GameState newState, GameStateMachine.GameEvent event) {
        System.out.printf("stateChange(prevState=%s, newState=%s, event=%s)\n", prevState, newState, event);
        Platform.runLater(() -> {
            stateLabel.setText(newState.name());
            prevStateLabel.setText(prevState.name());
            eventLabel.setText(event.name());
            if(prevState == GameStateMachine.GameState.REPLACE_TARGET && newState == GameStateMachine.GameState.SHOOTING)
                clearTarget();
        });
    }

    @Override
    public void tagData(long time, double temp, RHIoTTag.KeyState keyState, int lux) {
        //System.out.printf("tagData(time=%s, temp=%.2f, keyState=%s, lux=%d)\n", new Date(time), temp, keyState, lux);
        Platform.runLater(() -> {
            luxLabel.setText(String.format("%d", lux));
            keyStateChoiceBox.setValue(keyState);
        });
    }

    @Override
    public void gameInfo(int shootingTimeLeft, int shotsLeft, int gameScore, int gameTimeLeft) {
        //System.out.printf("gameInfo(shootingTimeLeft=%d, shotsLeft=%d, gameScore=%d, gameTimeLeft=%d)\n", shootingTimeLeft, shotsLeft, gameScore, gameTimeLeft);
        Platform.runLater(() -> {
            shotsLeftLabel.setText(""+shotsLeft);
            gameScoreLabel.setText(""+gameScore);
            Duration stDuration = Duration.ofMillis(shootingTimeLeft);
            long mins = stDuration.toMinutes();
            long secs = stDuration.getSeconds() - mins*60;
            shotingTimeLeftLabel.setText(String.format("%02dM:%02dS", mins, secs));
            Duration gameDuration = Duration.ofMillis(gameTimeLeft);
            mins = gameDuration.toMinutes();
            secs = gameDuration.getSeconds() - mins*60;
            gameTimeLeftLabel.setText(String.format("%02dM:%02dS", mins, secs));
        });
    }

    @Override
    public void hitDetected(int hitScore, int ringsOffCenter) {
        System.out.printf("hitDetected(hitScore=%d, ringsOffCenter=%d)\n", hitScore, ringsOffCenter);
        Platform.runLater(() -> {
            displayShot(hitScore, ringsOffCenter);
        });
    }

    public void close() {
        try {
            cloudClient.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        stateLabel.setText("Idle");
        shotsLeftLabel.setText("10");
        hitCanvas.setId("HitCanvas");

        keyStateChoiceBox.setItems(FXCollections.observableArrayList(RHIoTTag.KeyState.values()));

        System.out.printf("mainPane children:\n");
        for(Node node : mainPane.getChildren()) {
            System.out.printf("\t%s\n", node);
        }

        redTarget = new Image("/target-red_720.png");
        yellowTarget = new Image("/target-yellow_720.png");
        greenTarget = new Image("/target-green_720.png");
        bulletHoles = new Image[6];
        for(int n = 0; n < bulletHoles.length; n ++) {
            bulletHoles[n] = new Image("/BH"+n+".png");
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.drawImage(greenTarget, 0, 0);
        System.out.printf("Set target to green\n");
        running = true;
        /*
        Thread t = new Thread(this::simulateShots, "ShotSimulator");
        t.setDaemon(true);
        t.start();
        */

        cloudClient = new CloudClient();
        Platform.runLater(this::startCloudClient);
    }

    private void startCloudClient() {
        try {
            cloudClient.start(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void simulateShots() {
        while (running) {
            displayShot(1000, -1);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
        }
    }
    private void displayShot(int hitScore, int ringsOffCenter) {
        int index = hitCount % bulletHoles.length;
        Image bh = bulletHoles[index];
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double height = canvas.getHeight();
        double width = canvas.getWidth();
        double x, y;
        if(ringsOffCenter < 0) {
            // center +/- random amount up to 40% of width/2
            x = (0.5 * width) + 0.9 * (0.5 - Math.random()) * width;
            y = (0.5 * height) + 0.9 * (0.5 - Math.random()) * height;
        } else {
            // In polar coords with r based on off center value and theta a random angle
            double r = 40*ringsOffCenter;
            double theta = Math.random() * 2*Math.PI;
            // convert to cartesian with origin in center of screen
            x = (0.5 * width) + r*Math.cos(theta);
            y = (0.5 * height) + r*Math.sin(theta);
        }
        gc.drawImage(bh, x, y);
        hitCount ++;

        final GraphicsContext gcHit = hitCanvas.getGraphicsContext2D();
        hitCanvas.setOpacity(0.9);
        gcHit.drawImage(redTarget, 0, 0);
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double opacity = hitCanvas.getOpacity();
                opacity -= 0.01;
                if(opacity < 0) {
                    opacity = 0;
                    stop();
                }
                hitCanvas.setOpacity(opacity);
                //System.out.printf("hitCanvas(%d), opacity=%.2f\n", now, opacity);
            }
        };
        timer.start();
    }
    private void clearTarget() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(greenTarget, 0, 0);
    }
}
