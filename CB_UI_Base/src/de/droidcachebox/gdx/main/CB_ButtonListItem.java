package de.droidcachebox.gdx.main;

import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.math.CB_RectF;

public class CB_ButtonListItem extends ListViewItemBase {
    GestureButton mCB_Button;

    public CB_ButtonListItem(int Index, GestureButton Button, String Name) {
        super(new CB_RectF(Button), Index, Name);
        mCB_Button = Button;
        mCB_Button.setPos(0, 0);
        mCB_Button.setY(Button.getY());
        mCB_Button.setSize(Button);
        this.addChild(mCB_Button);
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        return mCB_Button.onTouchUp(x, y, pointer, button);
    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {
        return mCB_Button.click(x, y, pointer, button);
    }

}
