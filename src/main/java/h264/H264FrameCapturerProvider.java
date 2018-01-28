/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package h264;

class H264FrameCapturerProvider {

    static H264FrameCapturer getH264FrameCapturer() {
        return new FFmpegH264FrameCapturer();
    }

}
