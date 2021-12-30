package de.droidcachebox.utils;

public abstract class RunAndReady implements Runnable {

    private Thread mRunThread;
    private boolean isCanceled = false;
    private boolean isRunning = false;

    public RunAndReady() {
    }

    public abstract void ready(boolean isCanceled);

    public void doStart() {
        if (!isRunning) {
            isRunning = true;
            mRunThread = new Thread(() -> {
                RunAndReady.this.run();
                RunAndReady.this.ready(isCanceled);
            });
            mRunThread.start();
        }
    }

    /**
     * the RunAndReady run-method can test with Thread.interrupted()
     *
     */
    public void doInterrupt() {
        isCanceled = true;
        if (mRunThread != null)
            mRunThread.interrupt();
    }

    public abstract void setIsCanceled();
}
