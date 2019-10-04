package CB_UI_Base.GL_UI.Controls;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.Input.Keys;

/**
 * depending on the constuctor
 * without IKeyPressedListener: you can give a TextField to be filled (by registerTextField)
 * or
 * with IKeyPressedListener: handle the values from the pressed Buttons by this listener (set with the constructor)
 * The NumPadType gives the different layout of the NumPad
 */
public class NumPad extends CB_View_Base {
    public enum NumPadType {
        withDot, withOkCancel, withoutDotOkCancel, withDoubleDotOkCancel,
    }
    private final NumPadType mNumPadType;
    private final IKeyPressedListener mKeyPressedListener;
    private EditTextField textField;
    private CB_Button btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9, btn_Dot, btn_left, btn_right;
    OnClickListener onBtnClick = new OnClickListener() {
        @Override
        public boolean onClick(final GL_View_Base v, int x, int y, int pointer, int button) {
            if (v instanceof CB_Button) {
                Thread t = new Thread(() -> {
                    if (mKeyPressedListener != null) {
                        mKeyPressedListener.KeyPressed(((CB_Button) v).getText());
                    }
                });
                t.start();
            }
            return true;
        }
    };
    private CB_Button btn_Del, btn_Bck;
    private CB_Button btn_OK, btn_Cancel;

    private CB_RectF btnRec;
    private CB_RectF btnRecWide;
    private CB_RectF btnRecHalfWide;

    public NumPad(CB_RectF rec, String Name, NumPadType numPadType, IKeyPressedListener listener) {
        super(rec, Name);
        mNumPadType = numPadType;
        mKeyPressedListener = listener;
    }

    public NumPad(CB_RectF rec, String Name, NumPadType numPadType) {
        super(rec, Name);
        textField = null; // use registerTextField
        mNumPadType = numPadType;
        mKeyPressedListener = value -> {
            if (textField == null || value == null)
                return;

            char c = value.charAt(0);

            switch (c) {
                case '<':
                    textField.cursorLeftRight(-1);
                    // textField.clearSelection();
                    break;
                case '>':
                    textField.keyDown(Keys.RIGHT);
                    break;
                case 'D':
                    textField.keyTyped(EditTextField.DELETE);
                    break;
                case 'B':
                    textField.keyTyped(EditTextField.BACKSPACE);
                    break;
                default:
                    textField.keyTyped(c);
            }

        };
    }

    @Override
    protected void initialize() {
        this.removeChilds();
        calcSizes();
        calcPositions();

        this.addChild(btn_left);
        this.addChild(btn_right);
        this.addChild(btn_7);
        this.addChild(btn_8);
        this.addChild(btn_9);
        this.addChild(btn_0);
        this.addChild(btn_Del);
        this.addChild(btn_Bck);
        this.addChild(btn_4);
        this.addChild(btn_5);
        this.addChild(btn_6);
        this.addChild(btn_1);
        this.addChild(btn_2);
        this.addChild(btn_3);
    }

    private void calcPositions() {
        float center = this.getWidth() / 2;
        float left = (this.getWidth() - (btnRec.getWidth() * 5)) / 2;

        // LastLine
        btn_left.setX(center - btn_left.getWidth());
        btn_right.setX(center);

        //
        float y = btn_left.getMaxY();
        btn_0.setPos(left + btn_0.getWidth(), y);
        btn_Del.setPos(btn_0.getMaxX() + btn_0.getWidth(), y);
        btn_Bck.setPos(btn_Del.getMaxX(), y);
        if (mNumPadType == NumPadType.withDot || mNumPadType == NumPadType.withOkCancel || mNumPadType == NumPadType.withDoubleDotOkCancel) {
            btn_Dot.setPos(btn_0.getMaxX(), y);
            this.addChild(btn_Dot);
        }

        // third line
        y = btn_0.getMaxY();
        btn_7.setPos(left, y);
        btn_8.setPos(btn_7.getMaxX(), y);
        btn_9.setPos(btn_8.getMaxX(), y);

        y = btn_7.getMaxY();
        btn_4.setPos(left, y);
        btn_5.setPos(btn_4.getMaxX(), y);
        btn_6.setPos(btn_5.getMaxX(), y);

        if (mNumPadType == NumPadType.withOkCancel || mNumPadType == NumPadType.withoutDotOkCancel || mNumPadType == NumPadType.withDoubleDotOkCancel) {
            btn_Cancel.setPos(btn_6.getMaxX(), y);
            this.addChild(btn_Cancel);
        }

        y = btn_4.getMaxY();
        btn_1.setPos(left, y);
        btn_2.setPos(btn_1.getMaxX(), y);
        btn_3.setPos(btn_2.getMaxX(), y);

        if (mNumPadType == NumPadType.withOkCancel || mNumPadType == NumPadType.withoutDotOkCancel || mNumPadType == NumPadType.withDoubleDotOkCancel) {
            btn_OK.setPos(btn_3.getMaxX(), y);
            this.addChild(btn_OK);
        }

    }

