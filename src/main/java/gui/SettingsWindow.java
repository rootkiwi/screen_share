/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package gui;

import build.BuildInfo;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

class SettingsWindow {

    private Stage stage;
    private CheckBox doSaveCheckbox;
    private CheckBox saveRemotePasswordCheckbox;

    SettingsWindow(boolean saveOnExit, boolean savePassword) {
        stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.setTitle("settings");
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);

        doSaveCheckbox = new CheckBox("Save all settings except the remote password");
        doSaveCheckbox.setSelected(saveOnExit);
        saveRemotePasswordCheckbox = new CheckBox("Also save remote password in cleartext");
        saveRemotePasswordCheckbox.setSelected(savePassword);

        doSaveCheckbox.selectedProperty().addListener(this::handleDoSaveCheckbox);
        if (!saveOnExit) {
            saveRemotePasswordCheckbox.setDisable(true);
        }

        Button okeyButton = new Button("okey");
        okeyButton.setOnAction(e -> stage.close());
        VBox checkBoxesVbox = new VBox(doSaveCheckbox, saveRemotePasswordCheckbox);
        checkBoxesVbox.setId("checkButtonVbox");
        checkBoxesVbox.setAlignment(Pos.CENTER);
        checkBoxesVbox.setSpacing(20);

        Label title = new Label("Automatically save your settings when exiting the program");
        title.setId("title");
        VBox root = new VBox(
                title,
                new Label("Config path:"),
                new Label(BuildInfo.configFilePath.toString()),
                checkBoxesVbox,
                okeyButton
        );
        root.setAlignment(Pos.CENTER);
        root.setSpacing(10);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/javafx/css/settings_window.css").toExternalForm());
        stage.setScene(scene);
    }

    private void handleDoSaveCheckbox(ObservableValue<? extends Boolean> observable,
                                        boolean oldValue, boolean newValue) {
        if (newValue) {
            saveRemotePasswordCheckbox.setDisable(false);
        } else {
            saveRemotePasswordCheckbox.setDisable(true);
            saveRemotePasswordCheckbox.setSelected(false);
        }
    }

    SettingsReturnValue displayAndWait() {
        stage.setMinWidth(920);
        stage.setMinHeight(440);
        stage.showAndWait();
        return new SettingsReturnValue(doSaveCheckbox.isSelected(), saveRemotePasswordCheckbox.isSelected());
    }

    static class SettingsReturnValue {
        public boolean saveOnExit;
        public boolean saveRemotePassword;
        private SettingsReturnValue(boolean doSave, boolean saveRemotePassword) {
            this.saveOnExit = doSave;
            this.saveRemotePassword = saveRemotePassword;
        }
    }

}
