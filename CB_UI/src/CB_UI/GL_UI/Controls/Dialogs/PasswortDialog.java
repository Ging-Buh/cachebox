package CB_UI.GL_UI.Controls.Dialogs;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Linearlayout;
import CB_UI_Base.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.SizeF;

public class PasswortDialog extends ButtonDialog {

    public EditTextField editTextUser;
    public EditTextField editTextPW;

    private Linearlayout layout;

    private float TextFieldHeight;
    private float LabelHeight;
    private SizeF msgBoxContentSize;
    private IReturnListener mReturnListener;

    public PasswortDialog(IReturnListener listener) {
        super(Menu.getMenuRec(), "PW-Dialog", "", Translation.Get("enterPW"), MessageBoxButtons.OKCancel, MessageBoxIcon.GC_Live, null);
        mReturnListener = listener;

        msgBoxContentSize = getContentSize();
        // initial VariableField
        TextFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;
        LabelHeight = Fonts.getNormal().getLineHeight();

        layout = new Linearlayout(msgBoxContentSize.width, "Layout");

        Label lblName = new Label(this.name + " lblName", 0, 0, msgBoxContentSize.width, LabelHeight);
        lblName.setText(Translation.Get("LogIn"));
        layout.addChild(lblName);

        CB_RectF rec = new CB_RectF(0, 0, msgBoxContentSize.width, TextFieldHeight);

        editTextUser = new EditTextField(this, rec, WrapType.SINGLELINE, "SolverDialogTextField");
        layout.addChild(editTextUser);

        Label lblPW = new Label(this.name + " lblPW", 0, 0, msgBoxContentSize.width, LabelHeight);
        lblPW.setText(Translation.Get("GCPW"));
        layout.addChild(lblPW);

        editTextPW = new EditTextField(this, rec, WrapType.SINGLELINE, "SolverDialogTextField");

        // TODO set PW-Mode => hat noch einen Fehler
        // editTextPW.setPasswordMode();
        // editTextPW.setPasswordCharacter("*".charAt(0));
        //
        layout.addChild(editTextPW);

        this.addChild(layout);

        Size msgBoxSize = GL_MsgBox.calcMsgBoxSize("teste", true, true, false);
        msgBoxSize.height = (int) (msgBoxSize.height + layout.getHeight());
        this.setSize(msgBoxSize.asFloat());

        mMsgBoxClickListener = new OnMsgBoxClickListener() {

            @Override
            public boolean onClick(int which, Object data) {
                if (which == BUTTON_POSITIVE) {

                    if (mReturnListener != null)
                        mReturnListener.returnFromPW_Dialog(editTextUser.getText(), editTextPW.getText());
                    close();
                } else {
                    if (mReturnListener != null)
                        mReturnListener.returnFromPW_Dialog(null, null);
                    close();
                }

                return true;
            }
        };

    }

    public interface IReturnListener {
        public void returnFromPW_Dialog(String User, String PW);
    }

}
