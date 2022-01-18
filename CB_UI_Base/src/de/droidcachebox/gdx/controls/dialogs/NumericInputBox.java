package de.droidcachebox.gdx.controls.dialogs;

import com.badlogic.gdx.Input.Keys;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.EditTextField;
import de.droidcachebox.gdx.controls.NumPad;
import de.droidcachebox.gdx.controls.NumPad.IKeyPressedListener;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;

public class NumericInputBox extends ButtonDialog {

    public static IReturnValueListener mReturnListener;
    public static IReturnValueListenerDouble mReturnListenerDouble;
    public static IReturnValueListenerTime mReturnListenerTime;
    private static Type mType; // using a String (or object or generic ) as return value would reduce code here
    private static EditTextField editText;

    public NumericInputBox(String msg, String title) {
        super(msg, title, MsgBoxButton.NOTHING, MsgBoxIcon.None);
        newContentBox();

        editText = new EditTextField(null, "NumericInputBox editText");
        float topBottom = editText.getStyle().getTopHeight(true) + editText.getStyle().getBottomHeight(true); // true if focused
        float singleLineHeight = editText.getFont().getLineHeight() + (editText.getFont().getAscent() * 4);
        editText.setHeight(topBottom + singleLineHeight);
        editText.disableKeyboardPopup();

        CB_Label label = new CB_Label();
        label.setWrappedText(msg);

        contentBox.addLast(label);
        contentBox.addLast(editText);
    }

    public void initIntInput(int initialValue, IReturnValueListener listener) {
        mReturnListener = listener;
        mType = Type.intType;
        editText.setText(String.valueOf(initialValue));
        editText.setCursorPosition((String.valueOf(initialValue)).length());
        CB_RectF numPadRec = new CB_RectF(0, 0, contentBox.getWidth(), UiSizes.getInstance().getButtonHeight() * 6);
        NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.NumPadType.withoutDotOkCancel, getKeyListener());
        contentBox.addLast(numPad);
        readyContentBox();
    }

    public void initDoubleInput(String initialValue, IReturnValueListenerDouble listener) {
        // initialValue is String cause implicit conversion of float to double gives ugly results
        mReturnListenerDouble = listener;
        mType = Type.doubleType;
        editText.setText(initialValue);
        editText.setCursorPosition(initialValue.length());
        CB_RectF numPadRec = new CB_RectF(0, 0, contentBox.getWidth(), UiSizes.getInstance().getButtonHeight() * 6);
        NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.NumPadType.withOkCancel, getKeyListener());
        contentBox.addLast(numPad);
        readyContentBox();
    }

    /**
     * Time here means minutes and seconds (not really used anywhere)
     * simply two integers separated by :
     * there is no value check
     *
     * @param initialMin minute value to change
     * @param initialSec seconds value to change
     * @param listener   for handling the result
     */
    public void initTimeInput(int initialMin, int initialSec, IReturnValueListenerTime listener) {
        mReturnListenerTime = listener;
        mType = Type.timeType;
        String initialValue = "" + initialMin + ":" + initialSec;
        editText.setText(initialValue);
        editText.setCursorPosition(initialValue.length());
        CB_RectF numPadRec = new CB_RectF(0, 0, contentBox.getWidth(), UiSizes.getInstance().getButtonHeight() * 6);
        NumPad numPad = new NumPad(numPadRec, "NumPad", NumPad.NumPadType.withDoubleDotOkCancel, getKeyListener());
        contentBox.addLast(numPad);
        readyContentBox();
    }

    private IKeyPressedListener getKeyListener() {
        return value -> {
            if (editText == null || value == null)
                return;
            char c = value.charAt(0);

            switch (c) {
                case 'O':
                    String inputString = editText.getText();

                    // replace linebreak
                    inputString = inputString.replace("\n", "");
                    inputString = inputString.replace("\r", "");

                    boolean ParseError = false;

                    if (mType == Type.doubleType) {
                        if (mReturnListenerDouble != null) {
                            try {
                                double dblValue = Double.parseDouble(inputString);
                                mReturnListenerDouble.returnValue(dblValue);
                            } catch (NumberFormatException e) {
                                ParseError = true;
                            }
                        }
                    } else if (mType == Type.intType) {
                        if (mReturnListener != null) {
                            try {
                                int intValue = Integer.parseInt(inputString);
                                mReturnListener.returnValue(intValue);
                            } catch (NumberFormatException e) {
                                ParseError = true;
                            }
                        }
                    } else if (mType == Type.timeType) {
                        if (mReturnListenerTime != null) {
                            try {
                                String[] s = inputString.split(":");

                                int intValueMin = Integer.parseInt(s[0]);
                                int intValueSec = Integer.parseInt(s[1]);
                                mReturnListenerTime.returnValue(intValueMin, intValueSec);
                            } catch (NumberFormatException e) {
                                ParseError = true;
                            }
                        }
                    }

                    if (ParseError) {
                        GL.that.toast(Translation.get("wrongValue"));
                    } else {
                        close();
                    }
                    break;

                case 'C':
                    if (mType == Type.doubleType) {
                        if (mReturnListenerDouble != null) {
                            mReturnListenerDouble.cancelClicked();
                        }
                    } else if (mType == Type.intType) {
                        if (mReturnListener != null) {
                            mReturnListener.cancelClicked();
                        }
                    } else if (mType == Type.timeType) {
                        if (mReturnListenerTime != null) {
                            mReturnListenerTime.cancelClicked();
                        }
                    }

                    close();
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

        };
    }

    @Override
    public void onShow() {
        editText.setFocus(true);
    }

    private enum Type {
        intType, doubleType, timeType
    }

    public interface IReturnValueListener {
        void returnValue(int value);

        void cancelClicked();
    }

    public interface IReturnValueListenerDouble {
        void returnValue(double value);

        void cancelClicked();
    }

    public interface IReturnValueListenerTime {
        void returnValue(int min, int sec);

        void cancelClicked();
    }

}
