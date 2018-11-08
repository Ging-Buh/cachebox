package CB_UI_Base.GL_UI.interfaces;

import CB_Utils.Interfaces.ICancelRunnable;

/**
 * Extends ICancelRunnable with the abstract RunnableIsReady(isCanceled)
 *
 * @author Longri
 */
public abstract class RunnableReadyHandler implements ICancelRunnable {

    private Thread mRunThread;
    private boolean isCanceled = false;
    private boolean isRunning = false;

    public RunnableReadyHandler() {
    }

    public abstract void RunnableIsReady(boolean isCanceled);

    public void start() {
        if (!isRunning) {
            isRunning = true;
            mRunThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    RunnableReadyHandler.this.run();
                    RunnableIsReady(isCanceled);
                }
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
