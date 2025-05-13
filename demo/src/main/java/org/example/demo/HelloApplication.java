package org.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private HelloController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        controller = fxmlLoader.getController();
        
        stage.setTitle("Memory Game");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Clean up resources when application is stopped
        if (controller != null) {
            controller.shutdown();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}