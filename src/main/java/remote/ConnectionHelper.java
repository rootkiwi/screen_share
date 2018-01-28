/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package remote;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;

class ConnectionHelper {

    static byte[] readAll(int size, InputStream in) throws IOException {
        byte[] buf = new byte[size];
        int len = 0;
        int tmp;
        while (len < size){
            tmp = in.read(buf, len, size-len);
            if (tmp == -1){
                throw new SocketException();
            } else {
                len += tmp;
            }
        }
        return buf;
    }

    static int byteArrayToInt(byte[] value){
        return ByteBuffer.wrap(value).getInt();
    }

    static byte[] intToByteArray(int value) {
        return  ByteBuffer.allocate(4).putInt(value).array();
    }

}
