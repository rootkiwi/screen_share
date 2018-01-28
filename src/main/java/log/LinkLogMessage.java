/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package log;

import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import static util.Util.openUrlInDefaultBrowser;

public class LinkLogMessage extends LogMessage {

    LinkLogMessage(String message) {
        super(message);
    }

    @Override
    public void setGui(Label timestampLabel, Label messageLabel) {
        super.setGui(timestampLabel, messageLabel);
        messageLabel.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> openUrlInDefaultBrowser(message));
        messageLabel.getStyleClass().add("link");
    }

}
