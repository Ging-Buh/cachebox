package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.ViewID;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowActivity extends CB_Action_ShowView {
    private ViewID viewConst;
    private Sprite mIcon;

    public CB_Action_ShowActivity(String Name, int ID, ViewID ViewConst, Sprite icon) {
        super(Name, ID);
        viewConst = ViewConst;
        mIcon = icon;
        tabMainView = TabMainView.that;
        tab = TabMainView.leftTab;
    }

    @Override
    public void Execute() {
        PlatformConnector.showView(viewConst, 0, 0, 0, 0);
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return mIcon;
    }

    @Override
    public CB_View_Base getView() {
        return null;
    }
}
