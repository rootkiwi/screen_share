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
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import stats.EmbeddedStatsUpdater;

class JettyEmbeddedWebServer implements EmbeddedWebServer {

    private Server webServer;
    private EmbeddedStatsUpdater statsUpdater;
    private FrameQueueFiller queueFiller;
    private LogWriter logWriter;

    JettyEmbeddedWebServer(EmbeddedStatsUpdater statsUpdater, FrameQueueFiller queueFiller, LogWriter logWriter) {
        this.statsUpdater = statsUpdater;
        this.queueFiller = queueFiller;
        this.logWriter = logWriter;
    }

    @Override
    public boolean start(int port) {
        return startWebServer(port);
    }

    @Override
    public void stop() {
        WebSocketHandler.cancelAllConnections();
        try {
            webServer.stop();
        } catch (Exception e) {}
    }

    private boolean startWebServer(int port) {
        webServer = new Server();

        ServerConnector http = new ServerConnector(webServer);
        http.setPort(port);
        webServer.addConnector(http);

        org.eclipse.jetty.websocket.server.WebSocketHandler wsHandler = new org.eclipse.jetty.websocket.server.WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.getExtensionFactory().unregister("permessage-deflate");
                factory.setCreator((req, resp) -> new WebSocketHandler(statsUpdater, queueFiller, logWriter));
            }
        };
        ContextHandler webSocketContext = new ContextHandler();
        webSocketContext.setContextPath("/ws/");
        webSocketContext.setHandler(wsHandler);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase(getClass().getClassLoader().getResource("web/static").toExternalForm());

        ContextHandler staticFilesContext = new ContextHandler();
        staticFilesContext.setContextPath("/");
        staticFilesContext.setHandler(resourceHandler);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{staticFilesContext, webSocketContext});
        webServer.setHandler(handlers);

        try {
            webServer.start();
            return true;
        } catch (Exception e) {
            writeErrorMessageToLog(e.getMessage());
            return false;
        }
    }

    private void writeErrorMessageToLog(String error) {
        logWriter.writeLogError("could not start embedded web server: " + error);
    }

}
