/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package remote;

public interface RemoteCallback {

    void remoteFailedToConnect();
    void remoteConnected();
    void remoteDisconnected();

}
