package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.WrapType;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.math.Size;

public class StringInputBox extends MessageBox {
    public static EditTextField editText;

    private StringInputBox(Size size) {
        super(size, "StringInputBox");
    }

    public static void Show(WrapType type, String msg, String title, String initialString, OnMsgBoxClickListener Listener) {

        StringInputBox msgBox = new StringInputBox(calcMsgBoxSize(msg, true, true, false));

        editText = new EditTextField(msgBox, "StringInputBox editText").setWrapType(type);
        editText.setWidth(msgBox.getWidth());
        editText.setText(initialString);
        editText.setCursorPosition(initialString.length());
        editText.showFromLineNo(0);
        float topBottom = editText.getStyle().getTopHeight(true) + editText.getStyle().getBottomHeight(true);
        if (type == WrapType.SINGLELINE) {
            editText.setHeight(topBottom + editText.getFont().getLineHeight() + editText.getFont().getAscent() - editText.getFont().getDescent());
        } else {
            editText.setHeight(topBottom + editText.getFont().getLineHeight() * 5 + editText.getFont().getAscent() - editText.getFont().getDescent());
        }

        CB_Label label = new CB_Label(msg, null, null, WrapType.WRAPPED);
        label.setWidth(msgBox.getWidth());
        label.setHeight(label.getTextHeight());

        msgBox.setHeight(label.getHeight() + editText.getHeight() + calcFooterHeight(true) + getTitleHeight());

        msgBox.setTitle(title);
        msgBox.addLast(editText);
        msgBox.addLast(label);
        msgBox.addButtons(MessageBoxButtons.OKCancel);

        msgBox.setMsgBoxClickListener(Listener);

        GL.that.showDialog(msgBox, true);

    }

    @Override
    public void onShow() {
        editText.setFocus(true);
    }

}
