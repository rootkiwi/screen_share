/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package remote;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

class Sha256Helper {

    static String getSha256Fingerprint(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update(bytes);
        byte[] hash = sha256.digest();
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02X", b);
        }
        return formatter.toString();
    }

}
