package de.droidcachebox.gdx.controls.dialogs;

import com.badlogic.gdx.graphics.g2d.Batch;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.VAlignment;
import de.droidcachebox.gdx.controls.animation.AnimationBase;
import de.droidcachebox.gdx.controls.animation.WorkAnimation;
import de.droidcachebox.gdx.controls.messagebox.ButtonDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.utils.log.Trace;

public class WaitDialog extends ButtonDialog {
    private static final String log = "WaitDialog";
    AnimationBase animation;
    boolean canceld = false;

    public WaitDialog(Size size, String name) {
        super(size.getBounds().asFloat(), name, "", "", null, null, null);

    }

    public static WaitDialog ShowWait() {
        WaitDialog wd = createDialog("");
        wd.setCallerName(Trace.getCallerName());
        wd.show();
        return wd;
    }

    public static WaitDialog ShowWait(String Msg) {
        WaitDialog wd = createDialog(Msg);
        wd.setCallerName(Trace.getCallerName());
        wd.show();
        return wd;
    }

    protected static WaitDialog createDialog(String msg) {

        Size size = calcMsgBoxSize(msg, false, false, true, false);

        WaitDialog waitDialog = new WaitDialog(size, "WaitDialog");
        waitDialog.setTitle("");

        SizeF contentSize = waitDialog.getContentSize();

        CB_RectF imageRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
        waitDialog.animation = WorkAnimation.GetINSTANCE(imageRec);
        waitDialog.addChild(waitDialog.animation);

        waitDialog.label = new CB_Label(contentSize.getBounds());
        waitDialog.label.setWidth(contentSize.getBounds().getWidth() - margin - margin - margin - UiSizes.getInstance().getButtonHeight());
        waitDialog.label.setX(imageRec.getMaxX() + margin);
        waitDialog.label.setWrappedText(msg);

        int lineCount = waitDialog.label.getLineCount();
        waitDialog.label.setY(0);

        if (lineCount == 1) {
            waitDialog.label.setText(msg);
            waitDialog.label.setVAlignment(VAlignment.CENTER);
        } else {
            waitDialog.label.setVAlignment(VAlignment.TOP);
        }

        float imageYPos = (contentSize.height < (waitDialog.animation.getHeight() * 1.7)) ? contentSize.halfHeight - waitDialog.animation.getHalfHeight() : contentSize.height - waitDialog.animation.getHeight() - margin;
        waitDialog.animation.setY(imageYPos);

        waitDialog.addChild(waitDialog.label);
        waitDialog.setButtonCaptions(MessageBoxButtons.NOTHING);

        return waitDialog;

    }

    public void setAnimation(final AnimationBase Animation) {
        GL.that.RunOnGL(() -> {
            WaitDialog.this.removeChild(WaitDialog.this.animation);
            CB_RectF imageRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
            WaitDialog.this.animation = Animation.INSTANCE(imageRec);
            WaitDialog.this.addChild(WaitDialog.this.animation);
        });

    }

    public void dismis() {
        Log.debug(log, "WaitDialog.Dismis");
        GL.that.RunOnGL(() -> {
            GL.that.closeDialog(WaitDialog.this);
            GL.that.renderOnce();
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        String caller = Trace.getCallerName(1);
        Log.debug(log, "WaitDialog.disposed ID:[" + this.DialogID + "] called:" + caller);
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }

    @Override
    public String toString() {
        return getName() + "DialogID[" + DialogID + "] \"" + this.label.getText() + "\" Created by: " + CallerName;
    }

}
