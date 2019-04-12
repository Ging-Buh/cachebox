package CB_UI.GL_UI.Main.Actions;

import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.ViewID;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_PlatformActivity extends CB_Action {
    private ViewID viewConst;
    private Sprite mIcon;

    public CB_Action_PlatformActivity(String Name, int ID, ViewID ViewConst, Sprite icon) {
        super(Name, ID);
        viewConst = ViewConst;
        mIcon = icon;
    }

    @Override
    public void Execute() {
        PlatformConnector.showView(viewConst, 0, 0, 0, 0);
    }

    @Override
    public Sprite getIcon() {
        return mIcon;
    }
}
