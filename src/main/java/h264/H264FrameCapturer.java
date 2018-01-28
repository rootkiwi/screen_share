/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package h264;

import java.awt.*;

public interface H264FrameCapturer {

    /**
     * Will fill frameHolder with result
     */
    void getFrameFullscreen(FrameHolder frameHolder, boolean forceKeyFrame);

    /**
     * Will fill frameHolder with result
     */
    void getFrameRoi(FrameHolder frameHolder, boolean forceKeyFrame, Rectangle roi);

    void stop();
    void setFps(int fps);
    void setCrf(int crf);
    void setMaxrateMbits(int maxrateMbits);

}
