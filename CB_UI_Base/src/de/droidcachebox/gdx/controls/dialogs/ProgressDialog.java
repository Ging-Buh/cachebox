package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.ProgressBar;
import de.droidcachebox.gdx.controls.animation.AnimationBase;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.RunnableReadyHandler;

public class ProgressDialog extends MsgBox {
    private final CB_Label messageTextView;
    private final CB_Label progressMessageTextView;
    private final ProgressBar progressBar;
    private final RunnableReadyHandler runnableReadyHandler;
    public float measuredLabelHeight;
    private AnimationBase animation;
    private ICancelListener mCancelListener;

    public ProgressDialog(String title, AnimationBase animation, RunnableReadyHandler runnableReadyHandler) {
        super(calcMsgBoxSize(title, true, true, true), title);

        addButtons(MsgBoxButton.Cancel);
        btnRightNegative.setClickHandler((view, x, y, pointer, button) -> {
            runnableReadyHandler.cancel();
            btnRightNegative.disable();
            btnRightNegative.setText(Translation.get("waitForCancel"));
            if (mCancelListener != null)
                mCancelListener.setIsCanceled();
            return true;
        });

        measuredLabelHeight = Fonts.Measure("T").height * 1.5f;

        progressMessageTextView = new CB_Label(this.name + " progressMessageTextView", leftBorder, margin, innerWidth, measuredLabelHeight);
        this.addChild(progressMessageTextView);

        CB_RectF rec = new CB_RectF(0, progressMessageTextView.getMaxY() + margin, this.getContentSize().getWidth(), UiSizes.getInstance().getButtonHeight() * 0.75f);

        progressBar = new ProgressBar(rec);
        progressBar.fillBarAt(0);
        this.addChild(progressBar);

        messageTextView = new CB_Label(this.name + " messageTextView", leftBorder, progressBar.getMaxY() + margin, innerWidth, measuredLabelHeight);
        this.addChild(messageTextView);

        this.runnableReadyHandler = runnableReadyHandler;
        float heightForAnimation;
        if (animation == null) {
            heightForAnimation = 0;
        } else {
            heightForAnimation = UiSizes.getInstance().getButtonHeight() >> 1;
            setAnimation(animation);
        }
        setHeight(getHeight() + (measuredLabelHeight * 2f) + heightForAnimation);
        setTitle(title);

    }

    public void setCancelListener(ICancelListener listener) {
        mCancelListener = listener;
    }

    public void setAnimation(final AnimationBase animation) {
        GL.that.RunOnGL(() -> {
            removeChild(this.animation);
            CB_RectF imageRec = new CB_RectF(0, progressBar.getMaxY() + margin, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
            this.animation = animation;
            this.animation.setRec(imageRec);
            addChild(this.animation);
        });

    }

    @Override
    public void onShow() {
        if (runnableReadyHandler != null) runnableReadyHandler.start();
    }

    public void setProgress(final String msg, final String progressMessage, final int value) {
        GL.that.RunOnGL(() -> {
            if (ProgressDialog.this.isDisposed())
                return;
            progressBar.fillBarAt(value);
            progressMessageTextView.setText(progressMessage);
            if (!msg.equals(""))
                messageTextView.setText(msg);
        });
    }

    public interface ICancelListener {
        void setIsCanceled();
    }

}
