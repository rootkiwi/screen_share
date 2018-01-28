/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package log;

import javafx.scene.control.Label;

class ErrorLogMessage extends LogMessage {

    ErrorLogMessage(String message) {
        super(message);
    }

    @Override
    public void setGui(Label timestampLabel, Label messageLabel) {
        super.setGui(timestampLabel, messageLabel);
        messageLabel.setStyle("-fx-text-fill: #FF1744;");
    }

}
