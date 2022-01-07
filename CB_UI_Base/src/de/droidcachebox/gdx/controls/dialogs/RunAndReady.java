package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.utils.ProgressChangedEvent;

public abstract class RunAndReady implements Runnable {

    private Thread mRunThread;
    private boolean isRunning = false;
    private ProgressChangedEvent progressChangedEvent;

    public RunAndReady() {
    }

    public abstract void ready();

    public void doStart(CB_View_Base dialog) {
        if (!isRunning) {
            isRunning = true;
            mRunThread = new Thread(() -> {
                RunAndReady.this.run();
                GL.that.closeDialog(dialog);
                GL.that.renderOnce();
                RunAndReady.this.ready();
            });
            mRunThread.start();
        }
    }

    /**
     * the RunAndReady run-method can test with Thread.interrupted() (if this would be called instead of setIsCanceled)
     * but I prefer the way by using indication with setIsCanceled
     */
    public void doInterrupt() {
        if (mRunThread != null)
            mRunThread.interrupt();
    }

    /**
     * used by the Cancel and Progress Dialog to inform the caller about click on the cancel button
     */
    public abstract void setIsCanceled();

    /**
     * the runnable (and ready) in the ProgressDialog call this method for indication of progress
     * the
     *
     * @param msg the id text
     * @param progressMessage the text for the increment
     * @param value to fill the progressbar in percent
     */
    public void changeProgress(final String msg, final String progressMessage, final int value) {
        if (progressChangedEvent != null)
            progressChangedEvent.changedProgress(msg, progressMessage, value);
    }

    public void setProgressChangedListener(ProgressChangedEvent progressChangedEvent) {
        this.progressChangedEvent = progressChangedEvent;
    }

}
