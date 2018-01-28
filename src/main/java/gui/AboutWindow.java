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
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import build.BuildInfo;
import javafx.stage.StageStyle;

import static util.Util.openUrlInDefaultBrowser;

class AboutWindow {

    private Stage stage;

    AboutWindow() {
        stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.setTitle("about");
        stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                stage.close();
            }
        });
        stage.initStyle(StageStyle.UNDECORATED);
        stage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        Label title = new Label("screen_share");
        title.setId("title");
        Label version = new Label("Version: " + BuildInfo.getBuildVersion());

        Label licenseLabel = new Label("License: ");
        Label licenseLink = new Label("GNU General Public License version 3 or later");
        setLinkOnClickHandler(licenseLink, "https://www.gnu.org/licenses/gpl-3.0.en.html");
        licenseLink.getStyleClass().add("link");
        HBox licenseHbox = new HBox(licenseLabel, licenseLink);
        licenseHbox.setAlignment(Pos.CENTER);

        Label sourceLabel = new Label("Source: ");
        Label sourceLink = new Label("https://github.com/rootkiwi/screen_share/");
        setLinkOnClickHandler(sourceLink, "https://github.com/rootkiwi/screen_share/");
        sourceLink.getStyleClass().add("link");
        HBox sourceHbox = new HBox(sourceLabel, sourceLink);
        sourceHbox.setAlignment(Pos.CENTER);

        Label thirdPartiesTitle = new Label("Third party components:");
        thirdPartiesTitle.setId("thirdPartyComponentsTitle");

        VBox thirdPartiesVbox = new VBox(
                thirdPartiesTitle,
                getJettyVbox(),
                getJavaCvVbox(),
                getFFmpegVbox(),
                getX264Vbox(),
                getBroadwayVbox(),
                getGradleShadowVbox()
        );
        thirdPartiesVbox.setId("thirdPartyVbox");
        thirdPartiesVbox.setAlignment(Pos.CENTER);
        thirdPartiesVbox.setSpacing(20);

        VBox root = new VBox(title, version, licenseHbox, sourceHbox, thirdPartiesVbox);
        root.setAlignment(Pos.CENTER);
        root.setSpacing(10);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/about_window.css").toExternalForm());
        stage.setScene(scene);
    }

    private void setLinkOnClickHandler(Label label, String url) {
        label.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> openUrlInDefaultBrowser(url));
    }

    private VBox getThirdPartyVbox(String name, String homepageUrl, String licenseName, String licenseUrl) {
        Label title = new Label(name);
        title.getStyleClass().add("thirdPartyTitle");

        Label homepageLabel = new Label("Homepage: ");
        Label homepageLink = new Label(homepageUrl);
        setLinkOnClickHandler(homepageLink, homepageUrl);
        homepageLink.getStyleClass().add("link");
        HBox jettyHomepageHbox = new HBox(homepageLabel, homepageLink);
        jettyHomepageHbox.setAlignment(Pos.CENTER);

        Label licenseLabel = new Label("License: ");
        Label licenseLink = new Label(licenseName);
        setLinkOnClickHandler(licenseLink, licenseUrl);
        licenseLink.getStyleClass().add("link");
        HBox jettyLicenseHbox = new HBox(licenseLabel, licenseLink);
        jettyLicenseHbox.setAlignment(Pos.CENTER);

        VBox thirdPartyVbox = new VBox(title, jettyHomepageHbox, jettyLicenseHbox);
        thirdPartyVbox.setSpacing(4);
        thirdPartyVbox.setAlignment(Pos.CENTER);
        return thirdPartyVbox;
    }

    private VBox getJettyVbox() {
        return getThirdPartyVbox(
                "Eclipse Jetty",
                "https://www.eclipse.org/jetty/",
                "Apache License 2.0",
                "https://www.eclipse.org/jetty/licenses.html"
        );
    }

    private VBox getJavaCvVbox() {
        return getThirdPartyVbox(
                "JavaCV",
                "https://github.com/bytedeco/javacv/",
                "Apache License 2.0",
                "https://github.com/bytedeco/javacv/blob/master/LICENSE.txt"
        );
    }

    private VBox getFFmpegVbox() {
        return getThirdPartyVbox(
                "FFmpeg",
                "https://ffmpeg.org/",
                "GNU General Public License version 2 or later",
                "https://www.ffmpeg.org/legal.html"
        );
    }

    private VBox getX264Vbox() {
        return getThirdPartyVbox(
                "x264",
                "https://www.videolan.org/developers/x264.html",
                "GNU General Public License version 2 or later",
                "https://www.gnu.org/licenses/old-licenses/gpl-2.0.html"
        );
    }

    private VBox getBroadwayVbox() {
        return getThirdPartyVbox(
                "Broadway",
                "https://github.com/mbebenita/Broadway/",
                "3-clause BSD License",
                "https://github.com/mbebenita/Broadway/blob/master/LICENSE"
        );
    }

    private VBox getGradleShadowVbox() {
        return getThirdPartyVbox(
                "Gradle Shadow",
                "https://github.com/johnrengelman/shadow/",
                "Apache License 2.0",
                "https://github.com/johnrengelman/shadow/blob/master/LICENSE"
        );
    }

    void display() {
        stage.show();
    }

}
