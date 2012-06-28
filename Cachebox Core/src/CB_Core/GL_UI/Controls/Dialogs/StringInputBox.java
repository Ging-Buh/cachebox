package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.libGdx_Controls.CB_TextField;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;

public class StringInputBox extends GL_MsgBox
{
	public StringInputBox(Size size, String name)
	{
		super(size, name);

	}

	public static CB_TextField editText;

	public static void Show(String msg, String title, String initialString, OnMsgBoxClickListener Listener)
	{
		mMsgBoxClickListner = Listener;

		Size msgBoxSize = calcMsgBoxSize(msg, true, true, false);

		GL_MsgBox msgBox = new GL_MsgBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		textFieldRec.setHeight(Fonts.getNormal().getLineHeight() * 1.6f);

		editText = new CB_TextField(textFieldRec, "MsgBoxLabel");
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
}
