/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package h264;

public interface H264FrameQueueFillerController {

    void start(int fps, int crf, int maxrateMbits);
    void stop();
    void useFullscreen();
    void useRectangle();

}
