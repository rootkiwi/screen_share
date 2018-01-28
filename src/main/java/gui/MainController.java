/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package gui;

import config.ConfigHandler;
import config.ConfigHandlerFactory;
import config.Settings;
import h264.H264FrameQueueFiller;
import h264.H264FrameQueueFillerController;
import h264.RectangleUpdater;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import embedded.EmbeddedWebServer;
import embedded.WebServerProvider;
import log.*;
import monitor.MonitorInfo;
import monitor.VirtualScreenBoundingBox;
import remote.RemoteCallback;
import remote.RemoteHandler;
import remote.RemoteConnectionHandler;
import stats.StatsUpdaterHandler;

import java.awt.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static util.Util.isHexNumeric;

public class MainController implements GuiStatsUpdater, RectangleUpdater, RemoteCallback {

    @FXML private VBox root;
    @FXML private MenuItem menuSettings;
    @FXML private MenuItem menuExit;
    @FXML private MenuItem menuAbout;

    @FXML private CheckBox embeddedCheckbox;
    @FXML private HBox embeddedPortHbox;
    @FXML private TextField embeddedPort;
    @FXML private HBox embeddedHboxStats;
    @FXML private Label embeddedConnectionsValue;
    @FXML private Label embeddedBitsPerSecondValue;
    @FXML private Label embeddedBitsPerSecondLabel;
    @FXML private Label embeddedMegabytesTransferredValue;

    @FXML private CheckBox remoteCheckbox;
    @FXML private HBox remoteAddressHbox;
    @FXML private HBox remotePasswordHbox;
    @FXML private HBox remoteFingerprintHbox;
    @FXML private HBox remoteButtonHbox;
    @FXML private Label toRemoteLabel;
    @FXML private HBox toRemoteHbox;
    @FXML private Label fromRemoteLabel;
    @FXML private HBox fromRemoteHbox;

    @FXML private TextField remoteAddress;
    @FXML private PasswordField remotePassword;
    @FXML private TextField remoteFingerprint;
    @FXML private Button remoteConnectButton;
    @FXML private Button remoteDisconnectButton;

    @FXML private Label resolutionWidthValue;
    @FXML private Label resolutionHeightValue;

    @FXML private ListView<LogMessage> log;
    @FXML private ChoiceBox<String> fpsChoiceBox;
    @FXML private ChoiceBox<String> crfChoiceBox;
    @FXML private ChoiceBox<String> maxRateChoiceBox;
    @FXML private ChoiceBox<String> whatToShareChoiceBox;
    @FXML private Button startShareButton;
    @FXML private Button stopShareButton;
    @FXML private Button editRectangleButton;

    private final String CHOICE_BOX_FULLSCREEN = "share fullscreen";
    private final String CHOICE_BOX_RECTANGLE = "share inside rectangle";
    private LogWriter logWriter;
    private H264FrameQueueFillerController h264FrameQueueFiller;
    private RectangleUpdater queueFillerRectangleUpdater;
    private RectangleWindow rectangleWindow;
    private EmbeddedWebServer embeddedWebServer;
    private RemoteConnectionHandler remoteHandler;
    private StatsUpdaterHandler statsUpdater;
    private Dimension fullscreenResolution;
    private Rectangle rectangleBounds = new Rectangle();
    private ConfigHandler configHandler = ConfigHandlerFactory.getConfigHandler();
    private Settings settings;
    private boolean saveSettingsOnExit = false;

    private boolean remoteIsConnected = false;

    @FXML
    public void initialize() {
        logWriter = new JavaFxLogWriter(log, root.widthProperty());
        settings = configHandler.getSettings(logWriter);
        statsUpdater = new StatsUpdaterHandler(this);
        H264FrameQueueFiller h264FrameQueueFiller = new H264FrameQueueFiller();
        this.h264FrameQueueFiller = h264FrameQueueFiller;
        queueFillerRectangleUpdater = h264FrameQueueFiller;
        embeddedWebServer = WebServerProvider.getEmbeddedWebserver(statsUpdater, h264FrameQueueFiller, logWriter);
        remoteHandler = new RemoteHandler(h264FrameQueueFiller, this, logWriter);
        setUpWhatToShareChoiceBox();
        registerEventHandlers();
        setUpVideoSettingChoices();
        rectangleWindow = new RectangleWindow(
                this,
                new Rectangle(settings.rectX, settings.rectY, settings.rectWidth, settings.rectHeight)
        );
        setFromSettings();
        logWriter.writeLogInfo("screen_share gui started");
    }

