package CB_UI_Base.GL_UI.interfaces;

import CB_Utils.Interfaces.cancelRunnable;

/**
 * Extends Runnable um eine Ready Meldung
 *
 * @author Longri
 */
public abstract class RunnableReadyHandler implements cancelRunnable {

    // cancelRunable mRunnable;
    Thread mRunThread;
    boolean isCanceld = false;
    boolean isRunning = false;

    public RunnableReadyHandler() {
        // mRunnable = runnable;
    }

    public abstract void RunnableReady(boolean canceld);

    public void start() {
        if (!isRunning) {
            isRunning = true;
            mRunThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    RunnableReadyHandler.this.run();
                    RunnableReady(isCanceld);
                }
            });
            mRunThread.start();
        }
    }

    /*
     * Bricht den Thread, in dem das Runnable l�uft ab!
     */
    public void Cancel() {
        isCanceld = true;
        if (mRunThread != null)
            mRunThread.interrupt();
    }

}
