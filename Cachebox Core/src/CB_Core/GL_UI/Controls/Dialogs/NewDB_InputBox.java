package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.chkBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.TranslationEngine.Translation;

public class NewDB_InputBox extends GL_MsgBox
{
	public NewDB_InputBox(Size size, String name)
	{
		super(size, name);
	}

	public static EditWrapedTextField editText;

	public static void Show(TextFieldType type, String msg, String title, String initialString, final OnMsgBoxClickListener Listener)
	{

		Size msgBoxSize = calcMsgBoxSize(msg, true, true, false);

		NewDB_InputBox msgBox = new NewDB_InputBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		final chkBox chk = new chkBox("");
		msgBox.mMsgBoxClickListner = new OnMsgBoxClickListener()
		{

			@Override
			public boolean onClick(int which, Object data)
			{
				Listener.onClick(which, chk.isChecked());
				return true;
			}
		};
		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		chk.setY(margin * 2);
		chk.setChecked(true);
		CB_RectF LabelRec = msgBox.getContentSize().getBounds();
		LabelRec.setHeight(LabelRec.getHeight() - chk.getHeight());

		Label lbl = new Label(LabelRec, "MsgBoxLabel");
		lbl.setX(chk.getMaxX() + margin);
		lbl.setY(chk.getY());
		lbl.setText(Translation.Get("UseDefaultRep"));

		editText = new EditWrapedTextField(msgBox, textFieldRec, type, "MsgBoxLabel");
		editText.setZeroPos();
		editText.setY(chk.getMaxY() + margin);
		editText.setText(initialString);
		editText.setCursorPosition(initialString.length());

		float topBottom = editText.getStyle().background.getTopHeight() + editText.getStyle().background.getBottomHeight();

		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);

		if (type == TextFieldType.SingleLine)
		{
			editText.setHeight(topBottom + SingleLineHeight);
		}
		else
		{
			editText.setHeight(topBottom + (SingleLineHeight * 5));
		}
		msgBox.setHeight(msgBox.getHeight() + editText.getHeight() + (margin * 4) + chk.getHalfHeight());

		LabelRec.setHeight(LabelRec.getHeight() - editText.getHeight());

		Label label = new Label(LabelRec, "MsgBoxLabel");
		label.setZeroPos();
		label.setY(editText.getMaxY());
		label.setWrappedText(msg);

		msgBox.addChild(editText);
		msgBox.addChild(label);
		msgBox.addChild(chk);
		msgBox.addChild(lbl);

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
