/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package log;

public interface LogWriter {

    void writeLogInfo(String message);
    void writeLogLink(String message);
    void writeLogError(String message);

}
