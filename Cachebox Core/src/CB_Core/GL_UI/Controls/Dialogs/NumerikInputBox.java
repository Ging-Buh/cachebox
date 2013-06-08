package CB_Core.GL_UI.Controls.Dialogs;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.runOnGL;
import CB_Core.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_Core.GL_UI.Controls.EditWrapedTextField;
import CB_Core.GL_UI.Controls.EditWrapedTextField.TextFieldType;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.NumPad;
import CB_Core.GL_UI.Controls.NumPad.keyEventListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.Size;
import CB_Core.Math.UI_Size_Base;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.Input.Keys;

public class NumerikInputBox extends CB_View_Base
{

	public NumerikInputBox(String name)
	{
		super(name);
	}

	private enum type
	{
		intType, doubleType, timeType
	}

	private static type mType;

	private static EditWrapedTextField editText;

	public static returnValueListner mReturnListner;
	public static returnValueListnerDouble mReturnListnerDouble;
	public static returnValueListnerTime mReturnListnerTime;

	/**
	 ** show msgbox for input of int
	 **/
	public static GL_MsgBox Show(String msg, String title, int initialValue, returnValueListner Listner)
	{
		mReturnListner = Listner;
		mType = type.intType;

		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize(msg, true, true, false);

		float margin = UI_Size_Base.that.getMargin();
		GL_MsgBox msgBox = new GL_MsgBox(msgBoxSize, "MsgBox");

		editText = new EditWrapedTextField("editText", TextFieldType.SingleLine);
		float topBottom = editText.getStyle().background.getTopHeight() + editText.getStyle().background.getBottomHeight();
		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);
		editText.setHeight(topBottom + SingleLineHeight);

		Label label = new Label("MsgBoxLabel");

		CB_RectF numPadRec = new CB_RectF(0, 0, msgBoxSize.width, UI_Size_Base.that.getButtonHeight() * 6);
		msgBox.setHeight(msgBox.getHeight() + editText.getHeight() + numPadRec.getHeight());

		msgBox.setMargins(0, margin);
		msgBox.setBorders(margin, margin);

		NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.Type.withoutDotOkCancel, getkeyListner(msgBox));

		msgBox.initRow(BOTTOMUP, margin);
		msgBox.addLast(numPad);

		msgBox.setFooterHeight(msgBox.getHeightFromBottom());
		msgBox.addLast(editText);

		msgBox.addLast(label);

		msgBox.adjustHeight();

		msgBox.setTitle(title);
		msgBox.setHeight(msgBox.getHeight() + 2 * SingleLineHeight);

		label.setWrappedText(msg);

		editText.setText(String.valueOf(initialValue));
		editText.setCursorPosition((String.valueOf(initialValue)).length());
		editText.dontShowSoftKeyBoardOnFocus();
		editText.setOnscreenKeyboard(new OnscreenKeyboard()
		{

			@Override
			public void show(boolean arg0)
			{
				// do nothing, don´t show Keybord
			}
		});
		editText.setFocus();

		GL.that.showDialog(msgBox);
		return msgBox;
	}

	/**
	 ** show msgbox for input of double
	 **/
	public static GL_MsgBox Show(String msg, String title, double initialValue, returnValueListnerDouble Listner)
	{
		mReturnListnerDouble = Listner;
		mType = type.doubleType;
		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize(msg, true, true, false);

		float margin = UI_Size_Base.that.getMargin();
		GL_MsgBox msgBox = new GL_MsgBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		CB_RectF numPadRec = new CB_RectF(0, 0, msgBoxSize.width, UI_Size_Base.that.getButtonHeight() * 6);

		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		textFieldRec.setHeight(Fonts.getNormal().getLineHeight() * 1.6f);

		editText = new EditWrapedTextField(msgBox, textFieldRec, "MsgBoxLabel");
		editText.dontShowSoftKeyBoardOnFocus();
		editText.setZeroPos();
		editText.setY(margin * 3);
		editText.setText(String.valueOf(initialValue));
		editText.setCursorPosition((String.valueOf(initialValue)).length());

		float topBottom = editText.getStyle().background.getTopHeight() + editText.getStyle().background.getBottomHeight();
		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);

		editText.setHeight(topBottom + SingleLineHeight);

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

		NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.Type.withOkCancel, getkeyListner(msgBox));
		numPad.setY(margin);

		msgBox.initRow(BOTTOMUP, margin);
		msgBox.addLast(numPad);
		msgBox.setFooterHeight(msgBox.getHeightFromBottom());

		GL.that.showDialog(msgBox);

		return msgBox;
	}

	/**
	 ** show msgbox for input of min + sec (int)
	 **/
	public static GL_MsgBox Show(String msg, String title, int initialMin, int initialSec, returnValueListnerTime Listner)
	{
		mReturnListnerTime = Listner;
		mType = type.timeType;

		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize(msg, true, true, false);

		float margin = UI_Size_Base.that.getMargin();
		GL_MsgBox msgBox = new GL_MsgBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		CB_RectF numPadRec = new CB_RectF(0, 0, msgBoxSize.width, UI_Size_Base.that.getButtonHeight() * 6);

		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		textFieldRec.setHeight(Fonts.getNormal().getLineHeight() * 1.6f);

		editText = new EditWrapedTextField(msgBox, textFieldRec, "MsgBoxLabel");
		editText.dontShowSoftKeyBoardOnFocus();
		editText.setZeroPos();
		editText.setY(margin * 3);

		String initialValue = String.valueOf(initialMin) + ":" + String.valueOf(initialSec);

		editText.setText(String.valueOf(initialValue));
		editText.setCursorPosition((String.valueOf(initialValue)).length());

		float topBottom = editText.getStyle().background.getTopHeight() + editText.getStyle().background.getBottomHeight();
		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);

		editText.setHeight(topBottom + SingleLineHeight);

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

		NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.Type.withDoubleDotOkCancel, getkeyListner(msgBox));
		numPad.setY(margin);

		msgBox.initRow(BOTTOMUP, margin);
		msgBox.addLast(numPad);
		msgBox.setFooterHeight(msgBox.getHeightFromBottom());

		GL.that.showDialog(msgBox);

		return msgBox;
	}

	public interface returnValueListner
	{
		public void returnValue(int value);

		public void cancelClicked();
	}

	public interface returnValueListnerDouble
	{
		public void returnValue(double value);

		public void cancelClicked();
	}

	public interface returnValueListnerTime
	{
		public void returnValue(int min, int sec);

		public void cancelClicked();
	}

	static keyEventListner getkeyListner(final GL_MsgBox msgBox)
	{

		keyEventListner keyListner = new keyEventListner()
		{

			@Override
			public void KeyPressed(String value)
			{
				if (editText == null || value == null) return;
				char c = value.charAt(0);

				switch (c)
				{
				case 'O':
					String StringValue = editText.getText();

					// Replase Linebraek
					StringValue = StringValue.replace("\n", "");
					StringValue = StringValue.replace("\r", "");

					boolean ParseError = false;

					if (mType == type.doubleType)
					{
						if (mReturnListnerDouble != null)
						{
							try
							{
								double dblValue = Double.parseDouble(StringValue);
								mReturnListnerDouble.returnValue(dblValue);
							}
							catch (NumberFormatException e)
							{
								ParseError = true;
							}
						}
					}
					else if (mType == type.intType)
					{
						if (mReturnListner != null)
						{
							try
							{

								int intValue = Integer.parseInt(StringValue);
								mReturnListner.returnValue(intValue);
							}
							catch (NumberFormatException e)
							{
								ParseError = true;
							}
						}
					}

					else if (mType == type.timeType)
					{
						if (mReturnListnerTime != null)
						{
							try
							{
								String[] s = StringValue.split(":");

								int intValueMin = Integer.parseInt(s[0]);
								int intValueSec = Integer.parseInt(s[1]);
								mReturnListnerTime.returnValue(intValueMin, intValueSec);
							}
							catch (NumberFormatException e)
							{
								ParseError = true;
							}
						}
					}

					if (ParseError)
					{
						GL.that.Toast(Translation.Get("wrongValueEnterd"));
					}
					else
					{
						close(msgBox);
					}
					break;

				case 'C':
					if (mType == type.doubleType)

					{
						if (mReturnListnerDouble != null)
						{
							mReturnListnerDouble.cancelClicked();
						}
					}
					else if (mType == type.intType)
					{
						if (mReturnListner != null)
						{
							mReturnListner.cancelClicked();
						}
					}
					else if (mType == type.timeType)
					{
						if (mReturnListnerTime != null)
						{
							mReturnListnerTime.cancelClicked();
						}
					}

					close(msgBox);
					break;

				case '<':
					editText.keyDown(Keys.LEFT);
					break;

				case '>':
					editText.keyDown(Keys.RIGHT);
					break;

				case 'D':
					editText.keyTyped(EditWrapedTextField.DELETE);
					break;
				case 'B':
					editText.keyTyped(EditWrapedTextField.BACKSPACE);
					break;

				default:
					editText.keyTyped(c);
				}

			}
		};
		return keyListner;
	}

	@Override
	public void onShow()
	{
		super.onShow();
		editText.setFocus();
	}

	@Override
	protected void Initial()
	{
	}

	@Override
	protected void SkinIsChanged()
	{
	}

	private static void close(final GL_MsgBox msgBox)
	{
		GL.that.RunOnGL(new runOnGL()
		{

			@Override
			public void run()
			{
				GL.that.closeDialog(msgBox);
			}
		});

	}

}
