package CB_UI_Base.GL_UI.Main;

import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import com.badlogic.gdx.math.Vector2;

public class CB_ButtonListItem extends ListViewItemBase {
    CB_Button mCB_Button;

    public CB_ButtonListItem(int Index, CB_Button Button, String Name) {
        super(new CB_RectF(Button), Index, Name);
        mCB_Button = Button;
        mCB_Button.setPos(new Vector2(0, 0));
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
