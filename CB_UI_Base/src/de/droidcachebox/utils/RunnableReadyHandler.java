package de.droidcachebox.utils;

/**
 * Extends ICancelRunnable with the abstract RunnableIsReady(isCanceled)
 *
 * @author Longri
 */
public abstract class RunnableReadyHandler implements TestCancelRunnable {

    private Thread mRunThread;
    private boolean isCanceled = false;
    private boolean isRunning = false;

    public RunnableReadyHandler() {
    }

    public abstract void runnableIsReady(boolean isCanceled);

    public void start() {
        if (!isRunning) {
            isRunning = true;
            mRunThread = new Thread(() -> {
                this.run();
                runnableIsReady(isCanceled);
            });
            mRunThread.start();
        }
    }

    /*
     * the thread of the runnable is canceled!
     */
    public void Cancel() {
        isCanceled = true;
        if (mRunThread != null)
            mRunThread.interrupt();
    }

}