    private void calcSizes() {

        this.addClickHandler(onBtnClick);

        float btnHeight = this.getHeight() / 5f;
        float btnWidth = this.getWidth() / 5f;
        float minValue = Math.min(btnHeight, btnWidth);

        btnRec = new CB_RectF(0, 0, minValue, minValue);
        btnRecWide = new CB_RectF(0, 0, minValue * 2, minValue);
        btnRecHalfWide = new CB_RectF(0, 0, minValue * 1.5f, minValue);

        btn_0 = new CB_Button(btnRec, "btn 0");
        btn_1 = new CB_Button(btnRec, "btn 1");
        btn_2 = new CB_Button(btnRec, "btn 2");
        btn_3 = new CB_Button(btnRec, "btn 3");
        btn_4 = new CB_Button(btnRec, "btn 4");
        btn_5 = new CB_Button(btnRec, "btn 5");
        btn_6 = new CB_Button(btnRec, "btn 6");
        btn_7 = new CB_Button(btnRec, "btn 7");
        btn_8 = new CB_Button(btnRec, "btn 8");
        btn_9 = new CB_Button(btnRec, "btn 9");

        btn_Dot = new CB_Button(btnRec, "btn Dot");
        btn_Del = new CB_Button(btnRec, "btn Del");
        btn_Bck = new CB_Button(btnRec, "btn_Bck");

        btn_left = new CB_Button(btnRecHalfWide, "btn left");
        btn_right = new CB_Button(btnRecHalfWide, "btn right");

        btn_OK = new CB_Button(btnRecWide, "btn OK");
        btn_Cancel = new CB_Button(btnRecWide, "btn Cancel");

        // set captions
        btn_0.setText("0");
        btn_1.setText("1");
        btn_2.setText("2");
        btn_3.setText("3");
        btn_4.setText("4");
        btn_5.setText("5");
        btn_6.setText("6");
        btn_7.setText("7");
        btn_8.setText("8");
        btn_9.setText("9");

        if (mNumPadType == NumPadType.withDoubleDotOkCancel)
            btn_Dot.setText(":");
        else
            btn_Dot.setText(".");

        btn_Del.setText("Del");
        btn_Bck.setText("Back");
        btn_OK.setText(Translation.get("ok"));
        btn_Cancel.setText(Translation.get("cancel"));
        btn_left.setText("<");
        btn_right.setText(">");

        btn_0.addClickHandler(onBtnClick);
        btn_1.addClickHandler(onBtnClick);
        btn_2.addClickHandler(onBtnClick);
        btn_3.addClickHandler(onBtnClick);
        btn_4.addClickHandler(onBtnClick);
        btn_5.addClickHandler(onBtnClick);
        btn_6.addClickHandler(onBtnClick);
        btn_7.addClickHandler(onBtnClick);
        btn_8.addClickHandler(onBtnClick);
        btn_9.addClickHandler(onBtnClick);
        btn_Dot.addClickHandler(onBtnClick);

        btn_left.addClickHandler(onBtnClick);
        btn_right.addClickHandler(onBtnClick);

        btn_OK.addClickHandler(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (mKeyPressedListener != null) {
                    PlatformConnector.vibrate();
                    mKeyPressedListener.KeyPressed("O");
                    return true;
                }
                return false;
            }
        });
        btn_Cancel.addClickHandler(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (mKeyPressedListener != null) {
                    PlatformConnector.vibrate();
                    mKeyPressedListener.KeyPressed("C");
                    return true;
                }
                return false;
            }
        });

        btn_Del.addClickHandler(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (mKeyPressedListener != null) {
                    PlatformConnector.vibrate();
                    mKeyPressedListener.KeyPressed("D");
                    return true;
                }
                return false;
            }
        });

        btn_Bck.addClickHandler((v, x, y, pointer, button) -> {
            if (mKeyPressedListener != null) {
                PlatformConnector.vibrate();
                mKeyPressedListener.KeyPressed("B");
                return true;
            }
            return false;
        });

    }

    public void registerTextField(final EditTextField textField) {
        this.textField = textField;
        textField.disableKeyboardPopup();
    }

    public interface IKeyPressedListener {
        /**
         * Value hat den Wert 0-9 oder "." <br>
         * oder <br>
         * "D" für Delete Button<br>
         * "O" für Ok Button<br>
         * "C" für Cancel Button<br>
         * "<" für Left Button<br>
         * ">" für Right Button<br>
         *
         * @param value
         */
        void KeyPressed(String value);
    }

}
