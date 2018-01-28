/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package log;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

public class JavaFxLogWriter implements LogWriter {

    private ObservableList<LogMessage> logList = FXCollections.observableArrayList();

    public JavaFxLogWriter(ListView<LogMessage> log, ReadOnlyDoubleProperty rootWidth) {
        log.setCellFactory(lv -> new ListViewCell());
        log.setItems(logList);
        logList.addListener((ListChangeListener<LogMessage>) c ->
                Platform.runLater(() -> log.scrollTo(logList.size()-1))
        );
        log.prefWidthProperty().bind(rootWidth);
    }

    @Override
    public void writeLogInfo(String message) {
        Platform.runLater(() -> logList.add(new InfoLogMessage(message)));
    }

    @Override
    public void writeLogLink(String message) {
        Platform.runLater(() -> logList.add(new LinkLogMessage(message)));
    }

    @Override
    public void writeLogError(String message) {
        Platform.runLater(() -> logList.add(new ErrorLogMessage(message)));
    }

    private class ListViewCell extends ListCell<LogMessage> {
        @Override
        public void updateItem(LogMessage logMessage, boolean empty) {
            super.updateItem(logMessage, empty);
            if (empty || logMessage == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new LogListCell(logMessage).getRoot());
            }
        }
    }

}
