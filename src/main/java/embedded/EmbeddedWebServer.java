/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package embedded;

public interface EmbeddedWebServer {

    /**
     * @return Whether or not web server started successfully
     */
    boolean start(int port, String pageTitle);
    void stop();

}
