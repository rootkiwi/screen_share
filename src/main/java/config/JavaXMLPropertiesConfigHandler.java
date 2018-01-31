/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package config;

import build.BuildInfo;
import log.LogWriter;
import monitor.MonitorInfo;
import monitor.VirtualScreenBoundingBox;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

class JavaXMLPropertiesConfigHandler implements ConfigHandler {

    @Override
    public Settings getSettings(LogWriter logWriter) {
        if (Files.exists(BuildInfo.configFilePath)) {
            Properties properties = new Properties();
            try (InputStream in = Files.newInputStream(BuildInfo.configFilePath)) {
                properties.loadFromXML(in);
                if (isValidProperties(properties)) {
                    boolean isMissingNewlyAddedPageTitleProperty = !properties.containsKey("pageTitle");
                    if (isMissingNewlyAddedPageTitleProperty) {
                        properties.put("pageTitle", "screen_share");
                        logWriter.writeLogError("newly added setting 'pageTitle' is missing in your config");
                        logWriter.writeLogError("will add and use default 'pageTitle'");
                    }
                    return new Settings(properties);
                }
            } catch (Exception e) {
                logWriter.writeLogError("error loading config file: " + e.getMessage());
                logWriter.writeLogError("using default settings instead");
            }
        }
        Settings defaultSettings = new Settings();
        defaultSettings.saveSettingsOnExit = false;
        defaultSettings.saveRemotePassword = false;
        defaultSettings.pageTitle = "scree_share";
        defaultSettings.shareUsingEmbedded = false;
        defaultSettings.embeddedPort = "8080";
        defaultSettings.shareToRemote = false;
        defaultSettings.remoteAddress = "";
        defaultSettings.remotePassword = "";
        defaultSettings.remoteFingerprint = "";
        defaultSettings.fps = 8;
        defaultSettings.crf = 28;
        defaultSettings.maxRate = 4;
        defaultSettings.shareFullscreen = true;
        Rectangle primaryScreenBounds = MonitorInfo.getPrimaryScreenBounds();
        defaultSettings.rectX = primaryScreenBounds.x + primaryScreenBounds.width/4;
        defaultSettings.rectY = primaryScreenBounds.y + primaryScreenBounds.height/4;
        defaultSettings.rectWidth = primaryScreenBounds.width/2;
        defaultSettings.rectHeight = primaryScreenBounds.height/2;
        return defaultSettings;
    }

    private static boolean isValidProperties(Properties properties) throws Exception {
        String[] propertiesToContain = {
                "saveRemotePassword",
                "shareUsingEmbedded",
                "embeddedPort",
                "shareToRemote",
                "remoteAddress",
                "remotePassword",
                "remoteFingerprint",
                "fps",
                "crf",
                "maxRate",
                "shareFullscreen",
                "rectX",
                "rectY",
                "rectWidth",
                "rectHeight"
        };
        for (String property : propertiesToContain) {
            if (!properties.containsKey(property)) {
                throw new Exception("missing property: " + property);
            }
        }

        if (!validIntegerProp(properties.getProperty("fps"), 1,20)) {
            throw new Exception("invalid fps: " + properties.getProperty("fps"));
        }
        if (!validIntegerProp(properties.getProperty("crf"), 1,51)) {
            throw new Exception("invalid crf: " + properties.getProperty("crf"));
        }
        if (!validIntegerProp(properties.getProperty("maxRate"), 1,20)) {
            throw new Exception("invalid maxRate: " + properties.getProperty("maxRate"));
        }

        VirtualScreenBoundingBox boundingBox = MonitorInfo.getVirtualScreenBoundingBox();
        String rectWidthProp = properties.getProperty("rectWidth");
        if (!validIntegerProp(rectWidthProp, 200, boundingBox.width)) {
            throw new Exception("invalid rectWidth: " + rectWidthProp);
        }
        String rectHeightProp = properties.getProperty("rectHeight");
        if (!validIntegerProp(rectHeightProp, 200, boundingBox.height)) {
            throw new Exception("invalid rectHeight: " + rectHeightProp);
        }
        int rectWidth = Integer.parseInt(rectWidthProp);
        int rectHeight = Integer.parseInt(rectHeightProp);

        if (!validIntegerProp(properties.getProperty("rectX"), boundingBox.topLeftX, boundingBox.lowerRightX-rectWidth)) {
            throw new Exception("invalid rectX: " + properties.getProperty("rectX"));
        }
        if (!validIntegerProp(properties.getProperty("rectY"), boundingBox.topLeftY, boundingBox.lowerRightY-rectHeight)) {
            throw new Exception("invalid rectY: " + properties.getProperty("rectY"));
        }

        return true;
    }

    private static boolean validIntegerProp(String prop, int min, int max) {
        try {
            int num = Integer.parseInt(prop);
            if (num >= min && num <= max) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    @Override
    public void saveToDisk(Settings settings) {
        Properties properties = new Properties();
        fillProperties(properties, settings);
        try {
            Files.createDirectories(BuildInfo.configDirPath);
        } catch (IOException e) {
            System.err.println("error creating config directories: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        try (OutputStream out = Files.newOutputStream(BuildInfo.configFilePath)) {
            properties.storeToXML(out, null, "UTF-8");
        } catch (IOException e) {
            System.err.println("error writing config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void fillProperties(Properties properties, Settings settings) {
        properties.setProperty("saveRemotePassword", String.valueOf(settings.saveRemotePassword));
        properties.setProperty("shareUsingEmbedded", String.valueOf(settings.shareUsingEmbedded));
        properties.setProperty("pageTitle", String.valueOf(settings.pageTitle));
        properties.setProperty("embeddedPort", settings.embeddedPort);
        properties.setProperty("shareToRemote", String.valueOf(settings.shareToRemote));
        properties.setProperty("remoteAddress", settings.remoteAddress);
        properties.setProperty("remotePassword", settings.remotePassword);
        properties.setProperty("remoteFingerprint", settings.remoteFingerprint);
        properties.setProperty("fps", String.valueOf(settings.fps));
        properties.setProperty("crf", String.valueOf(settings.crf));
        properties.setProperty("maxRate", String.valueOf(settings.maxRate));
        properties.setProperty("shareFullscreen", String.valueOf(settings.shareFullscreen));
        properties.setProperty("rectX", String.valueOf(settings.rectX));
        properties.setProperty("rectY", String.valueOf(settings.rectY));
        properties.setProperty("rectWidth", String.valueOf(settings.rectWidth));
        properties.setProperty("rectHeight", String.valueOf(settings.rectHeight));
    }

    @Override
    public void deleteIfExists() {
        try {
            Files.deleteIfExists(BuildInfo.configFilePath);
            Files.deleteIfExists(BuildInfo.configDirPath);
        } catch (Exception e) {
            System.err.println("error removing conf file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
