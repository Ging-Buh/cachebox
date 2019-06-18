package CB_UI.GL_UI.Controls.Dialogs;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Controls.CB_CheckBox;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;

public class NewDB_InputBox extends MessageBox {
    public static EditTextField editText;

    public NewDB_InputBox(Size size, String name) {
        super(size, name);
    }

    public static void Show(WrapType type, String msg, String title, String initialString, final OnMsgBoxClickListener Listener) {

        Size msgBoxSize = calcMsgBoxSize(msg, true, true, false);

        NewDB_InputBox msgBox = new NewDB_InputBox(msgBoxSize, "MsgBox");
        msgBox.setTitle(title);

        final CB_CheckBox chk = new CB_CheckBox("");
        msgBox.setMsgBoxClickListener((which, data) -> {
            Listener.onClick(which, chk.isChecked());
            return true;
        });
        CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

        chk.setY(margin * 2);
        chk.setChecked(true);
        CB_RectF LabelRec = msgBox.getContentSize().getBounds();
        LabelRec.setHeight(LabelRec.getHeight() - chk.getHeight());

        CB_Label lbl = new CB_Label(LabelRec);
        lbl.setX(chk.getMaxX() + margin);
        lbl.setY(chk.getY());
        lbl.setText(Translation.get("UseDefaultRep"));

        editText = new EditTextField(textFieldRec, msgBox, "editText", type);
        editText.setZeroPos();
        editText.setY(chk.getMaxY() + margin);
        editText.setText(initialString);
        editText.setCursorPosition(initialString.length());

        float topBottom = editText.getStyle().getTopHeight(true) + editText.getStyle().getBottomHeight(true);

        float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);

        if (type == WrapType.SINGLELINE) {
            editText.setHeight(topBottom + SingleLineHeight);
        } else {
            editText.setHeight(topBottom + (SingleLineHeight * 5));
        }
        msgBox.setHeight(msgBox.getHeight() + editText.getHeight() + (margin * 4) + chk.getHalfHeight());

        LabelRec.setHeight(LabelRec.getHeight() - editText.getHeight());

        CB_Label label = new CB_Label(LabelRec);
        label.setZeroPos();
        label.setY(editText.getMaxY());
        label.setWrappedText(msg);

        msgBox.addChild(editText);
        msgBox.addChild(label);
        msgBox.addChild(chk);
        msgBox.addChild(lbl);

        msgBox.addButtons(MessageBoxButtons.OKCancel);

        GL.that.showDialog(msgBox, true);

    }

    @Override
    public void onShow() {
        // register Textfield render
        editText.setFocus(true);
    }

}
