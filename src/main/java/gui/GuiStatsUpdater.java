/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package gui;

public interface GuiStatsUpdater {

    void setEmbeddedConnections(int connections);
    void setEmbeddedBitsPerSecond(float bitsPerSecond);
    void setEmbeddedMegabytesTransferred(float megabytesTransferred);

}
