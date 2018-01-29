/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package h264;

import monitor.VirtualScreenBoundingBox;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import monitor.MonitorInfo;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static build.BuildInfo.OPERATIVE_SYSTEM;

public class FFmpegH264FrameCapturer implements H264FrameCapturer {

    private int fps, crf, maxrateMbits;
    private Rectangle rectangleFullScreen;
    private int lowerRightX, lowerRightY;
    private int positiveTopLeftX, positiveTopLeftY;
    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder recorder;
    private FFmpegFrameFilter cropFilter;
    private ByteArrayOutputStream recorderOutput = new ByteArrayOutputStream();
    private int grabberPixelFormat;
    private int lastWidth, lastHeight, lastX, lastY;
    private boolean started = false;

    FFmpegH264FrameCapturer() {
        VirtualScreenBoundingBox boundingBox = MonitorInfo.getVirtualScreenBoundingBox();
        rectangleFullScreen = new Rectangle(
                boundingBox.topLeftX, boundingBox.topLeftY,
                boundingBox.width, boundingBox.height
        );
        positiveTopLeftX = -boundingBox.topLeftX;
        positiveTopLeftY = -boundingBox.topLeftY;
        lowerRightX = boundingBox.lowerRightX;
        lowerRightY = boundingBox.lowerRightY;
        validateAndFixRoi(rectangleFullScreen);
        // https://trac.ffmpeg.org/wiki/Capture/Desktop
        if (OPERATIVE_SYSTEM.isLinux()) {
            // https://www.ffmpeg.org/ffmpeg-devices.html#x11grab
            String display = System.getenv("DISPLAY");
            if (display == null) {
                display = ":0";
            }
            grabber = new FFmpegFrameGrabber(display + "+" + rectangleFullScreen.x + "," + rectangleFullScreen.y);
            grabber.setFormat("x11grab");
        } else if (OPERATIVE_SYSTEM.isMacOs()) {
            // https://www.ffmpeg.org/ffmpeg-devices.html#avfoundation
            grabber = new FFmpegFrameGrabber("default");
            grabber.setFormat("avfoundation");
            grabber.setOption("-capture_cursor", "1");
        } else {
            // https://www.ffmpeg.org/ffmpeg-devices.html#gdigrab
            grabber = new FFmpegFrameGrabber("desktop");
            grabber.setFormat("gdigrab");
            grabber.setOption("offset_x", String.valueOf(rectangleFullScreen.x));
            grabber.setOption("offset_y", String.valueOf(rectangleFullScreen.y));
        }
        grabber.setImageWidth((int) rectangleFullScreen.getWidth());
        grabber.setImageHeight((int) rectangleFullScreen.getHeight());
        grabberPixelFormat = grabber.getPixelFormat();
        cropFilter = new FFmpegFrameFilter(null, rectangleFullScreen.width, rectangleFullScreen.height);
    }

    private void setupNewRecorder(Rectangle roi) {
        recorder = new FFmpegFrameRecorder(recorderOutput, roi.width, roi.height, 0);
        recorder.setFrameRate(fps);
        recorder.setFormat("h264");
        recorder.setVideoCodecName("libx264");
        recorder.setVideoOption("profile", "baseline");
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("maxrate", String.valueOf(1024 * 1024 * maxrateMbits));
        recorder.setVideoOption("bufsize", String.valueOf(1024 * 1024 * maxrateMbits));
        recorder.setVideoOption("crf", String.valueOf(crf));
        recorder.setVideoOption("g", String.valueOf(fps * 10));
    }

    @Override
    public void setFps(int fps) {
        this.fps = fps;
    }

    @Override
    public void setCrf(int crf) {
        this.crf = crf;
    }

    @Override
    public void setMaxrateMbits(int maxrateMbits) {
        this.maxrateMbits = maxrateMbits;
    }

    @Override
    public synchronized void stop() {
        if (started) {
            try {
                grabber.stop();
            } catch (Exception e) {}
            try {
                recorder.stop();
            } catch (Exception e) {}
            try {
                cropFilter.stop();
            } catch (Exception e) {}
            recorder = null;
            started = false;
            lastWidth = 0;
            lastHeight = 0;
            lastX = 0;
            lastY = 0;
            recorderOutput.reset();
        }
    }

    private boolean roiChanged(Rectangle roi) {
        return resolutionChanged(roi) || lastX != roi.x || lastY != roi.y;
    }

    private boolean resolutionChanged(Rectangle roi) {
        return lastWidth != roi.width || lastHeight != roi.height;
    }

    private void validateAndFixRoi(Rectangle roi) {
        // x264 needs even size
        if (roi.x+roi.width > lowerRightX) {
            roi.width = lowerRightX - roi.x;
        }
        if (roi.y+roi.height > lowerRightY) {
            roi.height = lowerRightY - roi.y;
        }
        if (roi.width % 2 == 1) {
            roi.width--;
        }
        if (roi.height % 2 == 1) {
            roi.height--;
        }
    }

    @Override
    public void getFrameFullscreen(FrameHolder frameHolder, boolean forceKeyFrame) {
        getFrame(frameHolder, forceKeyFrame, rectangleFullScreen);
    }

    @Override
    public void getFrameRoi(FrameHolder frameHolder, boolean forceKeyFrame, Rectangle roi) {
        validateAndFixRoi(roi);
        getFrame(frameHolder, forceKeyFrame, roi);
    }

    private synchronized void getFrame(FrameHolder frameHolder, boolean forceKeyFrame, Rectangle roi) {
        try {
            if (!started) {
                grabber.setFrameRate(fps);
                grabber.start();
                setupNewRecorder(roi);
                recorder.start();
                started = true;
            } else if (resolutionChanged(roi) || forceKeyFrame) {
                // best solution I found for forcing a key frame/updating resolution
                // is restarting the FFmpegFrameRecorder
                // when a new client connects it sets the flag forceKeyFrame
                // otherwise the new client would not be able to see anything until up to GOP size which is 10 secs
                recorder.stop();
                setupNewRecorder(roi);
                recorder.start();
            }

            Frame frame;
            while ((frame = grabber.grabImage()) == null && started) {
                // apparently on windows lock screen grabImage() returns null, so we wait and try every second
                // or until !started anymore
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
            if (!roi.equals(rectangleFullScreen)) {
                if (roiChanged(roi)) {
                    cropFilter.setFilters("crop=w=" + roi.width + ":h=" + roi.height
                            + ":x=" + (roi.x+positiveTopLeftX) + ":y=" + (roi.y+positiveTopLeftY)
                    );
                    cropFilter.restart();
                }
                cropFilter.push(frame);
                frame = cropFilter.pull();
            }
            lastWidth = roi.width;
            lastHeight = roi.height;
            lastX = roi.x;
            lastY = roi.y;

            recorder.record(frame, grabberPixelFormat);
            frameHolder.update(
                    recorderOutput.toByteArray(),
                    (short) recorder.getImageWidth(),
                    (short) recorder.getImageHeight()
            );
            recorderOutput.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
