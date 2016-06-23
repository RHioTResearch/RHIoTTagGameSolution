package org.jboss.rhiot.game;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
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

    /**
     * Allow providing BLE tag address on command line so one can run multiple clients
     * @param args [0] = optional command line setting for MY_TAG_ADDRESS_OVERRIDE
     */
    public static void main(String[] args) {
        if(args.length > 0)
            CodeSourceTODOs.MY_TAG_ADDRESS_OVERRIDE = args[0];
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle(String.format("Shoot the RHIoTTag! - [%s/%s]", CodeSourceTODOs.MY_GW_NO, CodeSourceTODOs.getMyTagAddress()));
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