/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package gui;

import h264.RectangleUpdater;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;

class RectangleWindow {

    private Stage stage;

    RectangleWindow(RectangleUpdater rectangleUpdater,
                    Rectangle initialBounds) {
        Button okeyButton = new Button("okey");
        okeyButton.setOnAction(e -> close());
        StackPane stackPane = new StackPane(okeyButton);

        Scene scene = new Scene(stackPane, initialBounds.width, initialBounds.height);
        scene.getStylesheets().add(getClass().getResource("/css/rectangle_window.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setAlwaysOnTop(true);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setX(initialBounds.x);
        stage.setY(initialBounds.y);

        new RectangleResizeHelper().setStageToHandle(stage);
        registerPropertyListeners(rectangleUpdater, initialBounds);
    }

    private void registerPropertyListeners(RectangleUpdater rectangleUpdater, Rectangle initialBounds) {
        rectangleUpdater.updateRectangleX(initialBounds.x);
        rectangleUpdater.updateRectangleY(initialBounds.y);
        rectangleUpdater.updateRectangleWidth(initialBounds.width);
        rectangleUpdater.updateRectangleHeight(initialBounds.height);
        stage.xProperty().addListener((observable, oldValue, newValue) -> {
            rectangleUpdater.updateRectangleX(newValue.intValue());
        });
        stage.yProperty().addListener((observable, oldValue, newValue) -> {
            rectangleUpdater.updateRectangleY(newValue.intValue());
        });
        stage.widthProperty().addListener((observable, oldValue, newValue) -> {
            rectangleUpdater.updateRectangleWidth(newValue.intValue());
        });
        stage.heightProperty().addListener((observable, oldValue, newValue) -> {
            rectangleUpdater.updateRectangleHeight(newValue.intValue());
        });
    }

    void display() {
        stage.show();
    }

    private void close() {
        stage.close();
    }

}
