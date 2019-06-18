package CB_UI.GL_UI.Controls.Dialogs;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Controls.CB_Label.VAlignment;
import CB_UI_Base.GL_UI.Controls.MessageBox.ButtonDialog;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.Size;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class RouteDialog extends ButtonDialog {

    int state = -1;
    private Linearlayout layout;
    private float TextFieldHeight;
    private SizeF msgBoxContentSize;
    private ImageMultiToggleButton btMotoWay, btCycleWay, btFootWay;
    private CB_CheckBox chkTmc;
    private IReturnListener mReturnListener;

    public RouteDialog(IReturnListener listener) {
        super(Menu.getMenuRec(), "PW-Dialog", "", Translation.get("RouteToWaypoit"), MessageBoxButtons.OKCancel, null, null);
        mReturnListener = listener;

        msgBoxContentSize = getContentSize();
        // initial VariableField
        TextFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;

        float innerWidth = msgBoxContentSize.width + leftBorder + rightBorder;

        layout = new Linearlayout(innerWidth, "Layout");
        layout.setX(0);
        // layout.setBackground(new ColorDrawable(Color.GREEN));

        CB_RectF MTBRec = new CB_RectF(0, 0, innerWidth / 3, UI_Size_Base.that.getButtonHeight() * 2);

        btMotoWay = new ImageMultiToggleButton(MTBRec, "btMotoWay");
        btCycleWay = new ImageMultiToggleButton(MTBRec, "btCycleWay");
        btFootWay = new ImageMultiToggleButton(MTBRec, "btFootWay");

        btMotoWay.setImage(new SpriteDrawable(Sprites.getSprite("pictureBox2")));
        btCycleWay.setImage(new SpriteDrawable(Sprites.getSprite("pictureBox1")));
        btFootWay.setImage(new SpriteDrawable(Sprites.getSprite("pictureBox3")));

        btMotoWay.setX(0);
        btCycleWay.setX(btMotoWay.getMaxX());
        btFootWay.setX(btCycleWay.getMaxX());

        Box box = new Box(new CB_RectF(0, 0, innerWidth, UI_Size_Base.that.getButtonHeight() * 2), "");

        box.addChild(btMotoWay);
        box.addChild(btCycleWay);
        box.addChild(btFootWay);

        layout.addChild(box);

        btMotoWay.initialOn_Off_ToggleStates("", "");
        btCycleWay.initialOn_Off_ToggleStates("", "");
        btFootWay.initialOn_Off_ToggleStates("", "");

        Box box2 = new Box(new CB_RectF(0, 0, innerWidth, UI_Size_Base.that.getButtonHeight()), "");
        chkTmc = new CB_CheckBox("TMC");
        box2.addChild(chkTmc);

        CB_Label lblPW = new CB_Label(this.name + " lblPW", chkTmc.getMaxX() + margin, 0, innerWidth - chkTmc.getWidth() - margin, chkTmc.getHeight());
        lblPW.setVAlignment(VAlignment.CENTER);
        lblPW.setText(Translation.get("UseTmc"));
        box2.addChild(lblPW);

        layout.addChild(box2);

        this.addChild(layout);

        Size msgBoxSize = MessageBox.calcMsgBoxSize("teste", true, true, false);
        msgBoxSize.height = (int) (msgBoxSize.height + layout.getHeight() - (TextFieldHeight / 2));
        this.setSize(msgBoxSize.asFloat());

        mMsgBoxClickListener = new OnMsgBoxClickListener() {

            @Override
            public boolean onClick(int which, Object data) {
                if (which == BUTTON_POSITIVE) {

                    if (mReturnListener != null)
                        mReturnListener.returnFromRoute_Dialog(false, state == 0, state == 1, state == 2, chkTmc.isChecked());
                } else {
                    if (mReturnListener != null)
                        mReturnListener.returnFromRoute_Dialog(true, false, false, false, false);
                }

                return true;
            }
        };

        btMotoWay.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchVisibility(0);
                return true;
            }
        });

        btCycleWay.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchVisibility(1);
                return true;
            }
        });

        btFootWay.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchVisibility(2);
                return true;
            }
        });

        switchVisibility(0);

    }

    private void switchVisibility(int state) {
        this.state = state;

        if (state == 0) {
            btMotoWay.setState(1);
            btCycleWay.setState(0);
            btFootWay.setState(0);
        }
        if (state == 1) {
            btMotoWay.setState(0);
            btCycleWay.setState(1);
            btFootWay.setState(0);
        }
        if (state == 2) {
            btMotoWay.setState(0);
            btCycleWay.setState(0);
            btFootWay.setState(1);
        }

    }

    public interface IReturnListener {
        public void returnFromRoute_Dialog(boolean canceld, boolean Motoway, boolean CycleWay, boolean FootWay, boolean UseTmc);
    }

}
