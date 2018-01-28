/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package monitor;

public class VirtualScreenBoundingBox {

    public int topLeftX;
    public int topLeftY;
    public int lowerRightX;
    public int lowerRightY;
    public int width;
    public int height;

    VirtualScreenBoundingBox(int topLeftX, int topLeftY, int lowerRightX, int lowerRightY, int width, int height) {
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.lowerRightX = lowerRightX;
        this.lowerRightY = lowerRightY;
        this.width = width;
        this.height = height;
    }

}
