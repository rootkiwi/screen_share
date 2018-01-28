/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package config;

import log.LogWriter;

public interface ConfigHandler {

    /**
     * Get the saved settings if exists, otherwise default settings
     * @return the settings
     */
    Settings getSettings(LogWriter logWriter);

    void saveToDisk(Settings settings);
    void deleteIfExists();

}
