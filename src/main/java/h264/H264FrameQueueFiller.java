/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package h264;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class H264FrameQueueFiller implements H264FrameQueueFillerController, FrameQueueFiller, RectangleUpdater {

    private List<BlockingQueue<ByteBuffer>> connectionQueues = new ArrayList<>();
    private H264FrameCapturer h264FrameCapturer = H264FrameCapturerProvider.getH264FrameCapturer();
    private AtomicBoolean forceKeyFrame = new AtomicBoolean();
    private boolean shareFullscreen = true;
    private Rectangle rectRoiUpdatedByUser;
    private Rectangle roiTmpCopy;
    private Thread workerThread;
    private boolean cancel = false;

    public H264FrameQueueFiller() {
        rectRoiUpdatedByUser = new Rectangle();
        roiTmpCopy = new Rectangle();
    }

    @Override
    public void start(int fps, int crf, int maxrateMbits) {
        h264FrameCapturer.setFps(fps);
        h264FrameCapturer.setCrf(crf);
        h264FrameCapturer.setMaxrateMbits(maxrateMbits);
        cancel = false;
        workerThread = new Thread(this::worker);
        workerThread.start();
    }

    @Override
    public void stop() {
        try {
            workerThread.interrupt();
        } catch (Exception e) {}
        cancel = true;
        synchronized (this) {
            connectionQueues.clear();
        }
        h264FrameCapturer.stop();
    }

    @Override
    public synchronized void addQueue(BlockingQueue<ByteBuffer> queue) {
        connectionQueues.add(queue);
        forceKeyFrame.set(true);
        notifyAll();
    }

    @Override
    public synchronized void removeQueue(BlockingQueue<ByteBuffer> queue) {
        connectionQueues.remove(queue);
    }

    @Override
    public void forceKeyFrame() {
        forceKeyFrame.set(true);
    }

    private void worker() {
        FrameHolder frameHolder = new FrameHolder();
        ByteBuffer buf;
        final int RESOLUTION_BYTE_SIZE = 4;
        while (!cancel) {
            try {
                waitIfNoConnections();
            } catch (InterruptedException e) {
                return;
            }

            if (shareFullscreen) {
                h264FrameCapturer.getFrameFullscreen(frameHolder, forceKeyFrame.compareAndSet(true, false));
            } else {
                roiTmpCopy.setRect(rectRoiUpdatedByUser);
                h264FrameCapturer.getFrameRoi(frameHolder, forceKeyFrame.compareAndSet(true, false), roiTmpCopy);
            }

            buf = ByteBuffer.allocate(frameHolder.frameBytes.length + RESOLUTION_BYTE_SIZE);
            buf.putShort(frameHolder.frameWidth);
            buf.putShort(frameHolder.frameHeight);
            buf.put(frameHolder.frameBytes);
            buf.flip();

            synchronized (this) {
                for (BlockingQueue<ByteBuffer> networkQueue : connectionQueues) {
                    ByteBuffer duplicate = buf.duplicate();
                    boolean queueIsFull = !networkQueue.offer(duplicate);
                    if (queueIsFull) {
                        networkQueue.clear();
                        networkQueue.add(duplicate);
                    }
                }
            }
        }
    }

    private synchronized void waitIfNoConnections() throws InterruptedException {
        while (connectionQueues.size() == 0) {
            h264FrameCapturer.stop();
            wait();
        }
    }

    @Override
    public void useFullscreen() {
        shareFullscreen = true;
    }

    @Override
    public void useRectangle() {
        shareFullscreen = false;
    }

    @Override
    public void updateRectangleX(int x) {
        rectRoiUpdatedByUser.x = x;
    }

    @Override
    public void updateRectangleY(int y) {
        rectRoiUpdatedByUser.y = y;
    }

    @Override
    public void updateRectangleWidth(int w) {
        rectRoiUpdatedByUser.width = w;
    }

    @Override
    public void updateRectangleHeight(int h) {
        rectRoiUpdatedByUser.height = h;
    }

}
