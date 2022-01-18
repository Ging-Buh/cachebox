package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.animation.AnimationBase;
import de.droidcachebox.gdx.controls.animation.WorkAnimation;

public class WaitDialog extends ButtonDialog {
    // private static final String sClass = "WaitDialog";
    AnimationBase animation;

    public WaitDialog(String msg) {
        super(msg, "", MsgBoxButton.NOTHING, MsgBoxIcon.Asterisk);
        // using MsgBoxIcon.Asterisk as placeholder to generate the iconImage
        animation = new WorkAnimation(iconImage);
        removeChild(iconImage);
        addChild(animation);
    }

    public void closeWaitDialog() {
        GL.that.runOnGLWithThreadCheck(() -> {
            GL.that.closeDialog(WaitDialog.this);
            GL.that.renderOnce();
        });
    }

}
