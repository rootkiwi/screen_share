/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package log;

import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class LogMessage {

    private String timestamp;
    String message;

    LogMessage(String message) {
        this.message = message;
        timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("[yyyy-MM-dd hh:mm:ss]"));
    }

    void setGui(Label timestampLabel, Label messageLabel) {
        timestampLabel.setText("[" + timestamp + "]");
        messageLabel.setText(message);
    }

}
