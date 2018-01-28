/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package stats;

public interface EmbeddedStatsUpdater {

    void newEmbeddedConnection();
    void closedEmbeddedConnection();
    void embeddedBytesTransferred(int numOfBytes);

}
