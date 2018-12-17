package CB_UI_Base.GL_UI.Controls.Dialogs;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Controls.Animation.AnimationBase;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.ProgressBar;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.interfaces.RunnableReadyHandler;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Events.ProgressChangedEvent;
import CB_Utils.Events.ProgresssChangedEventList;

import java.util.Timer;
import java.util.TimerTask;

public class ProgressDialog extends GL_MsgBox implements ProgressChangedEvent {
    private static RunnableReadyHandler ProgressThread;
    private static String titleText;
    private static ProgressDialog that;
    public float measuredLabelHeight = 0;
    private Label messageTextView;
    private Label progressMessageTextView;
    private ProgressBar progressBar;
    private AnimationBase animation;
    private boolean isCanceld = false;
    private ICancelListener mCancelListener;

    public ProgressDialog(Size size, String name) {
        super(size, name);
        that = this;
        isCanceld = false;

        setButtonCaptions(MessageBoxButtons.Cancel);
        button3.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                ProgressThread.Cancel();
                button3.disable();
                button3.setText(Translation.Get("waitForCancel"));
                isCanceld = true;
                if (mCancelListener != null)
                    mCancelListener.isCanceled();
                return true;
            }
        });

        measuredLabelHeight = Fonts.Measure("T").height * 1.5f;

        progressMessageTextView = new Label(this.name + " progressMessageTextView", leftBorder, margin, innerWidth, measuredLabelHeight);
        this.addChild(progressMessageTextView);

        CB_RectF rec = new CB_RectF(0, progressMessageTextView.getMaxY() + margin, this.getContentSize().width, UI_Size_Base.that.getButtonHeight() * 0.75f);

        progressBar = new ProgressBar(rec, "");
        progressBar.setProgress(0);
        this.addChild(progressBar);

        messageTextView = new Label(this.name + " messageTextView", leftBorder, progressBar.getMaxY() + margin, innerWidth, measuredLabelHeight);
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

        float h = withAnimation ? UI_Size_Base.that.getButtonHeight() / 2 : 0;

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
        GL.that.RunOnGL(new IRunOnGL() {

            @Override
            public void run() {
                ProgressDialog.this.removeChild(ProgressDialog.this.animation);
                CB_RectF imageRec = new CB_RectF(0, progressBar.getMaxY() + margin, UI_Size_Base.that.getButtonHeight(), UI_Size_Base.that.getButtonHeight());
                ProgressDialog.this.animation = Animation.INSTANCE(imageRec);
                ProgressDialog.this.addChild(ProgressDialog.this.animation);
            }
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
                    ProgressThread.RunnableIsReady(ProgressThread.doCancel());
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
        GL.that.RunOnGL(new IRunOnGL() {

            @Override
            public void run() {
                if (ProgressDialog.this.isDisposed())
                    return;
                progressBar.setProgress(value);
                progressMessageTextView.setText(ProgressMessage);
                if (!Msg.equals(""))
                    messageTextView.setText(Msg);
            }
        });
    }

    public interface ICancelListener {
        public void isCanceled();
    }

}
