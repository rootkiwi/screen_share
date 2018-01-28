/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package config;

import java.util.Properties;

public class Settings {

    public boolean saveSettingsOnExit;
    public boolean saveRemotePassword;

    public boolean shareUsingEmbedded;
    public String embeddedPort;

    public boolean shareToRemote;
    public String remoteAddress;
    public String remotePassword;
    public String remoteFingerprint;

    public int fps;
    public int crf;
    public int maxRate;

    public boolean shareFullscreen;
    public int rectX;
    public int rectY;
    public int rectWidth;
    public int rectHeight;

    public Settings() {
    }

    Settings(Properties properties) {
        saveSettingsOnExit = true;
        saveRemotePassword = Boolean.parseBoolean(properties.getProperty("saveRemotePassword"));
        shareUsingEmbedded = Boolean.parseBoolean(properties.getProperty("shareUsingEmbedded"));
        embeddedPort = properties.getProperty("embeddedPort");
        shareToRemote = Boolean.parseBoolean(properties.getProperty("shareToRemote"));
        remoteAddress = properties.getProperty("remoteAddress");
        remotePassword = properties.getProperty("remotePassword");
        remoteFingerprint = properties.getProperty("remoteFingerprint");
        fps = Integer.parseInt(properties.getProperty("fps"));
        crf = Integer.parseInt(properties.getProperty("crf"));
        maxRate = Integer.parseInt(properties.getProperty("maxRate"));
        shareFullscreen = Boolean.parseBoolean(properties.getProperty("shareFullscreen"));
        rectX = Integer.parseInt(properties.getProperty("rectX"));
        rectY = Integer.parseInt(properties.getProperty("rectY"));
        rectWidth = Integer.parseInt(properties.getProperty("rectWidth"));
        rectHeight = Integer.parseInt(properties.getProperty("rectHeight"));
    }

}
