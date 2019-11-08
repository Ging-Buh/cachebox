package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.views.WaypointView;

public class Abstract_ShowWaypointAction extends AbstractShowAction {

    private static Abstract_ShowWaypointAction that;

    private Abstract_ShowWaypointAction() {
        super("Waypoints", MenuID.AID_SHOW_WAYPOINTS);
    }

    public static Abstract_ShowWaypointAction getInstance() {
        if (that == null) that = new Abstract_ShowWaypointAction();
        return that;
    }

    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(WaypointView.getInstance());
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
