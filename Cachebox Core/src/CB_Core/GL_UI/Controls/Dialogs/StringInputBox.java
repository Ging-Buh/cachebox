package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;

public class StringInputBox extends GL_MsgBox
{
	public StringInputBox(Size size, String name)
	{
		super(size, name);

	}

	public static EditWrapedTextField editText;

	public static void Show(TextFieldType type, String msg, String title, String initialString, OnMsgBoxClickListener Listener)
	{
		mMsgBoxClickListner = Listener;

		Size msgBoxSize = calcMsgBoxSize(msg, true, true, false);

		StringInputBox msgBox = new StringInputBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		if (type != TextFieldType.SingleLine)
		{
			msgBoxSize.height += (int) msgBox.getContentSize().height * 2;
			msgBox.setSize(msgBoxSize.asFloat());
		}

		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		editText = new EditWrapedTextField(msgBox, textFieldRec, type, "MsgBoxLabel");
		editText.setZeroPos();
		editText.setText(initialString);

		CB_RectF LabelRec = msgBox.getContentSize().getBounds();
		LabelRec.setHeight(LabelRec.getHeight() - textFieldRec.getHeight());

		Label label = new Label(LabelRec, "MsgBoxLabel");
		label.setZeroPos();
		label.setY(editText.getMaxY() + margin);
		label.setWrappedText(msg);
		msgBox.addChild(label);

		msgBox.setHeight(msgBox.getHeight() + editText.getHeight());

		msgBox.addChild(editText);
		setButtonCaptions(msgBox, MessageBoxButtons.OKCancel);

		GL_Listener.glListener.showDialog(msgBox, true);

	}

	@Override
	public void onShow()
	{
		super.onShow();

		// register Textfield render
		editText.setFocus();
	}

}
