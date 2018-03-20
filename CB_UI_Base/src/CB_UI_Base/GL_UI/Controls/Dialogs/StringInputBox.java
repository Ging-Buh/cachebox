package CB_UI_Base.GL_UI.Controls.Dialogs;

import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;

public class StringInputBox extends GL_MsgBox {
	public StringInputBox(Size size, String name) {
		super(size, name);
	}

	public static EditTextField editText;

	public static void Show(WrapType type, String msg, String title, String initialString, OnMsgBoxClickListener Listener) {

		Size msgBoxSize = calcMsgBoxSize(msg, true, true, false);

		StringInputBox msgBox = new StringInputBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);
		msgBox.mMsgBoxClickListener = Listener;

		editText = new EditTextField(msgBox, "MsgBoxLabel").setWrapType(type);
		editText.setText(initialString);
		editText.setCursorPosition(initialString.length());

		float topBottom = editText.getStyle().getTopHeight(true) + editText.getStyle().getBottomHeight(true);
		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);

		if (type == WrapType.SINGLELINE) {
			editText.setHeight(topBottom + SingleLineHeight);
		} else {
			editText.setHeight(topBottom + (SingleLineHeight * 5));
		}

		Label label = new Label(msg).setWrapType(WrapType.WRAPPED);

		msgBox.addLast(editText);
		msgBox.addLast(label);
		msgBox.setButtonCaptions(MessageBoxButtons.OKCancel);
		GL.that.showDialog(msgBox, true);

	}

	@Override
	public void onShow() {
		super.onShow();

		// register Textfield render
		editText.setFocus(true);
	}

}
