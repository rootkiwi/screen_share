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
import stats.EmbeddedStatsUpdater;

public class WebServerProvider {

    public static EmbeddedWebServer getEmbeddedWebserver(EmbeddedStatsUpdater statsUpdater,
                                                         FrameQueueFiller queueFiller,
                                                         LogWriter logWriter) {
        return new JettyEmbeddedWebServer(statsUpdater, queueFiller, logWriter);
    }

}
