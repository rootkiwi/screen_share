/*
 * Copyright 2018 rootkiwi
 *
 * screen_share is licensed under GNU General Public License 3 or later.
 *
 * See LICENSE for more details.
 */

package stats;

import gui.GuiStatsUpdater;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class StatsUpdaterHandler implements EmbeddedStatsUpdater {

    private GuiStatsUpdater guiStatsUpdater;
    private AtomicInteger embeddedConnections = new AtomicInteger();
    private AtomicLong embeddedBytesTransferred = new AtomicLong();
    private AtomicBoolean isZeroEmbeddedConnections = new AtomicBoolean(true);
    private AtomicBoolean isZeroRemoteConnections = new AtomicBoolean(true);
    private Thread workerThread;
    private boolean stopped = true;

    public StatsUpdaterHandler(GuiStatsUpdater guiStatsUpdater) {
        this.guiStatsUpdater = guiStatsUpdater;
    }

    public void start() {
        stopped = false;
        embeddedBytesTransferred.set(0);
        guiStatsUpdater.setEmbeddedMegabytesTransferred(0);
        workerThread = new Thread(this::work);
        workerThread.start();
    }

    public void stop() {
        stopped = true;
        try {
            workerThread.interrupt();
        } catch (Exception e) {}
        embeddedConnections.set(0);
        guiStatsUpdater.setEmbeddedConnections(0);
        guiStatsUpdater.setEmbeddedBitsPerSecond(0);
    }

    private void work() {
        long totalBytesTransferredLastCheck = 0;
        long timeLastStatsUpdate = 0;
        while (!stopped) {
            try {
                waitIfNoConnections();
            } catch (InterruptedException e) {
                return;
            }
            long bytesTransferred = embeddedBytesTransferred.get();
            guiStatsUpdater.setEmbeddedMegabytesTransferred(bytesTransferred/1024f/1024f);
            if (timeLastStatsUpdate != 0) {
                long bitsTransferredSinceLastTime = (bytesTransferred-totalBytesTransferredLastCheck) * 8;
                float timeElapsedSeconds = (System.currentTimeMillis()-timeLastStatsUpdate) / 1000f;
                float bitsPerSecond = bitsTransferredSinceLastTime / timeElapsedSeconds;
                guiStatsUpdater.setEmbeddedBitsPerSecond(bitsPerSecond);
            }
            timeLastStatsUpdate = System.currentTimeMillis();
            totalBytesTransferredLastCheck = bytesTransferred;
            sleepOneSecond();
        }
    }

    private synchronized void waitIfNoConnections() throws InterruptedException {
        while (isZeroEmbeddedConnections.get() && isZeroRemoteConnections.get()) {
            wait();
        }
    }

    private void sleepOneSecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
    }

    @Override
    public void newEmbeddedConnection() {
        if (!stopped) {
            int connections = embeddedConnections.incrementAndGet();
            guiStatsUpdater.setEmbeddedConnections(connections);
            boolean wasZeroEmbeddedConnections = connections == 1;
            if (wasZeroEmbeddedConnections) {
                isZeroRemoteConnections.set(false);
                synchronized (this) {
                    notifyAll();
                }
            }
        }
    }

    @Override
    public void closedEmbeddedConnection() {
        if (!stopped) {
            int connections = embeddedConnections.decrementAndGet();
            guiStatsUpdater.setEmbeddedConnections(connections);
            boolean isZeroEmbeddedConnectionsNow = connections == 0;
            if (isZeroEmbeddedConnectionsNow) {
                isZeroEmbeddedConnections.set(true);
            }
        }
    }

    @Override
    public void embeddedBytesTransferred(int numOfBytes) {
        if (!stopped) {
            embeddedBytesTransferred.addAndGet(numOfBytes);
        }
    }

}
