/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package h264;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

public interface FrameQueueFiller {

    void addQueue(BlockingQueue<ByteBuffer> queue);
    void removeQueue(BlockingQueue<ByteBuffer> queue);
    void forceKeyFrame();

}
