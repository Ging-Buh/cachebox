package CB_UI_Base.GL_UI.Controls.Dialogs;

import com.badlogic.gdx.Input.Keys;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase.OnscreenKeyboard;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.NumPad;
import CB_UI_Base.GL_UI.Controls.NumPad.IKeyEventListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.UI_Size_Base;

public class NumericInputBox extends CB_View_Base {

	public NumericInputBox(String name) {
		super(name);
	}

	private enum type {
		intType, doubleType, timeType
	}

	private static type mType;

	private static EditTextField editText;

	public static IReturnValueListener mReturnListener;
	public static IReturnValueListenerDouble mReturnListenerDouble;
	public static IReturnValueListenerTime mReturnListenerTime;

	/**
	 ** show msgbox for input of int
	 **/
	public static GL_MsgBox Show(String msg, String title, int initialValue, IReturnValueListener listener) {
		mReturnListener = listener;
		mType = type.intType;

		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize(msg, true, true, false);

		float margin = UI_Size_Base.that.getMargin();
		GL_MsgBox msgBox = new GL_MsgBox(msgBoxSize, "MsgBox");

		editText = new EditTextField("NumerikInputBox editText");
		float topBottom = editText.getStyle().getTopHeight(true) + editText.getStyle().getBottomHeight(true); // true if focused
		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);
		editText.setHeight(topBottom + SingleLineHeight);

		Label label = new Label("MsgBoxLabel");

		CB_RectF numPadRec = new CB_RectF(0, 0, msgBoxSize.width, UI_Size_Base.that.getButtonHeight() * 6);
		msgBox.setHeight(msgBox.getHeight() + editText.getHeight() + numPadRec.getHeight());

		msgBox.setMargins(0, margin);
		msgBox.setBorders(margin, margin);

		NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.Type.withoutDotOkCancel, getKeyListener(msgBox));

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
		editText.setOnscreenKeyboard(new OnscreenKeyboard() {

			@Override
			public void show(boolean arg0) {
				// do nothing, don�t show Keybord
			}
		});
		editText.setFocus(true);

		GL.that.showDialog(msgBox);
		return msgBox;
	}

	/**
	 ** show msgbox for input of double
	 **/
	public static GL_MsgBox Show(String msg, String title, double initialValue, IReturnValueListenerDouble listener) {
		mReturnListenerDouble = listener;
		mType = type.doubleType;
		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize(msg, true, true, false);

		float margin = UI_Size_Base.that.getMargin();
		GL_MsgBox msgBox = new GL_MsgBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		CB_RectF numPadRec = new CB_RectF(0, 0, msgBoxSize.width, UI_Size_Base.that.getButtonHeight() * 6);

		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		textFieldRec.setHeight(Fonts.getNormal().getLineHeight() * 1.6f);

		editText = new EditTextField(textFieldRec, msgBox, "NumerikInputBox editText");
		editText.dontShowSoftKeyBoardOnFocus();
		editText.setZeroPos();
		editText.setY(margin * 3);
		editText.setText(String.valueOf(initialValue));
		editText.setCursorPosition((String.valueOf(initialValue)).length());

		float topBottom = editText.getStyle().getTopHeight(true) + editText.getStyle().getBottomHeight(true); // true if focused
		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);

		editText.setHeight(topBottom + SingleLineHeight);

		editText.setOnscreenKeyboard(new OnscreenKeyboard() {

			@Override
			public void show(boolean arg0) {
				// do nothing, don�t show Keybord
			}
		});
		editText.setFocus(true);

		CB_RectF LabelRec = msgBox.getContentSize().getBounds();
		LabelRec.setHeight(LabelRec.getHeight() - textFieldRec.getHeight());

		Label label = new Label("NumerikInputBox" + " label", LabelRec);
		label.setZeroPos();
		label.setY(editText.getMaxY() + margin);
		label.setWrappedText(msg);
		msgBox.addChild(label);

		msgBox.setHeight(msgBox.getHeight() + editText.getHeight() + numPadRec.getHeight());

		msgBox.addChild(editText);

		// ######### NumPad ################

		NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.Type.withOkCancel, getKeyListener(msgBox));
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
	public static GL_MsgBox Show(String msg, String title, int initialMin, int initialSec, IReturnValueListenerTime listener) {
		mReturnListenerTime = listener;
		mType = type.timeType;

		Size msgBoxSize = GL_MsgBox.calcMsgBoxSize(msg, true, true, false);

		float margin = UI_Size_Base.that.getMargin();
		GL_MsgBox msgBox = new GL_MsgBox(msgBoxSize, "MsgBox");
		msgBox.setTitle(title);

		CB_RectF numPadRec = new CB_RectF(0, 0, msgBoxSize.width, UI_Size_Base.that.getButtonHeight() * 6);

		CB_RectF textFieldRec = msgBox.getContentSize().getBounds();

		textFieldRec.setHeight(Fonts.getNormal().getLineHeight() * 1.6f);

		editText = new EditTextField(textFieldRec, msgBox, "NumerikInputBox editText");
		editText.dontShowSoftKeyBoardOnFocus();
		editText.setZeroPos();
		editText.setY(margin * 3);

		String initialValue = String.valueOf(initialMin) + ":" + String.valueOf(initialSec);

		editText.setText(String.valueOf(initialValue));
		editText.setCursorPosition((String.valueOf(initialValue)).length());

		float topBottom = editText.getStyle().getTopHeight(true) + editText.getStyle().getBottomHeight(true); // true if focused
		float SingleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);

		editText.setHeight(topBottom + SingleLineHeight);

		editText.setOnscreenKeyboard(new OnscreenKeyboard() {

			@Override
			public void show(boolean arg0) {
				// do nothing, don�t show Keybord
			}
		});
		editText.setFocus(true);

		CB_RectF LabelRec = msgBox.getContentSize().getBounds();
		LabelRec.setHeight(LabelRec.getHeight() - textFieldRec.getHeight());

		Label label = new Label("NumerikInputBox" + " label", LabelRec);
		label.setZeroPos();
		label.setY(editText.getMaxY() + margin);
		label.setWrappedText(msg);
		msgBox.addChild(label);

		msgBox.setHeight(msgBox.getHeight() + editText.getHeight() + numPadRec.getHeight());

		msgBox.addChild(editText);

		// ######### NumPad ################

		NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.Type.withDoubleDotOkCancel, getKeyListener(msgBox));
		numPad.setY(margin);

		msgBox.initRow(BOTTOMUP, margin);
		msgBox.addLast(numPad);
		msgBox.setFooterHeight(msgBox.getHeightFromBottom());

		GL.that.showDialog(msgBox);

		return msgBox;
	}

	public interface IReturnValueListener {
		public void returnValue(int value);

		public void cancelClicked();
	}

	public interface IReturnValueListenerDouble {
		public void returnValue(double value);

		public void cancelClicked();
	}

	public interface IReturnValueListenerTime {
		public void returnValue(int min, int sec);

		public void cancelClicked();
	}

	static IKeyEventListener getKeyListener(final GL_MsgBox msgBox) {

		IKeyEventListener keyListener = new IKeyEventListener() {

			@Override
			public void KeyPressed(String value) {
				if (editText == null || value == null)
					return;
				char c = value.charAt(0);

				switch (c) {
				case 'O':
					String StringValue = editText.getText();

					// Replase Linebraek
					StringValue = StringValue.replace("\n", "");
					StringValue = StringValue.replace("\r", "");

					boolean ParseError = false;

					if (mType == type.doubleType) {
						if (mReturnListenerDouble != null) {
							try {
								double dblValue = Double.parseDouble(StringValue);
								mReturnListenerDouble.returnValue(dblValue);
							} catch (NumberFormatException e) {
								ParseError = true;
							}
						}
					} else if (mType == type.intType) {
						if (mReturnListener != null) {
							try {

								int intValue = Integer.parseInt(StringValue);
								mReturnListener.returnValue(intValue);
							} catch (NumberFormatException e) {
								ParseError = true;
							}
						}
					}

					else if (mType == type.timeType) {
						if (mReturnListenerTime != null) {
							try {
								String[] s = StringValue.split(":");

								int intValueMin = Integer.parseInt(s[0]);
								int intValueSec = Integer.parseInt(s[1]);
								mReturnListenerTime.returnValue(intValueMin, intValueSec);
							} catch (NumberFormatException e) {
								ParseError = true;
							}
						}
					}

					if (ParseError) {
						GL.that.Toast(Translation.Get("wrongValue"));
					} else {
						close(msgBox);
					}
					break;

				case 'C':
					if (mType == type.doubleType)

					{
						if (mReturnListenerDouble != null) {
							mReturnListenerDouble.cancelClicked();
						}
					} else if (mType == type.intType) {
						if (mReturnListener != null) {
							mReturnListener.cancelClicked();
						}
					} else if (mType == type.timeType) {
						if (mReturnListenerTime != null) {
							mReturnListenerTime.cancelClicked();
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
					editText.keyTyped(EditTextField.DELETE);
					break;
				case 'B':
					editText.keyTyped(EditTextField.BACKSPACE);
					break;

				default:
					editText.keyTyped(c);
				}

			}
		};
		return keyListener;
	}

	@Override
	public void onShow() {
		super.onShow();
		editText.setFocus(true);
	}

	@Override
	protected void Initial() {
	}

	@Override
	protected void SkinIsChanged() {
	}

	private static void close(final GL_MsgBox msgBox) {
		GL.that.RunOnGL(new IRunOnGL() {

			@Override
			public void run() {
				GL.that.closeDialog(msgBox);
			}
		});

	}

}
