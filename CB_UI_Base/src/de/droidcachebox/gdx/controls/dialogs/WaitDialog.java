package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.gdx.controls.animation.WorkAnimation;

public class WaitDialog extends ButtonDialog {
    // private static final String sClass = "WaitDialog";
    private final RunAndReady runAndReady;

    /**
     *
     * @param msg this text is shown
     * @param runAndReady this job is done. WaitDialog closes automatically when job is ready
     */
    public WaitDialog(String msg, RunAndReady runAndReady) {
        super(msg, "", MsgBoxButton.NOTHING, MsgBoxIcon.Asterisk);
        // using MsgBoxIcon.Asterisk as placeholder to generate the iconImage
        removeChild(iconImage);
        addChild(new WorkAnimation(iconImage));
        this.runAndReady = runAndReady;
    }

    @Override
    public void onShow() {
        if (runAndReady != null) runAndReady.doStart(this);
    }

}
