/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package monitor;

import java.awt.*;

public class MonitorInfo {

    private static VirtualScreenBoundingBox boundingBox;
    private static Rectangle primaryScreenBounds;

    public static VirtualScreenBoundingBox getVirtualScreenBoundingBox() {
        if (boundingBox == null) {
            setBoundingBox();
        }
        return boundingBox;
    }

    public static Rectangle getPrimaryScreenBounds() {
        if (primaryScreenBounds == null) {
            primaryScreenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration().getBounds();
        }
        return primaryScreenBounds;
    }

    private static void setBoundingBox() {
        int topLeftX = Integer.MAX_VALUE;
        int topLeftY = Integer.MAX_VALUE;
        int lowerRightX = Integer.MIN_VALUE;
        int loverRightY = Integer.MIN_VALUE;

        for (GraphicsDevice graphicsDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            Rectangle currentMonitorBounds = graphicsDevice.getDefaultConfiguration().getBounds();
            int currentTopLeftX = currentMonitorBounds.x;
            int currentTopLeftY = currentMonitorBounds.y;
            if (currentTopLeftX < topLeftX) {
                topLeftX = currentTopLeftX;
            }
            if (currentTopLeftY < topLeftY) {
                topLeftY = currentTopLeftY;
            }
            int currentWidth = currentMonitorBounds.width;
            int currentHeight = currentMonitorBounds.height;
            int currentLowerRightX = currentTopLeftX + currentWidth;
            int currentLowerRightY = currentTopLeftY + currentHeight;
            if (currentLowerRightX > lowerRightX) {
                lowerRightX = currentLowerRightX;
            }
            if (currentLowerRightY > loverRightY) {
                loverRightY = currentLowerRightY;
            }
        }

        int width = lowerRightX - topLeftX;
        int height = loverRightY - topLeftY;

        boundingBox = new VirtualScreenBoundingBox(topLeftX, topLeftY, lowerRightX, loverRightY, width, height);
    }

}
