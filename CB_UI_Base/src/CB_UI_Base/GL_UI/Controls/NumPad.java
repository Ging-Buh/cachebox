package CB_UI_Base.GL_UI.Controls;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.Math.CB_RectF;
import com.badlogic.gdx.Input.Keys;

public class NumPad extends CB_View_Base {

    private final IKeyEventListener mKeyPressedListener;
    private final Type mType;
    OnClickListener clickListener = new OnClickListener() {

        @Override
        public boolean onClick(final GL_View_Base v, int x, int y, int pointer, int button) {
            if (v instanceof Button) {
                Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        if (mKeyPressedListener != null) {
                            mKeyPressedListener.KeyPressed(((Button) v).getText());
                        }
                    }
                });
                t.start();
            }
            return true;
        }
    };
    private Button btn_0;
    private Button btn_1;
    private Button btn_2;
    private Button btn_3;
    private Button btn_4;
    private Button btn_5;
    private Button btn_6;
    private Button btn_7;
    private Button btn_8;
    private Button btn_9;
    private Button btn_Dot;
    private Button btn_Del;
    private Button btn_Bck;
    private Button btn_left;
    private Button btn_right;
    private Button btn_OK;
    private Button btn_Cancel;

    private CB_RectF btnRec;
    private CB_RectF btnRecWide;
    private CB_RectF btnRecHalfWide;
    private EditTextField focusedTextField = null;
    IKeyEventListener ownKeyListener = new IKeyEventListener() {

        @Override
        public void KeyPressed(String value) {
            if (focusedTextField == null || value == null)
                return;

            char c = value.charAt(0);

            switch (c) {
                case '<':
                    focusedTextField.cursorLeftRight(-1);
                    // focusedTextField.clearSelection();
                    break;

                case '>':
                    focusedTextField.keyDown(Keys.RIGHT);
                    break;

                case 'D':
                    focusedTextField.keyTyped(EditTextField.DELETE);
                    break;
                case 'B':
                    focusedTextField.keyTyped(EditTextField.BACKSPACE);
                    break;

                default:
                    focusedTextField.keyTyped(c);
            }

        }
    };

    public NumPad(CB_RectF rec, String Name, Type type, IKeyEventListener listener) {
        super(rec, Name);
        mType = type;
        mKeyPressedListener = listener;
    }

    public NumPad(CB_RectF rec, String Name, Type type) {
        super(rec, Name);
        mType = type;
        mKeyPressedListener = ownKeyListener;
    }

    @Override
    protected void Initial() {
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
        if (mType == Type.withDot || mType == Type.withOkCancel || mType == Type.withDoubleDotOkCancel) {
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

        if (mType == Type.withOkCancel || mType == Type.withoutDotOkCancel || mType == Type.withDoubleDotOkCancel) {
            btn_Cancel.setPos(btn_6.getMaxX(), y);
            this.addChild(btn_Cancel);
        }

        y = btn_4.getMaxY();
        btn_1.setPos(left, y);
        btn_2.setPos(btn_1.getMaxX(), y);
        btn_3.setPos(btn_2.getMaxX(), y);

        if (mType == Type.withOkCancel || mType == Type.withoutDotOkCancel || mType == Type.withDoubleDotOkCancel) {
            btn_OK.setPos(btn_3.getMaxX(), y);
            this.addChild(btn_OK);
        }

    }

    private void calcSizes() {

        this.setOnClickListener(clickListener);

        float btnHeight = this.getHeight() / 5f;
        float btnWidth = this.getWidth() / 5f;
        float minValue = Math.min(btnHeight, btnWidth);

        btnRec = new CB_RectF(0, 0, minValue, minValue);
        btnRecWide = new CB_RectF(0, 0, minValue * 2, minValue);
        btnRecHalfWide = new CB_RectF(0, 0, minValue * 1.5f, minValue);

        btn_0 = new Button(btnRec, "btn 0");
        btn_1 = new Button(btnRec, "btn 1");
        btn_2 = new Button(btnRec, "btn 2");
        btn_3 = new Button(btnRec, "btn 3");
        btn_4 = new Button(btnRec, "btn 4");
        btn_5 = new Button(btnRec, "btn 5");
        btn_6 = new Button(btnRec, "btn 6");
        btn_7 = new Button(btnRec, "btn 7");
        btn_8 = new Button(btnRec, "btn 8");
        btn_9 = new Button(btnRec, "btn 9");

        btn_Dot = new Button(btnRec, "btn Dot");
        btn_Del = new Button(btnRec, "btn Del");
        btn_Bck = new Button(btnRec, "btn_Bck");

        btn_left = new Button(btnRecHalfWide, "btn left");
        btn_right = new Button(btnRecHalfWide, "btn right");

        btn_OK = new Button(btnRecWide, "btn OK");
        btn_Cancel = new Button(btnRecWide, "btn Cancel");

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

        if (mType == Type.withDoubleDotOkCancel)
            btn_Dot.setText(":");
        else
            btn_Dot.setText(".");

        btn_Del.setText("Del");
        btn_Bck.setText("Back");
        btn_OK.setText(Translation.Get("ok"));
        btn_Cancel.setText(Translation.Get("cancel"));
        btn_left.setText("<");
        btn_right.setText(">");

        btn_0.setOnClickListener(clickListener);
        btn_1.setOnClickListener(clickListener);
        btn_2.setOnClickListener(clickListener);
        btn_3.setOnClickListener(clickListener);
        btn_4.setOnClickListener(clickListener);
        btn_5.setOnClickListener(clickListener);
        btn_6.setOnClickListener(clickListener);
        btn_7.setOnClickListener(clickListener);
        btn_8.setOnClickListener(clickListener);
        btn_9.setOnClickListener(clickListener);
        btn_Dot.setOnClickListener(clickListener);

        btn_left.setOnClickListener(clickListener);
        btn_right.setOnClickListener(clickListener);

        btn_OK.setOnClickListener(new OnClickListener() {

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
        btn_Cancel.setOnClickListener(new OnClickListener() {

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

        btn_Del.setOnClickListener(new OnClickListener() {

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

        btn_Bck.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (mKeyPressedListener != null) {
                    PlatformConnector.vibrate();
                    mKeyPressedListener.KeyPressed("B");
                    return true;
                }
                return false;
            }
        });

    }

    // ######## Register TextFields

    public void registerTextField(final EditTextField textField) {
        focusedTextField = textField;
        /*
        textField.setOnscreenKeyboard(new OnscreenKeyboard() {
            @Override
            public void show(boolean arg0) {
                textField.setFocus(true);
                focusedTextField = textField;
            }
        });
        */

        textField.disableKeyboardPopup();
    }

    public enum Type {
        withDot, withOkCancel, withoutDotOkCancel, withDoubleDotOkCancel,
    }

    public interface IKeyEventListener {
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
