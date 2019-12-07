package de.droidcachebox.gdx.controls.dialogs;

import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.CB_Label.VAlignment;
import de.droidcachebox.gdx.controls.messagebox.ButtonDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.Size;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;

public class RouteDialog extends ButtonDialog {

    int state = -1;
    private ImageMultiToggleButton btMotoWay, btCycleWay, btFootWay;
    private CB_CheckBox chkTmc;
    private IReturnListener mReturnListener;

    public RouteDialog(IReturnListener listener) {
        super(Menu.getMenuRec(), "RouteDialog", "", Translation.get("RouteToWaypoit"), MessageBoxButtons.OKCancel, null, null);
        mReturnListener = listener;

        SizeF msgBoxContentSize = getContentSize();
        // initial VariableField
        float textFieldHeight = Fonts.getNormal().getLineHeight() * 2.4f;

        float innerWidth = msgBoxContentSize.width + leftBorder + rightBorder;
        innerWidth = Menu.getMenuRec().getWidth();

        Linearlayout layout = new Linearlayout(innerWidth, "Layout");
        layout.setX(0);
        // layout.setBackground(new ColorDrawable(Color.GREEN));

        CB_RectF MTBRec = new CB_RectF(0, 0, innerWidth / 3, UiSizes.getInstance().getButtonHeight() * 2);

        btMotoWay = new ImageMultiToggleButton(MTBRec, "btMotoWay");
        btCycleWay = new ImageMultiToggleButton(MTBRec, "btCycleWay");
        btFootWay = new ImageMultiToggleButton(MTBRec, "btFootWay");

        btMotoWay.setImage(new SpriteDrawable(Sprites.getSprite("car")));
        btCycleWay.setImage(new SpriteDrawable(Sprites.getSprite("bicycle")));
        btFootWay.setImage(new SpriteDrawable(Sprites.getSprite("pedestrian")));

        /*
        btMotoWay.setX(0);
        btCycleWay.setX(btMotoWay.getMaxX());
        btFootWay.setX(btCycleWay.getMaxX());
         */

        Box box = new Box(new CB_RectF(0, 0, innerWidth, UiSizes.getInstance().getButtonHeight() * 2), "");
        layout.addLast(box);

        box.addNext(btMotoWay);
        box.addNext(btCycleWay);
        box.addLast(btFootWay);

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
        msgBoxSize.height = (int) (msgBoxSize.height + layout.getHeight() - (textFieldHeight / 2));
        this.setSize(msgBoxSize.asFloat());

        mMsgBoxClickListener = (which, data) -> {
            if (which == MessageBox.BTN_LEFT_POSITIVE) {
                if (mReturnListener != null)
                    mReturnListener.returnFromRoute_Dialog(false, state == 0, state == 1, state == 2, chkTmc.isChecked());
            } else {
                if (mReturnListener != null)
                    mReturnListener.returnFromRoute_Dialog(true, false, false, false, false);
            }

            return true;
        };

        btMotoWay.setClickHandler((view, x, y, pointer, button) -> {
            switchVisibility(0);
            return true;
        });

        btCycleWay.setClickHandler((view, x, y, pointer, button) -> {
            switchVisibility(1);
            return true;
        });

        btFootWay.setClickHandler((view, x, y, pointer, button) -> {
            switchVisibility(2);
            return true;
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
        void returnFromRoute_Dialog(boolean canceld, boolean Motoway, boolean CycleWay, boolean FootWay, boolean UseTmc);
    }

}
