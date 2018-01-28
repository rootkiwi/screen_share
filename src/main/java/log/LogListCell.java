/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package log;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

class LogListCell {

    @FXML private HBox root;
    @FXML private Label timestampLabel;
    @FXML private Label messageLabel;

    LogListCell(LogMessage logMessage) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/logMessageCellItem.fxml"));
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logMessage.setGui(timestampLabel, messageLabel);
    }

    HBox getRoot() {
        return root;
    }

}
