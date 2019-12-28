package de.droidcachebox.menu.menuBtn2;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.views.WaypointView;
import de.droidcachebox.menu.ViewManager;

public class ShowWaypoint extends AbstractShowAction {

    private static ShowWaypoint that;

    private ShowWaypoint() {
        super("Waypoints");
    }

    public static ShowWaypoint getInstance() {
        if (that == null) that = new ShowWaypoint();
        return that;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(WaypointView.getInstance());
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
