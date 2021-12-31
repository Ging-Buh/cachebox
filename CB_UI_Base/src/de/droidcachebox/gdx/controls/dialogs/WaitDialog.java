package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.VAlignment;
import de.droidcachebox.gdx.controls.animation.AnimationBase;
import de.droidcachebox.gdx.controls.animation.WorkAnimation;
import de.droidcachebox.gdx.controls.messagebox.ButtonDialog;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;

public class WaitDialog extends ButtonDialog {
    // private static final String sClass = "WaitDialog";
    AnimationBase animation;

    public WaitDialog(String msg, Size size, String name) {
        super(size.getBounds().asFloat(), name, "", "", null, null, null);

        setTitle("");

        // the msg on a label
        SizeF contentSize = getContentSize();
        label = new CB_Label(contentSize.getBounds());
        label.setWidth(contentSize.getBounds().getWidth() - margin - margin - margin - UiSizes.getInstance().getButtonHeight());
        CB_RectF imageRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
        label.setX(imageRec.getMaxX() + margin);
        label.setWrappedText(msg);
        label.setY(0);
        int lineCount = label.getLineCount();
        if (lineCount == 1) {
            label.setText(msg);
            label.setVAlignment(VAlignment.CENTER);
        } else {
            label.setVAlignment(VAlignment.TOP);
        }
        addChild(label);

        animation = new WorkAnimation(imageRec);
        float imageYPos = (contentSize.getHeight() < (animation.getHeight() * 1.7)) ? contentSize.getHalfHeight() - animation.getHalfHeight() : contentSize.getHeight() - animation.getHeight() - margin;
        animation.setY(imageYPos);
        addChild(animation);

        setButtonCaptions(MsgBoxButton.NOTHING);

    }

    public WaitDialog(String msg) {
        this(msg, calcMsgBoxSize(msg, false, false, true, false), "WaitDialog");
    }

    public void setAnimation(final AnimationBase animation) {
        GL.that.RunOnGL(() -> {
            removeChild(this.animation);
            this.animation = animation;
            CB_RectF imageRec = new CB_RectF(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
            animation.setRec(imageRec);
            addChild(animation);
        });

    }

    public void dismis() {
        GL.that.RunOnGL(() -> {
            GL.that.closeDialog(WaitDialog.this);
            GL.that.renderOnce();
        });
    }

}
