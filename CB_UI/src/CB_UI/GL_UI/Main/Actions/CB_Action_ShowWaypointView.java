package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.WaypointView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowWaypointView extends CB_Action_ShowView {

    private static CB_Action_ShowWaypointView that;

    private CB_Action_ShowWaypointView() {
        super("Waypoints", MenuID.AID_SHOW_WAYPOINTS);
    }

    public static CB_Action_ShowWaypointView getInstance() {
        if (that == null) that = new CB_Action_ShowWaypointView();
        return that;
    }

    @Override
    public void Execute() {
        TabMainView.leftTab.ShowView(WaypointView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.waypointListIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return WaypointView.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return WaypointView.getInstance().getContextMenu();
    }
}
