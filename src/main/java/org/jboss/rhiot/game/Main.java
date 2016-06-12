package org.jboss.rhiot.game;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Created by starksm on 6/9/16.
 */
public class Main extends Application
{
    private MainController controller;

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Shoot the RHIoTTag!");
        URL fxml = getClass().getResource("main.fxml");
        FXMLLoader loader = new FXMLLoader(fxml);
        Parent root = loader.load();
        this.controller = loader.getController();
        System.out.printf("Loaded controller: %s\n", controller);
        //
        primaryStage.setScene(new Scene(root, 820, 820));
        primaryStage.setOnCloseRequest(this::handleClose);
        primaryStage.show();
    }

    /**
     * Close the controller if the user closes the window
     * @param we
     */
    private void handleClose(WindowEvent we) {
        controller.close();
    }
}