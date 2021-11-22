package de.droidcachebox.menu.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.menu.menuBtn2.executes.Waypoints;
import de.droidcachebox.menu.menuBtn3.ShowMap;

public class AddWayPoint extends AbstractAction {

    private static AddWayPoint addWayPoint;

    private AddWayPoint() {
        super("addWP");
    }

    public static AddWayPoint getInstance() {
        if (addWayPoint == null) addWayPoint = new AddWayPoint();
        return addWayPoint;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite("add-wp");
    }

    @Override
    public void execute() {
        // wenn MapView sichtbar und im Modus Free, dann nehme Koordinaten vom Mittelpunkt der Karte
        // ansonsten mit den aktuellen Koordinaten!
        if (ShowMap.getInstance().normalMapView.isVisible()) {
            ShowMap.getInstance().normalMapView.createWaypointAtCenter();
            return;
        }
        Waypoints.getInstance().addWP();
    }
}
