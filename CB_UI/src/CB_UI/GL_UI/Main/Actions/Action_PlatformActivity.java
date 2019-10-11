package CB_UI.GL_UI.Main.Actions;

import CB_UI_Base.Events.PlatformUIBase;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.ViewID;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Action_PlatformActivity extends AbstractAction {
    private ViewID viewID;
    private Sprite sprite;

    public Action_PlatformActivity(String Name, int ID, ViewID viewID, Sprite icon) {
        super(Name, ID);
        this.viewID = viewID;
        this.sprite = icon;
    }

    @Override
    public void Execute() {
        PlatformUIBase.showView(viewID, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public Sprite getIcon() {
        return sprite;
    }
}
