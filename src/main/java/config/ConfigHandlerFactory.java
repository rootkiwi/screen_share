/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package config;

public class ConfigHandlerFactory {

    private ConfigHandlerFactory() {
    }

    public static ConfigHandler getConfigHandler() {
        return new JavaXMLPropertiesConfigHandler();
    }

}
