/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package remote;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

class RemoteConnection {

    private OutputStream out;
    private BlockingQueue<ByteBuffer> h264Frames;
    private RemoteConnectionCallback callback;
    private Thread workThread;
    private boolean cancel = false;

    RemoteConnection(OutputStream out,
                     BlockingQueue<ByteBuffer> h264Frames,
                     RemoteConnectionCallback callback) {
        this.out = out;
        this.h264Frames = h264Frames;
        this.callback = callback;
    }

    void start() {
        workThread = new Thread(this::work);
        workThread.start();
    }

    private void work() {
        byte[] frame;
        ByteBuffer size = ByteBuffer.allocate(4);
        while (!cancel) {
            try {
                try {
                    frame = h264Frames.take().array();
                } catch (InterruptedException e) {
                    continue;
                }
                size.clear();
                out.write(size.putInt(frame.length).array());
                out.write(frame);
            } catch (Exception e) {
                break;
            }
        }
        callback.remoteDisconnected();
    }

    void cancel() {
        cancel = true;
        try {
            workThread.interrupt();
        } catch (Exception e) {}
    }

}
