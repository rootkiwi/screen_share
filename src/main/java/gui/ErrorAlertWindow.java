/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import build.BuildInfo;

import java.util.List;

import static build.BuildInfo.JVM_BITNESS;
import static build.BuildInfo.OPERATIVE_SYSTEM;

class ErrorAlertWindow {

    private Stage stage;

    ErrorAlertWindow(Stage stage) {
        this.stage = stage;
        stage.setResizable(false);
        stage.setTitle("error");

        Label infoText = new Label("This build is not made for you");
        infoText.setId("title");
        Label thisBuildLabel = new Label("You are running:");
        thisBuildLabel.getStyleClass().add("infoLabel");
        Label thisBuild = new Label(getThisOsAndJvmInfoString());
        Label madeForLabel = new Label("But this build is made for:");
        madeForLabel.getStyleClass().add("infoLabel");
        Label madeFor = new Label(getBuiltForString());
        Button okeyButton = new Button("okey :(");
        okeyButton.setOnAction(e -> System.exit(0));
        VBox vBox = new VBox(infoText, thisBuildLabel, thisBuild, madeForLabel, madeFor, okeyButton);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(40);

        Scene scene = new Scene(vBox);
        scene.getStylesheets().add(getClass().getResource("/javafx/css/error_alert_window.css").toExternalForm());

        stage.setScene(scene);
    }

    private String getThisOsAndJvmInfoString() {
        String thisOsJvmInfo = OPERATIVE_SYSTEM.pretty
                + " with Java JVM " + JVM_BITNESS.pretty;
        return thisOsJvmInfo;
    }

    private String getBuiltForString() {
        List<String> builtFor = BuildInfo.getBuiltForList();
        StringBuilder builtForInfo = new StringBuilder();
        for (int i = 0; i < builtFor.size()-1; i++) {
            builtForInfo.append(builtFor.get(i));
            builtForInfo.append(", ");
        }
        builtForInfo.append(builtFor.get(builtFor.size()-1));
        return builtForInfo.toString();
    }

    void display() {
        stage.show();
    }

}
