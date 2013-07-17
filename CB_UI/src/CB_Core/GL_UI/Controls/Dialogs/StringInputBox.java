package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.Enums.WrapType;
import CB_Core.GL_UI.Controls.EditTextField;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;

public class StringInputBox extends GL_MsgBox
{
	public StringInputBox(Size size, String name)
	{
		super(size, name);
	}

	public static EditTextField editText;

	public static void Show(WrapType type, String msg, String title, String initialString, OnMsgBoxClickListener Listener)
	{

		Size msgBoxSize = calcMsgBoxSize(msg, true, true, false);

		StringInputBox msgBox = new StringInputBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);
		msgBox.mMsgBoxClickListner = Listener;
		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		editText = new EditTextField(msgBox, textFieldRec, type, "MsgBoxLabel");
		editText.setZeroPos();
		editText.setY(margin * 2);
		editText.setText(initialString);
		editText.setCursorPosition(initialString.length());

		float topBottom = editText.getStyle().background.getTopHeight() + editText.getStyle().background.getBottomHeight();

		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);

		if (type == WrapType.SINGLELINE)
		{
			editText.setHeight(topBottom + SingleLineHeight);
		}
		else
		{
			editText.setHeight(topBottom + (SingleLineHeight * 5));
		}
		msgBox.setHeight(msgBox.getHeight() + editText.getHeight() + (margin * 4));

		CB_RectF LabelRec = msgBox.getContentSize().getBounds();
		LabelRec.setHeight(LabelRec.getHeight() - editText.getHeight());

		Label label = new Label(LabelRec, "MsgBoxLabel");
		label.setZeroPos();
		label.setY(editText.getMaxY());
		label.setWrappedText(msg);

		msgBox.addChild(editText);
		msgBox.addChild(label);

		msgBox.setButtonCaptions(MessageBoxButtons.OKCancel);

		GL.that.showDialog(msgBox, true);

	}

	@Override
	public void onShow()
	{
		super.onShow();

		// register Textfield render
		editText.setFocus();
	}

}