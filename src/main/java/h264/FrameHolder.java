/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package h264;

class FrameHolder {

    byte[] frameBytes;
    short frameWidth;
    short frameHeight;

    void update(byte[] frameBytes, short frameWidth, short frameHeight) {
        this.frameBytes = frameBytes;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

}
