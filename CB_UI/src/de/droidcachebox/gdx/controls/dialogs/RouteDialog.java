package de.droidcachebox.gdx.controls.dialogs;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.CB_Label.VAlignment;
import de.droidcachebox.gdx.controls.messagebox.ButtonDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBox.OnMsgBoxClickListener;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;

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

        CB_RectF MTBRec = new CB_RectF(0, 0, innerWidth / 3, UiSizes.getInstance().getButtonHeight() * 2);

        btMotoWay = new ImageMultiToggleButton(MTBRec, "btMotoWay");
        btCycleWay = new ImageMultiToggleButton(MTBRec, "btCycleWay");
        btFootWay = new ImageMultiToggleButton(MTBRec, "btFootWay");

        btMotoWay.setImage(new SpriteDrawable(Sprites.getSprite("pictureBox2")));
        btCycleWay.setImage(new SpriteDrawable(Sprites.getSprite("pictureBox1")));
        btFootWay.setImage(new SpriteDrawable(Sprites.getSprite("pictureBox3")));

        btMotoWay.setX(0);
        btCycleWay.setX(btMotoWay.getMaxX());
        btFootWay.setX(btCycleWay.getMaxX());

        Box box = new Box(new CB_RectF(0, 0, innerWidth, UiSizes.getInstance().getButtonHeight() * 2), "");

        box.addChild(btMotoWay);
        box.addChild(btCycleWay);
        box.addChild(btFootWay);

        layout.addChild(box);

        btMotoWay.initialOn_Off_ToggleStates("", "");
        btCycleWay.initialOn_Off_ToggleStates("", "");
        btFootWay.initialOn_Off_ToggleStates("", "");

        Box box2 = new Box(new CB_RectF(0, 0, innerWidth, UiSizes.getInstance().getButtonHeight()), "");
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

        btMotoWay.addClickHandler(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchVisibility(0);
                return true;
            }
        });

        btCycleWay.addClickHandler(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switchVisibility(1);
                return true;
            }
        });

        btFootWay.addClickHandler(new OnClickListener() {

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
