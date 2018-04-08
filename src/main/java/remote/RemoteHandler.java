/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package remote;

import log.LogWriter;
import h264.FrameQueueFiller;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.concurrent.*;

import static remote.ConnectionHelper.intToByteArray;
import static remote.ConnectionHelper.readAll;
import static remote.RemoteMessageTypes.FIRST_NEW_CONNECTION;
import static remote.RemoteMessageTypes.NEW_CONNECTION;
import static remote.RemoteMessageTypes.ZERO_CONNECTIONS;
import static remote.TlsHelper.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384;
import static remote.TlsHelper.TLSv12;

public class RemoteHandler implements RemoteConnectionHandler, RemoteConnectionCallback {

    private RemoteCallback callback;
    private FrameQueueFiller queueFiller;
    private RemoteConnection remoteConnection;
    private BlockingQueue<ByteBuffer> h264Frames;
    private String remoteCertificateFingerprint;
    private boolean handshakeCompleted;
    private SSLSocket tlsSocket;
    private boolean stop = false;

    public RemoteHandler(FrameQueueFiller queueFiller, RemoteCallback callback) {
        this.queueFiller = queueFiller;
        this.callback = callback;
    }

    @Override
    public void connect(String host, int port, String password, String fingerprint, String pageTitle) {
        stop = false;
        remoteCertificateFingerprint = "";
        handshakeCompleted = false;
        new Thread(() -> tryToConnect(host, port, password, fingerprint, pageTitle)).start();
    }

    private void tryToConnect(String host, int port, String password, String fingerprint, String pageTitle) {
        try {
            tlsSocket = (SSLSocket) TlsHelper.getRemoteTlsContext().getSocketFactory().createSocket();
            tlsSocket.setUseClientMode(true);
            tlsSocket.setEnabledProtocols(TLSv12);
            tlsSocket.setEnabledCipherSuites(TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384);
        } catch (Exception e) {
            callback.remoteFailedToConnect("remote error creating socket: " + e.getMessage());
            return;
        }
        InputStream in;
        OutputStream out;
        try {
            tlsSocket.connect(new InetSocketAddress(host, port), 5000);
            in = tlsSocket.getInputStream();
            out = tlsSocket.getOutputStream();
            tlsSocket.setSoTimeout(5000);
        } catch (IOException e) {
            callback.remoteFailedToConnect("remote failed to connect: " + e.getMessage());
            closeSocket();
            return;
        }

        registerHandshakeCompletedCallback(tlsSocket);
        try {
            tlsSocket.startHandshake();
        } catch (IOException e) {
            callback.remoteFailedToConnect("remote TLS handshake failed: " + e.getMessage());
            closeSocket();
            return;
        }

        synchronized (this) {
            while (!handshakeCompleted) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.out.println("InterruptedException" + e.getMessage());
                    return;
                }
            }
        }

        if (!remoteCertificateFingerprint.equals(fingerprint)) {
            callback.remoteFailedToConnect("remote fingerprint does not match: " + remoteCertificateFingerprint);
            closeSocket();
            return;
        }

        if (!isScreenShareServer(in)) {
            callback.remoteFailedToConnect("remote connected to something, "
                    + "but does not seem to be a screen_share_remote server");
            closeSocket();
            return;
        }

        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        try {
            out.write(intToByteArray(passwordBytes.length));
            out.write(passwordBytes);
        } catch (IOException e) {
            callback.remoteFailedToConnect("socket closed: " + e.getMessage());
            return;
        }

        boolean passwordAccepted;
        try {
            passwordAccepted = in.read() == 1;
            if (!passwordAccepted) {
                callback.remoteFailedToConnect("wrong remote password");
                closeSocket();
                return;
            } else {
                callback.remoteConnected();
            }
        } catch (IOException e) {
            callback.remoteFailedToConnect("error waiting for password verification: " + e.getMessage());
            closeSocket();
            return;
        }

        try {
            byte[] pageTitleBytes = pageTitle.getBytes(StandardCharsets.UTF_8);
            out.write(intToByteArray(pageTitleBytes.length));
            out.write(pageTitleBytes);
        } catch (IOException e) {
            callback.remoteFailedToConnect("error sending page title: " + e.getMessage());
            return;
        }

        h264Frames = new ArrayBlockingQueue<>(80);
        remoteConnection = new RemoteConnection(out, h264Frames, this);
        remoteConnection.start();

        try {
            tlsSocket.setSoTimeout(0);
            while (!stop) {
                switch (in.read()) {
                    case FIRST_NEW_CONNECTION:
                        queueFiller.addQueue(h264Frames);
                        break;
                    case NEW_CONNECTION:
                        queueFiller.forceKeyFrame();
                        break;
                    case ZERO_CONNECTIONS:
                        queueFiller.removeQueue(h264Frames);
                        h264Frames.clear();
                        break;
                    case -1:
                        throw new SocketException("socket closed");
                }
            }
        } catch (IOException e) {
            disconnect();
            callback.remoteDisconnected();
        }
    }

    private void registerHandshakeCompletedCallback(SSLSocket tlsSocket) {
        tlsSocket.addHandshakeCompletedListener(event -> {
            try {
                Certificate remoteCert = event.getPeerCertificates()[0];
                remoteCertificateFingerprint = Sha256Helper.getSha256Fingerprint(remoteCert.getEncoded());
            } catch (Exception e) {
                remoteCertificateFingerprint = "remote fingerprint error: " + e.getMessage();
            }
            synchronized (this) {
                handshakeCompleted = true;
                notify();
            }
        });
    }

    /**
     * Not used for security or anything, just so we can close if remote to for example some random webserver.<br/>
     * For security fingerprints should be compared.
     */
    private boolean isScreenShareServer(InputStream in) {
        try {
            final byte[] expected = "im_a_screen_share_remote_server_i_promise".getBytes(StandardCharsets.UTF_8);
            byte[] actual = readAll(expected.length, in);
            return Arrays.equals(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void disconnect() {
        try {
            remoteConnection.cancel();
        } catch (Exception e) {}
        stop();
    }

    @Override
    public void remoteDisconnected() {
        stop();
    }

    private void stop() {
        stop = true;
        closeSocket();
        queueFiller.removeQueue(h264Frames);
    }

    private void closeSocket() {
        try {
            tlsSocket.close();
        } catch (Exception e) {}
    }

}
