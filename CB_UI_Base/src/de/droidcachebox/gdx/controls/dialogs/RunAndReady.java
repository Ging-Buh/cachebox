package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;

public abstract class RunAndReady implements Runnable {

    private Thread mRunThread;
    private boolean isCanceled = false;
    private boolean isRunning = false;

    public RunAndReady() {
    }

    public abstract void ready(boolean isCanceled);

    public void doStart(CB_View_Base dialog) {
        if (!isRunning) {
            isRunning = true;
            mRunThread = new Thread(() -> {
                RunAndReady.this.run();
                GL.that.closeDialog(dialog);
                RunAndReady.this.ready(isCanceled);
            });
            mRunThread.start();
        }
    }

    /**
     * the RunAndReady run-method can test with Thread.interrupted()
     * but I prefer the way by using indication with setIsCanceled
     *
     */
    public void doInterrupt() {
        isCanceled = true;
        if (mRunThread != null)
            mRunThread.interrupt();
    }

    public abstract void setIsCanceled();
}