    private void setFromSettings() {
        saveSettingsOnExit = settings.saveSettingsOnExit;
        embeddedCheckbox.setSelected(settings.shareUsingEmbedded);
        embeddedPort.setText(settings.embeddedPort);
        remoteCheckbox.setSelected(settings.shareToRemote);
        remoteAddress.setText(settings.remoteAddress);
        remotePassword.setText(settings.remotePassword);
        remoteFingerprint.setText(settings.remoteFingerprint);
        fpsChoiceBox.getSelectionModel().select(settings.fps-1);
        crfChoiceBox.getSelectionModel().select(settings.crf-1);
        maxRateChoiceBox.getSelectionModel().select(settings.maxRate-1);
    }

    private void setUpWhatToShareChoiceBox() {
        whatToShareChoiceBox.getItems().addAll(CHOICE_BOX_FULLSCREEN, CHOICE_BOX_RECTANGLE);
        if (settings.shareFullscreen) {
            whatToShareChoiceBox.getSelectionModel().selectFirst();
            handleChoiceShareFullscreen();
        } else {
            whatToShareChoiceBox.getSelectionModel().selectLast();
            handleChoiceShareRectangle();
        }
        whatToShareChoiceBox.getSelectionModel().selectedItemProperty().addListener(this::handleChoiceBoxSelection);
    }

    private void setUpVideoSettingChoices() {
        for (int i = 1; i <= 20; i++) {
            fpsChoiceBox.getItems().addAll("fps: " + i);
        }
        for (int i = 1; i <= 51; i++) {
            crfChoiceBox.getItems().addAll("x264 crf: " + i);
        }
        for (int i = 1; i <= 20; i++) {
            maxRateChoiceBox.getItems().addAll("max bitrate: " + i + " mbit/s");
        }
    }

    private void setResolutionValueToFullscreen() {
        if (fullscreenResolution == null) {
            VirtualScreenBoundingBox boundingBox = MonitorInfo.getVirtualScreenBoundingBox();
            fullscreenResolution = new Dimension(boundingBox.width, boundingBox.height);
        }
        resolutionWidthValue.setText(String.valueOf(fullscreenResolution.width));
        resolutionHeightValue.setText(String.valueOf(fullscreenResolution.height));
    }

    private void registerEventHandlers() {
        menuSettings.setOnAction(e -> {
            SettingsWindow.SettingsReturnValue returnValue = new SettingsWindow(
                    saveSettingsOnExit,
                    settings.saveRemotePassword
            ).displayAndWait();
            saveSettingsOnExit = returnValue.saveOnExit;
            settings.saveRemotePassword = returnValue.saveRemotePassword;
        });
        menuExit.setOnAction(e -> Platform.exit());
        menuAbout.setOnAction(e -> new AboutWindow().display());

        embeddedCheckbox.selectedProperty().addListener(this::handleEmbeddedCheckbox);
        remoteCheckbox.selectedProperty().addListener(this::handleRemoteCheckbox);

        remoteConnectButton.setOnAction(this::handleRemoteConnectButton);
        remoteDisconnectButton.setOnAction(this::handleRemoteDisconnectButton);

        startShareButton.setOnAction(this::handleStartShareButton);
        stopShareButton.setOnAction(this::handleStopShareButton);
        editRectangleButton.setOnAction(e -> rectangleWindow.display());
    }

    private void handleEmbeddedCheckbox(ObservableValue<? extends Boolean> observable,
                                        boolean oldValue, boolean newValue) {
        if (newValue) {
            enableEmbeddedGui();
            enableEmbeddedInput();
            if (!remoteCheckbox.isSelected() || remoteIsConnected) {
                enableStartShareButton();
            }
        } else {
            disableEmbeddedGui();
            if (!remoteIsConnected) {
                disableStartShareButton();
            }
        }
    }

    private void handleRemoteCheckbox(ObservableValue<? extends Boolean> observable,
                                      boolean oldValue, boolean newValue) {
        if (newValue) {
            enableRemoteGui();
            disableStartShareButton();
        } else {
            disableRemoteGui();
            if (embeddedCheckbox.isSelected()) {
                enableStartShareButton();
            }
        }
    }

