package de.droidcachebox.utils;

import de.droidcachebox.utils.log.Log;

public abstract class LoopThread {
    private static final String sKlasse = "LoopThread";

    private final long sleepTime;
    private boolean loopShouldRun;
    private Thread loopThread;
    private Thread monitoringThread;

    public LoopThread(long LoopBreakTime) {
        super();
        sleepTime = LoopBreakTime;
        loopShouldRun = false;
    }

    protected abstract void loop();

    protected abstract boolean cancelLoop();

    public void start() {
        if (loopThread == null) {

            loopThread = new Thread(() -> {
                do {
                    loopShouldRun = true;
                    if (cancelLoop()) {
                        loopShouldRun = false;
                        loopThread = null;
                    } else {
                        loop();
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException ignored) {
                        }
                    }
                } while (loopShouldRun);
                if (monitoringThread != null) monitoringThread.interrupt();
                monitoringThread = null;
                Log.debug(sKlasse, "Stop loopThread");
            });

            try {
                loopThread.start();
                // wait until loopThreads runnable is started
                do {
                    Thread.sleep(1000);
                } while (!loopShouldRun);

                if (monitoringThread == null) {

                    monitoringThread = new Thread(() -> {
                        do {
                            if (loopShouldRun) {
                                Log.debug(sKlasse, "MonitoringThread is checking!");
                                if (loopThread.isAlive()) {
                                    try {
                                        Thread.sleep(10000); // must not run that often
                                    } catch (InterruptedException ignored) {
                                        Log.debug(sKlasse, "Waking up monitoringThread");
                                    }
                                } else {
                                    // both threads will finish
                                    loopShouldRun = false;
                                    loopThread = null;
                                    monitoringThread = null;
                                    start(); // restarts both (if loop() is hanging
                                }
                            }
                        } while (loopShouldRun);
                        Log.debug(sKlasse, "Stop monitoringThread");
                    });

                    monitoringThread.setPriority(Thread.MIN_PRIORITY);
                    monitoringThread.start();
                }
            } catch (Exception ex) {
                Log.err(sKlasse, "monitoringThread: " + ex);
            }

        }
    }

}