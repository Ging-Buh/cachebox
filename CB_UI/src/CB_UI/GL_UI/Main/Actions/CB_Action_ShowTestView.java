package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.TestViews.TestView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTestView extends CB_Action_ShowView {

    public CB_Action_ShowTestView() {
        super("TestView", MenuID.AID_TEST_VIEW);
    }

    @Override
    public void Execute() {
        TabMainView.leftTab.ShowView(TestView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.manualWayPoint.name());
    }

    @Override
    public CB_View_Base getView() {
        return TestView.getInstance();
    }
}
