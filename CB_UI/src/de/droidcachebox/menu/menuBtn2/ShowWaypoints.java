package de.droidcachebox.menu.menuBtn2;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn2.executes.WaypointsView;

public class ShowWaypoints extends AbstractShowAction {

    private static ShowWaypoints that;

    private ShowWaypoints() {
        super("Waypoints");
    }

    public static ShowWaypoints getInstance() {
        if (that == null) that = new ShowWaypoints();
        return that;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(WaypointsView.getInstance());
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
        return WaypointsView.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return WaypointsView.getInstance().getContextMenu();
    }
}
