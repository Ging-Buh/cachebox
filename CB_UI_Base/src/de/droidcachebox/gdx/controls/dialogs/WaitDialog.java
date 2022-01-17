package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.animation.AnimationBase;
import de.droidcachebox.gdx.controls.animation.WorkAnimation;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

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

    public void setAnimation(final AnimationBase animation) {
        GL.that.runOnGL(() -> {
            removeChild(this.animation);
            this.animation = animation;
            CB_RectF imageRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
            animation.setRec(imageRec);
            addChild(animation);
        });

    }

    public void closeWaitDialog() {
        GL.that.runOnGL(() -> {
            GL.that.closeDialog(WaitDialog.this);
            GL.that.renderOnce();
        });
    }


    @Override
    public void onShow() {

    }


}
