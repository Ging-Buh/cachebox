package de.droidcachebox.utils;

/**
 * Extends TestCancelRunnable with the abstract runnableIsReady(isCanceled)
 * runnableIsReady is called after the TestCancelRunnable is executed
 *
 * @author Longri
 */
public abstract class RunnableReadyHandler implements TestCancelRunnable {

    private Thread mRunThread;
    private boolean isCanceled = false;
    private boolean isRunning = false;

    public RunnableReadyHandler() {
    }

    public abstract void afterRun(boolean isCanceled);

    public void start() {
        if (!isRunning) {
            isRunning = true;
            mRunThread = new Thread(() -> {
                RunnableReadyHandler.this.run(); // the TestCancelRunnable
                RunnableReadyHandler.this.afterRun(isCanceled);
            });
            mRunThread.start();
        }
    }

    /*
     * the thread of the runnable is canceled!
     */
    public void cancel() {
        isCanceled = true;
        if (mRunThread != null)
            mRunThread.interrupt();
    }

}
