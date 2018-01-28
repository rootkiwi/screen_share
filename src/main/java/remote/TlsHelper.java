/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package remote;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

public class TlsHelper {

    static final String[] TLSv12 = new String[]{"TLSv1.2"};
    static final String[] TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 = new String[]{"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"};

    static SSLContext getRemoteTlsContext() throws GeneralSecurityException {
        SSLContext tlsContext = SSLContext.getInstance(TLSv12[0]);
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        tlsContext.init(null, trustAllCerts, null);
        return tlsContext;
    }

}
