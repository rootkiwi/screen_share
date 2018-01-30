/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import build.BuildInfo;

public class Main extends Application {

    private MainController controller;

    @Override
    public void start(Stage stage) throws Exception {
        if (BuildInfo.isBuildMadeForMe()) {
            startApplicationGui(stage);
        } else {
            displayErrorWindow(stage);
        }
    }

    private void startApplicationGui(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/javafx/fxml/main.fxml"));
        Parent root = fxmlLoader.load();
        controller = fxmlLoader.getController();
        Runtime.getRuntime().addShutdownHook(new Thread(controller::cleanup));
        Scene scene = new Scene(root, 1120, 740);
        stage.setTitle("screen_share");
        stage.setScene(scene);
        stage.show();
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
    }

    private void displayErrorWindow(Stage stage) {
        new ErrorAlertWindow(stage).display();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        // needed as well as addShutdownHook because sometime only ShutdownHook gets called, for example on SIGINT
        // but sometimes like for example when some threads are running and the application gets closed
        // the ShutdownHook does not get called, but stop() gets called because Platform.ImplicitExit
        try {
            controller.cleanup();
        } catch (Exception e) {}
    }
}
