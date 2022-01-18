package de.droidcachebox.gdx.controls.dialogs;

import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.translation.Translation;

public class NewDB_InputBox extends ButtonDialog {
    public static EditTextField editTextField;

    public NewDB_InputBox(String newDBTitle, String insNewDBNameExplanation) {
        super("", newDBTitle, MsgBoxButton.OKCancel, MsgBoxIcon.None);
        newContentBox();
        editTextField = new EditTextField( this, "editTextField").setWrapType(WrapType.SINGLELINE);
        editTextField.setText("");
        editTextField.setCursorPosition(0);
        editTextField.showFromLineNo(0);
        float topBottom = editTextField.getStyle().getTopHeight(true) + editTextField.getStyle().getBottomHeight(true);
        editTextField.setHeight(topBottom + editTextField.getFont().getLineHeight() + editTextField.getFont().getAscent() - editTextField.getFont().getDescent());

        CB_Label messageLabel = new CB_Label();
        messageLabel.setWidth(contentBox.getWidth());
        messageLabel.setWrapType(WrapType.WRAPPED);
        messageLabel.setText(insNewDBNameExplanation);
        messageLabel.setHeight(messageLabel.getTextHeight());

        CB_CheckBox commonRepositoryCheckBox = new CB_CheckBox();
        commonRepositoryCheckBox.setChecked(true);
        data = new AtomicBoolean(true); // useCommonRepository
        commonRepositoryCheckBox.setClickHandler((view, x, y, pointer, button) -> {
            ((AtomicBoolean)data).set(commonRepositoryCheckBox.isChecked());
            return true;
        });
        commonRepositoryCheckBox.setElementAlignment(1);

        CB_Label commonRepositoryLabel = new CB_Label();
        commonRepositoryLabel.setWidth(contentBox.getWidth() - commonRepositoryCheckBox.getWidth());
        commonRepositoryLabel.setWrappedText(Translation.get("UseDefaultRep"));
        commonRepositoryLabel.setHeight(commonRepositoryLabel.getTextHeight());

        contentBox.addLast(editTextField);
        if (messageLabel.getText().length() > 0) contentBox.addLast(messageLabel);
        contentBox.addNext(commonRepositoryCheckBox, FIXED);
        contentBox.addLast(commonRepositoryLabel);

        readyContentBox();
    }

    @Override
    public void onShow() {
        editTextField.setFocus(true);
    }

}
