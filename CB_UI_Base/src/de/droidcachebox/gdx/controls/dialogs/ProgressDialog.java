package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.ProgressBar;
import de.droidcachebox.gdx.controls.animation.AnimationBase;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ProgressChangedEvent;
import de.droidcachebox.utils.ProgresssChangedEventList;
import de.droidcachebox.utils.RunnableReadyHandler;

import java.util.Timer;
import java.util.TimerTask;

public class ProgressDialog extends MessageBox implements ProgressChangedEvent {
    private static RunnableReadyHandler ProgressThread;
    private static String titleText;
    private static ProgressDialog that;
    public float measuredLabelHeight = 0;
    private CB_Label messageTextView;
    private CB_Label progressMessageTextView;
    private ProgressBar progressBar;
    private AnimationBase animation;
    private boolean isCanceld = false;
    private ICancelListener mCancelListener;

    public ProgressDialog(Size size, String name) {
        super(size, name);
        that = this;
        isCanceld = false;

        addButtons(MessageBoxButtons.Cancel);
        btnRightNegative.setClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                ProgressThread.Cancel();
                btnRightNegative.disable();
                btnRightNegative.setText(Translation.get("waitForCancel"));
                isCanceld = true;
                if (mCancelListener != null)
                    mCancelListener.isCanceled();
                return true;
            }
        });

        measuredLabelHeight = Fonts.Measure("T").height * 1.5f;

        progressMessageTextView = new CB_Label(this.name + " progressMessageTextView", leftBorder, margin, innerWidth, measuredLabelHeight);
        this.addChild(progressMessageTextView);

        CB_RectF rec = new CB_RectF(0, progressMessageTextView.getMaxY() + margin, this.getContentSize().getWidth(), UiSizes.getInstance().getButtonHeight() * 0.75f);

        progressBar = new ProgressBar(rec, "");
        progressBar.setProgress(0);
        this.addChild(progressBar);

        messageTextView = new CB_Label(this.name + " messageTextView", leftBorder, progressBar.getMaxY() + margin, innerWidth, measuredLabelHeight);
        this.addChild(messageTextView);

    }

    public static ProgressDialog Show(String title, AnimationBase Animation, RunnableReadyHandler RunThread) {
        ProgressDialog PD = createProgressDialog(title, true, RunThread);
        PD.setAnimation(Animation);

        GL.that.showDialog(PD);

        return PD;
    }

    public static ProgressDialog Show(String title, RunnableReadyHandler RunThread) {

        ProgressDialog PD = createProgressDialog(title, false, RunThread);
        GL.that.showDialog(PD);

        return PD;
    }

    private static ProgressDialog createProgressDialog(String title, boolean withAnimation, RunnableReadyHandler RunThread) {
        if (ProgressThread != null) {
            ProgressThread = null;

        }

        ProgressThread = RunThread;
        titleText = title;

        ProgressDialog PD = new ProgressDialog(calcMsgBoxSize(title, true, true, true), title);

        float h = withAnimation ? UiSizes.getInstance().getButtonHeight() / 2 : 0;

        PD.setHeight(PD.getHeight() + (PD.measuredLabelHeight * 2f) + h);

        PD.setTitle(titleText);
        return PD;
    }

    public static void Ready() {
        that.close();
    }

    public void setCancelListener(ICancelListener listener) {
        mCancelListener = listener;
    }

    public boolean isCanceld() {
        return isCanceld;
    }

    public void setAnimation(final AnimationBase Animation) {
        GL.that.RunOnGL(() -> {
            ProgressDialog.this.removeChild(ProgressDialog.this.animation);
            CB_RectF imageRec = new CB_RectF(0, progressBar.getMaxY() + margin, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight());
            ProgressDialog.this.animation = Animation.INSTANCE(imageRec);
            ProgressDialog.this.addChild(ProgressDialog.this.animation);
        });

    }

    @Override
    public void ProgressChangedEventCalled(String Message, String ProgressMessage, int Progress) {
        setProgress(Message, ProgressMessage, Progress);
    }

    @Override
    public void onShow() {
        // Registriere Progress Changed Event
        ProgresssChangedEventList.Add(this);
        if (ProgressThread != null) {
            Timer runTimer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    ProgressThread.run();
                    ProgressThread.runnableIsReady(ProgressThread.doCancel());
                }
            };

            runTimer.schedule(task, 20);

        }

    }

    @Override
    public void onHide() {
        // l�sche Registrierung Progress Changed Event
        ProgresssChangedEventList.Remove(this);
    }

    public void setProgress(final String Msg, final String ProgressMessage, final int value) {
        GL.that.RunOnGL(() -> {
            if (ProgressDialog.this.isDisposed())
                return;
            progressBar.setProgress(value);
            progressMessageTextView.setText(ProgressMessage);
            if (!Msg.equals(""))
                messageTextView.setText(Msg);
        });
    }

    public interface ICancelListener {
        public void isCanceled();
    }

}
