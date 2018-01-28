/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package h264;

public interface RectangleUpdater {

    void updateRectangleX(int x);
    void updateRectangleY(int y);
    void updateRectangleWidth(int w);
    void updateRectangleHeight(int h);

}
