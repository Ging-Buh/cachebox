package CB_UI.GL_UI.Activitys;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.CollapseBox.IAnimatedHeightChangedListener;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.ProgressBar;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Events.ProgressChangedEvent;
import CB_Utils.Events.ProgresssChangedEventList;

public class SyncActivity extends ActivityBase implements ProgressChangedEvent {
    public static final boolean RELEASED = false;
    private static Boolean importCancel = false;
    private Button bOK;
    private float innerHeight;
    private Label lblProgressMsg;
    private ProgressBar pgBar;
    private Boolean importStarted = false;

    private ScrollBox scrollBox;
    IAnimatedHeightChangedListener mAnimationListener = Height -> Layout();

    public SyncActivity() {
        super(ActivityRec(), "importActivity");
        innerHeight = 1000;
        scrollBox = new ScrollBox(ActivityRec());
        this.addChild(scrollBox);
        createOkCancelBtn();
        createTitleLine();
        scrollBox.setHeight(lblProgressMsg.getY() - bOK.getMaxY() - margin - margin);
        scrollBox.setY(bOK.getMaxY() + margin);
        scrollBox.setBackground(this.getBackground());

        initialForm();

        Layout();
    }

    public static boolean isCanceld() {
        return importCancel;
    }

    @Override
    public void onShow() {
        ProgresssChangedEventList.Add(this);
    }

    @Override
    public void onHide() {
        ProgresssChangedEventList.Remove(this);
    }

    private void createOkCancelBtn() {
        bOK = new Button(leftBorder, leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
        Button bCancel = new Button(bOK.getMaxX(), leftBorder, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

        // Translations
        bOK.setText(Translation.Get("import"));
        bCancel.setText(Translation.Get("cancel"));

        this.addChild(bOK);
        bOK.setOnClickListener((v, x, y, pointer, button) -> {
            // ImportNow();
            return true;
        });

        this.addChild(bCancel);
        bCancel.setOnClickListener((v, x, y, pointer, button) -> {
            if (importCancel)
                return true;

            if (importStarted) {
                GL_MsgBox.Show(Translation.Get("WantCancelImport"), Translation.Get("CancelImport"), MessageBoxButtons.YesNo, MessageBoxIcon.Stop, (which, data) -> {
                    if (which == GL_MsgBox.BUTTON_POSITIVE) {
                        // cancelImport();
                    }
                    return true;
                });
            } else
                finish();
            return true;
        });

    }

    private void createTitleLine() {
        // Title+Progressbar

        float lineHeight = UI_Size_Base.that.getButtonHeight() * 0.75f;

        Label lblTitle = new Label(this.name + " lblTitle", leftBorder + margin, this.getHeight() - this.getTopHeight() - lineHeight - margin, innerWidth - margin, lineHeight);
        lblTitle.setFont(Fonts.getBig());
        float lblWidth = lblTitle.setText(Translation.Get("import")).getTextWidth();
        this.addChild(lblTitle);

        CB_RectF rec = new CB_RectF(lblTitle.getX() + lblWidth + margin, lblTitle.getY(), innerWidth - margin - margin - lblWidth, lineHeight);

        pgBar = new ProgressBar(rec, "ProgressBar");

        pgBar.setProgress(0, "");

        float SmallLineHeight = Fonts.MeasureSmall("Tg").height;

        lblProgressMsg = new Label(this.name + " lblProgressMsg", leftBorder + margin, lblTitle.getY() - margin - SmallLineHeight, innerWidth - margin - margin, SmallLineHeight);

        lblProgressMsg.setFont(Fonts.getSmall());

        this.addChild(pgBar);
        this.addChild(lblProgressMsg);

    }

    private void Layout() {

        innerHeight = this.getHeight();
        scrollBox.setVirtualHeight(innerHeight);
    }

    private void initialForm() {

    }

    @Override
    public void ProgressChangedEventCalled(final String Message, final String ProgressMessage, final int Progress) {

        GL.that.RunOnGL(() -> {
            pgBar.setProgress(Progress);
            lblProgressMsg.setText(ProgressMessage);
            if (!Message.equals(""))
                pgBar.setText(Message);
        });

    }

}