    private void handleChoiceBoxSelection(ObservableValue<? extends String> observable,
                                          String oldValue, String newValue) {
        if (newValue.equals(CHOICE_BOX_FULLSCREEN)) {
            handleChoiceShareFullscreen();
        } else {
            handleChoiceShareRectangle();
            rectangleWindow.display();
        }
    }

    private void handleChoiceShareFullscreen() {
        h264FrameQueueFiller.useFullscreen();
        editRectangleButton.setVisible(false);
        editRectangleButton.setManaged(false);
        setResolutionValueToFullscreen();
    }

    private void handleChoiceShareRectangle() {
        h264FrameQueueFiller.useRectangle();
        editRectangleButton.setVisible(true);
        editRectangleButton.setManaged(true);
        setResolutionValueFromRectangle();
    }

    private void setResolutionValueFromRectangle() {
        resolutionWidthValue.setText(String.valueOf(rectangleBounds.width));
        resolutionHeightValue.setText(String.valueOf(rectangleBounds.height));
    }

    @Override
    public void remoteFailedToConnect() {
        enableRemoteConnectButton();
    }

    @Override
    public void remoteConnected() {
        hideRemoteConnectButton();
        showRemoteDisconnectButton();
        disableRemoteCheckbox();
        disableRemoteInput();
        enableStartShareButton();
        remoteIsConnected = true;
    }

    @Override
    public void remoteDisconnected() {
        hideRemoteDisconnectButton();
        showRemoteConnectButton();
        enableRemoteConnectButton();
        enableRemoteCheckbox();
        enableRemoteInput();
        remoteIsConnected = false;
    }

    private void handleRemoteConnectButton(ActionEvent e) {
        String addressInput = remoteAddress.getText().trim();
        String[] addressInputSplit = addressInput.split(":");
        if (addressInputSplit.length != 2) {
            logWriter.writeLogError("invalid remote address");
            return;
        }
        String passwordInput = remotePassword.getText();
        if (passwordInput.length() < 1) {
            logWriter.writeLogError("invalid remote password: minimum 1 characters");
            return;
        }
        String fingerprintInput = remoteFingerprint.getText().toUpperCase();
        if (!isHexNumeric(fingerprintInput)) {
            logWriter.writeLogError("invalid remote fingerprint: contains non hex");
            return;
        }
        if (fingerprintInput.length() < 64) {
            logWriter.writeLogError("invalid remote fingerprint: too short");
            return;
        }
        if (fingerprintInput.length() > 64) {
            logWriter.writeLogError("invalid remote fingerprint: too long");
            return;
        }
        String address = addressInputSplit[0];
        int port;
        try {
            port = Integer.parseInt(addressInputSplit[1]);
            if (port >= 1 && port <= 65535) {
                disableRemoteConnectButton();
                remoteHandler.connect(address, port, passwordInput, fingerprintInput);
            } else {
                logWriter.writeLogError("remote port error: invalid port (1-65535)");
            }
        } catch (NumberFormatException nfe) {
            logWriter.writeLogError("remote port error: not a number");
        }
    }

    private void handleRemoteDisconnectButton(ActionEvent e) {
        remoteHandler.disconnect();
    }

    private void handleStartShareButton(ActionEvent e) {
        if (embeddedCheckbox.isSelected()) {
            int port;
            try {
                port = Integer.parseInt(embeddedPort.getText());
                if (port >= 0 && port <= 65535) {
                    boolean success = embeddedWebServer.start(port);
                    if (success) {
                        logWriter.writeLogInfo("embedded web server started");
                        printLocalAddressesToLog(port);
                    } else {
                        return;
                    }
                } else {
                    logWriter.writeLogError("embedded port error: invalid port (0-65535)");
                    return;
                }
            } catch (NumberFormatException nfe) {
                logWriter.writeLogError("embedded port error: not a number");
                return;
            }
        }
        statsUpdater.start();
        h264FrameQueueFiller.start(getSelectedFps(), getSelectedCrf(), getSelectedMaxRate());
        disableVideoSettingsChoices();
        disableEmbeddedCheckbox();
        disableEmbeddedInput();
        disableRemoteCheckbox();
        disableRemoteInput();
        hideStartShareButton();
        showStopShareButton();
    }

