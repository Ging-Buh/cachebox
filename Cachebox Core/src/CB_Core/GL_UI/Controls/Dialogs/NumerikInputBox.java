package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.NumPad;
import CB_Core.GL_UI.Controls.NumPad.keyEventListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.GL_UI.libGdx_Controls.CB_TextField;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;

import com.badlogic.gdx.scenes.scene2d.ui.TextField.OnscreenKeyboard;

public class NumerikInputBox extends GL_MsgBox
{

	private static NumerikInputBox that;

	public NumerikInputBox(Size size, String name)
	{
		super(size, name);
		that = this;
	}

	public static CB_TextField editText;
	public static returnValueListner mReturnListner;

	public static void Show(String msg, String title, int initialValue, returnValueListner Listner)
	{
		mReturnListner = Listner;

		Size msgBoxSize = calcMsgBoxSize(msg, true, true, false);
		CB_RectF numPadRec = new CB_RectF(0, 0, msgBoxSize.width, (msgBoxSize.width - (margin * 2)) / 5 * 4);

		NumerikInputBox msgBox = new NumerikInputBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		textFieldRec.setHeight(Fonts.getNormal().getLineHeight() * 1.6f);

		editText = new CB_TextField(textFieldRec, "MsgBoxLabel");
		editText.setZeroPos();
		editText.setY(margin);
		editText.setText(String.valueOf(initialValue));
		editText.setCursorPosition((String.valueOf(initialValue)).length());
		editText.setOnscreenKeyboard(new OnscreenKeyboard()
		{

			@Override
			public void show(boolean arg0)
			{
				// do nothing, don´t show Keybord
			}
		});
		editText.setFocus();

		CB_RectF LabelRec = msgBox.getContentSize().getBounds();
		LabelRec.setHeight(LabelRec.getHeight() - textFieldRec.getHeight());

		Label label = new Label(LabelRec, "MsgBoxLabel");
		label.setZeroPos();
		label.setY(editText.getMaxY() + margin);
		label.setWrappedText(msg);
		msgBox.addChild(label);

		msgBox.setHeight(msgBox.getHeight() + editText.getHeight() + numPadRec.getHeight());

		msgBox.addChild(editText);

		// ######### NumPad ################

		NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.Type.withOkCancel, listner);
		numPad.setY(margin);
		msgBox.addFooterChild(numPad);
		msgBox.mFooterHeight = numPad.getHeight() + (margin * 2);

		GL_Listener.glListener.showDialog(msgBox);
		GL_Listener.glListener.addRenderView(msgBox, GL_Listener.FRAME_RATE_IDLE);// Cursor blink
	}

	public interface returnValueListner
	{
		public void returnValue(int value);
	}

	static keyEventListner listner = new keyEventListner()
	{

		@Override
		public void KeyPressed(String value)
		{
			int cursorPos = editText.getCursorPosition();

			if (value.equals("O"))
			{
				if (mReturnListner != null)
				{
					try
					{
						int intValue = Integer.parseInt(editText.getText());
						mReturnListner.returnValue(intValue);
					}
					catch (NumberFormatException e)
					{
						e.printStackTrace();
					}
				}
				GL_Listener.glListener.closeDialog(that);
			}
			else if (value.equals("C"))
			{
				GL_Listener.glListener.closeDialog(that);
			}
			else if (value.equals("<"))
			{
				if (cursorPos == 0) cursorPos = 1; // cursorPos darf nicht 0 sein
				editText.setCursorPosition(cursorPos - 1);
			}
			else if (value.equals(">"))
			{
				editText.setCursorPosition(cursorPos + 1);
			}
			else if (value.equals("D"))
			{
				if (cursorPos > 0)
				{
					String text2 = editText.getText().substring(cursorPos);
					String text1 = editText.getText().substring(0, cursorPos - 1);

					editText.setText(text1 + text2);
					editText.setCursorPosition(cursorPos + -1);
				}
			}
			else
			{
				String text2 = editText.getText().substring(cursorPos);
				String text1 = editText.getText().substring(0, cursorPos);

				editText.setText(text1 + value + text2);
				editText.setCursorPosition(cursorPos + value.length());
			}

		}
	};

}
