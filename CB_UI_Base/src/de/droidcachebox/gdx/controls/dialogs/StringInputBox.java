package de.droidcachebox.gdx.controls.dialogs;

import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;

public class StringInputBox extends ButtonDialog {
    public static EditTextField editTextField;

    public StringInputBox(String msg, String title, String initialString, WrapType wrapType) {
        super(msg, title, MsgBoxButton.OKCancel, MsgBoxIcon.None);
        newContentBox();

        editTextField = new EditTextField(this,"StringInputBox editText").setWrapType(wrapType);
        editTextField.setText(initialString);
        editTextField.setCursorPosition(initialString.length());
        editTextField.showFromLineNo(0);
        float topBottom = editTextField.getStyle().getTopHeight(true) + editTextField.getStyle().getBottomHeight(true);
        if (wrapType == WrapType.SINGLELINE) {
            editTextField.setHeight(topBottom + editTextField.getFont().getLineHeight() + editTextField.getFont().getAscent() - editTextField.getFont().getDescent());
        } else {
            editTextField.setHeight(topBottom + editTextField.getFont().getLineHeight() * 5 + editTextField.getFont().getAscent() - editTextField.getFont().getDescent());
        }

        CB_Label label = new CB_Label();
        label.setWidth(contentBox.getWidth());
        label.setWrappedText(msg);
        label.setHeight(label.getTextHeight());

        contentBox.addLast(editTextField);
        if (label.getText().length() > 0) contentBox.addLast(label);

        readyContentBox();
    }

    @Override
    public void onShow() {
        editTextField.setFocus(true);
    }

}
