package de.droidcachebox.menu.menuBtn2;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn2.executes.WayPointsView;

public class ShowWayPoints extends AbstractShowAction {

    private WayPointsView wayPointsView;

    public ShowWayPoints() {
        super("Waypoints");
    }

    @Override
    public void execute() {
        if (wayPointsView == null)
            wayPointsView = new WayPointsView();
        ViewManager.leftTab.showView(wayPointsView);
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
        return wayPointsView;
    }

    @Override
    public void viewIsHiding() {
        wayPointsView = null;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        if (wayPointsView == null) wayPointsView = new WayPointsView();
        return wayPointsView.getContextMenu();
    }

    public void addWayPoint() {
        if (wayPointsView == null) wayPointsView = new WayPointsView();
        wayPointsView.addWP();
    }
}
