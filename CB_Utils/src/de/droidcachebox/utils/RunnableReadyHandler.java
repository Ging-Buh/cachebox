package de.droidcachebox.utils;

/**
 * Extends TestCancelRunnable with the abstract ready(isCanceled)
 * afterRun is called after the TestCancelRunnable is executed
 *
 * @author Longri
 */
public abstract class RunnableReadyHandler implements TestCancelRunnable {

    private Thread mRunThread;
    private boolean isCanceled = false;
    private boolean isRunning = false;

    public RunnableReadyHandler() {
    }

    public abstract void ready(boolean isCanceled);

    public void doStart() {
        if (!isRunning) {
            isRunning = true;
            mRunThread = new Thread(() -> {
                RunnableReadyHandler.this.run(); // TestCancelRunnable
                RunnableReadyHandler.this.ready(isCanceled);
            });
            mRunThread.start();
        }
    }

    /**
     * the RunnableReadyHandlers run-method can test with Thread.interrupted()
     * that is a kind of overkill in cancellation,
     * because the calling class of the run-method can have an indicating variable (AtomicBoolean isCanceled) for that
     *
     * usages in: CancelWaitDialogs-ShowWait and in ProgressDialog
     */
    public void doInterrupt() {
        isCanceled = true;
        if (mRunThread != null)
            mRunThread.interrupt();
    }

}
