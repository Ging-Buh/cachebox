package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.libGdx_Controls.TextField;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;

public class StringInputBox extends GL_MsgBox
{
	public StringInputBox(Size size, String name)
	{
		super(size, name);

	}

	public static TextField editText;

	public static void Show(String msg, String title, String initialString, OnMsgBoxClickListener Listener)
	{
		mMsgBoxClickListner = Listener;

		Size msgBoxSize = calcMsgBoxSize(msg, true, true, false);

		GL_MsgBox msgBox = new GL_MsgBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();
		float newYPos = textFieldRec.getHeight();

		textFieldRec.setHeight(textFieldRec.getHeight() / 1.2f);

		newYPos -= textFieldRec.getHeight();

		editText = new TextField(textFieldRec, "MsgBoxLabel");
		editText.setZeroPos();
		editText.setY(newYPos);
		editText.setText(initialString);
		msgBox.addChild(editText);
		setButtonCaptions(msgBox, MessageBoxButtons.OKCancel);

		GL_Listener.glListener.showDialog(msgBox);

	}
}