    private void handleStopShareButton(ActionEvent e) {
        if (embeddedCheckbox.isSelected()) {
            embeddedWebServer.stop();
            enableEmbeddedInput();
            logWriter.writeLogInfo("embedded web server stopped");
        }
        if (remoteCheckbox.isSelected()) {
            remoteHandler.disconnect();
            enableRemoteInput();
        }
        enableEmbeddedCheckbox();
        enableRemoteCheckbox();
        enableVideoSettingsChoices();
        hideStopShareButton();
        showStartShareButton();
        h264FrameQueueFiller.stop();
        statsUpdater.stop();
    }

    private void printLocalAddressesToLog(int port) {
        try {
            List<String> ipAddresses = new ArrayList<>();
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp() && !networkInterface.isPointToPoint()) {
                    for (InetAddress inetAddress: Collections.list(networkInterface.getInetAddresses())) {
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            ipAddresses.add("http://" + inetAddress.getHostAddress() + ":" + port);
                        }
                    }
                }
            }
            logWriter.writeLogInfo("waiting for connections locally on");
            for (String address : ipAddresses) {
                logWriter.writeLogLink(address);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private int getSelectedFps() {
        return fpsChoiceBox.getSelectionModel().getSelectedIndex()+1;
    }

    private int getSelectedCrf() {
        return crfChoiceBox.getSelectionModel().getSelectedIndex()+1;
    }

    private int getSelectedMaxRate() {
        return maxRateChoiceBox.getSelectionModel().getSelectedIndex()+1;
    }

    private void enableEmbeddedGui() {
        embeddedPortHbox.setDisable(false);
        embeddedHboxStats.setDisable(false);
    }

    private void disableEmbeddedGui() {
        embeddedPortHbox.setDisable(true);
        embeddedHboxStats.setDisable(true);
    }

    private void enableRemoteGui() {
        remoteAddressHbox.setDisable(false);
        remotePasswordHbox.setDisable(false);
        remoteFingerprintHbox.setDisable(false);
        remoteButtonHbox.setDisable(false);
        toRemoteLabel.setDisable(false);
        toRemoteHbox.setDisable(false);
        fromRemoteLabel.setDisable(false);
        fromRemoteHbox.setDisable(false);
    }

    private void disableRemoteGui() {
        remoteAddressHbox.setDisable(true);
        remotePasswordHbox.setDisable(true);
        remoteFingerprintHbox.setDisable(true);
        remoteButtonHbox.setDisable(true);
        toRemoteLabel.setDisable(true);
        toRemoteHbox.setDisable(true);
        fromRemoteLabel.setDisable(true);
        fromRemoteHbox.setDisable(true);
    }

    private void disableVideoSettingsChoices() {
        fpsChoiceBox.setDisable(true);
        crfChoiceBox.setDisable(true);
        maxRateChoiceBox.setDisable(true);
    }

    private void enableVideoSettingsChoices() {
        fpsChoiceBox.setDisable(false);
        crfChoiceBox.setDisable(false);
        maxRateChoiceBox.setDisable(false);
    }

    private void enableEmbeddedCheckbox() {
        embeddedCheckbox.setDisable(false);
    }

    private void disableEmbeddedCheckbox() {
        embeddedCheckbox.setDisable(true);
    }

    private void enableEmbeddedInput() {
        embeddedPort.setDisable(false);
    }

    private void disableEmbeddedInput() {
        embeddedPort.setDisable(true);
    }

    private void enableRemoteCheckbox() {
        remoteCheckbox.setDisable(false);
    }

    private void disableRemoteCheckbox() {
        remoteCheckbox.setDisable(true);
    }

    private void enableRemoteConnectButton() {
        remoteConnectButton.setDisable(false);
    }

    private void disableRemoteConnectButton() {
        remoteConnectButton.setDisable(true);
    }

    private void enableRemoteInput() {
        remoteAddressHbox.setDisable(false);
        remotePasswordHbox.setDisable(false);
        remoteFingerprintHbox.setDisable(false);
    }

    private void disableRemoteInput() {
        remoteAddressHbox.setDisable(true);
        remotePasswordHbox.setDisable(true);
        remoteFingerprintHbox.setDisable(true);
    }

    private void enableStartShareButton() {
        startShareButton.setDisable(false);
    }

    private void disableStartShareButton() {
        startShareButton.setDisable(true);
    }

    private void showStartShareButton() {
        startShareButton.setManaged(true);
        startShareButton.setVisible(true);
    }

    private void hideStartShareButton() {
        startShareButton.setManaged(false);
        startShareButton.setVisible(false);
    }

    private void showStopShareButton() {
        stopShareButton.setManaged(true);
        stopShareButton.setVisible(true);
    }

    private void hideStopShareButton() {
        stopShareButton.setManaged(false);
        stopShareButton.setVisible(false);
    }

    private void showRemoteConnectButton() {
        remoteConnectButton.setManaged(true);
        remoteConnectButton.setVisible(true);
    }

    private void hideRemoteConnectButton() {
        remoteConnectButton.setManaged(false);
        remoteConnectButton.setVisible(false);
    }

    private void showRemoteDisconnectButton() {
        remoteDisconnectButton.setManaged(true);
        remoteDisconnectButton.setVisible(true);
    }

    private void hideRemoteDisconnectButton() {
        remoteDisconnectButton.setManaged(false);
        remoteDisconnectButton.setVisible(false);
    }

    @Override
    public void setEmbeddedConnections(int connections) {
        Platform.runLater(() -> embeddedConnectionsValue.setText(String.valueOf(connections)));
    }

    @Override
    public void setEmbeddedBitsPerSecond(float bitsPerSecond) {
        String label;
        String value;
        if (bitsPerSecond < 1024*1024) {
            label = "kbit/s";
            value = String.format("%.2f", bitsPerSecond/1024f);
        } else {
            label = "mbit/s";
            value = String.format("%.2f", bitsPerSecond/1024f/1024f);
        }
        Platform.runLater(() -> {
            embeddedBitsPerSecondLabel.setText(label);
            embeddedBitsPerSecondValue.setText(value);
        });
    }

    @Override
    public void setEmbeddedMegabytesTransferred(float megabytesTransferred) {
        Platform.runLater(() -> embeddedMegabytesTransferredValue.setText(String.format("%.2f", megabytesTransferred)));
    }

    @Override
    public void updateRectangleX(int x) {
        queueFillerRectangleUpdater.updateRectangleX(x);
        rectangleBounds.x = x;
    }

    @Override
    public void updateRectangleY(int y) {
        queueFillerRectangleUpdater.updateRectangleY(y);
        rectangleBounds.y = y;
    }

    @Override
    public void updateRectangleWidth(int width) {
        queueFillerRectangleUpdater.updateRectangleWidth(width);
        int evenWidth = width % 2 == 0 ? width : width-1;
        rectangleBounds.width = evenWidth;
        if (whatToShareChoiceBox.getValue().equals(CHOICE_BOX_RECTANGLE)) {
            resolutionWidthValue.setText(String.valueOf(evenWidth));
        }
    }

    @Override
    public void updateRectangleHeight(int height) {
        queueFillerRectangleUpdater.updateRectangleHeight(height);
        int evenHeight = height % 2 == 0 ? height : height-1;
        rectangleBounds.height = evenHeight;
        if (whatToShareChoiceBox.getValue().equals(CHOICE_BOX_RECTANGLE)) {
            resolutionHeightValue.setText(String.valueOf(evenHeight));
        }
    }

    void cleanup() {
        statsUpdater.stop();
        h264FrameQueueFiller.stop();
        embeddedWebServer.stop();
        remoteHandler.disconnect();
        if (saveSettingsOnExit) {
            saveSettings();
        } else {
            configHandler.deleteIfExists();
        }
    }

    private void saveSettings() {
        settings.shareUsingEmbedded = embeddedCheckbox.isSelected();
        settings.embeddedPort = embeddedPort.getText();
        settings.shareToRemote = remoteCheckbox.isSelected();
        settings.remoteAddress = remoteAddress.getText();
        settings.remotePassword = settings.saveRemotePassword ? remotePassword.getText() : "";
        settings.remoteFingerprint = remoteFingerprint.getText();
        settings.fps = getSelectedFps();
        settings.crf = getSelectedCrf();
        settings.maxRate = getSelectedMaxRate();
        settings.shareFullscreen = whatToShareChoiceBox.getSelectionModel().getSelectedItem()
                .equals(CHOICE_BOX_FULLSCREEN);
        settings.rectX = rectangleBounds.x;
        settings.rectY = rectangleBounds.y;
        settings.rectWidth = rectangleBounds.width;
        settings.rectHeight = rectangleBounds.height;
        configHandler.saveToDisk(settings);
    }

}
