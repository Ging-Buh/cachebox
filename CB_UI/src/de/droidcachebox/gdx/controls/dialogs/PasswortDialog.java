package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.WrapType;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.Linearlayout;
import de.droidcachebox.gdx.controls.messagebox.ButtonDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBox.OnMsgBoxClickListener;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.translation.Translation;

public class PasswortDialog extends ButtonDialog {

    public EditTextField editTextUser;
    public EditTextField editTextPW;

    private Linearlayout layout;

    private float TextFieldHeight;
    private float LabelHeight;
    private SizeF msgBoxContentSize;
    private IReturnListener mReturnListener;

    public PasswortDialog(IReturnListener listener) {
        super(Menu.getMenuRec(), "PW-Dialog", "", Translation.get("enterPW"), MessageBoxButton.OKCancel, MessageBoxIcon.GC_Live, null);
        mReturnListener = listener;

        msgBoxContentSize = getContentSize();
        // initial VariableField
        TextFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;
        LabelHeight = Fonts.getNormal().getLineHeight();

        layout = new Linearlayout(msgBoxContentSize.getWidth(), "layout");

        CB_Label lblName = new CB_Label(this.name + " lblName", 0, 0, msgBoxContentSize.getWidth(), LabelHeight);
        lblName.setText(Translation.get("LogIn"));
        layout.addChild(lblName);

        CB_RectF rec = new CB_RectF(0, 0, msgBoxContentSize.getWidth(), TextFieldHeight);

        editTextUser = new EditTextField(rec, this, "*" + Translation.get("LogIn"), WrapType.SINGLELINE);
        layout.addChild(editTextUser);

        CB_Label lblPW = new CB_Label(this.name + " lblPW", 0, 0, msgBoxContentSize.getWidth(), LabelHeight);
        lblPW.setText(Translation.get("GCPW"));
        layout.addChild(lblPW);

        editTextPW = new EditTextField(rec, this, "*" + Translation.get("GCPW"), WrapType.SINGLELINE);

        // TODO set PW-Mode => hat noch einen Fehler
        // editTextPW.setPasswordMode();
        // editTextPW.setPasswordCharacter("*".charAt(0));
        //
        layout.addChild(editTextPW);

        this.addChild(layout);

        Size msgBoxSize = MessageBox.calcMsgBoxSize("teste", true, true, false);
        msgBoxSize.height = (int) (msgBoxSize.height + layout.getHeight());
        this.setSize(msgBoxSize.asFloat());

        mMsgBoxClickListener = new OnMsgBoxClickListener() {

            @Override
            public boolean onClick(int which, Object data) {
                if (which == MessageBox.BTN_LEFT_POSITIVE) {

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
