/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package embedded;

import log.LogWriter;
import h264.FrameQueueFiller;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import stats.EmbeddedStatsUpdater;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@WebSocket
public class WebSocketHandler {

    private static Map<Session, WebSocketConnection> connections = new HashMap<>();

    private EmbeddedStatsUpdater statsUpdater;
    private FrameQueueFiller queueFiller;
    private LogWriter logWriter;
    private String ipAddress;

    WebSocketHandler(EmbeddedStatsUpdater statsUpdater, FrameQueueFiller queueFiller, LogWriter logWriter) {
        this.statsUpdater = statsUpdater;
        this.queueFiller = queueFiller;
        this.logWriter = logWriter;
    }

    @OnWebSocketConnect
    public void connected(Session session) {
        BlockingQueue<ByteBuffer> h264Frames = new ArrayBlockingQueue<>(40);
        WebSocketConnection wsc = new WebSocketConnection(statsUpdater, session.getRemote(), h264Frames);
        wsc.start();
        queueFiller.addQueue(h264Frames);
        connections.put(session, wsc);
        ipAddress = session.getRemote().getInetSocketAddress().getHostString();
        logWriter.writeLogInfo(ipAddress + " connected");
        statsUpdater.newEmbeddedConnection();
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        BlockingQueue<ByteBuffer> h264Frames = connections.get(session).getH264FramesQueue();
        queueFiller.removeQueue(h264Frames);
        connections.remove(session);
        logWriter.writeLogInfo(ipAddress + " disconnected");
        statsUpdater.closedEmbeddedConnection();
    }

    static void cancelAllConnections() {
        try {
            connections.values().forEach(WebSocketConnection::cancel);
        } catch (Exception e) {}
    }

}
