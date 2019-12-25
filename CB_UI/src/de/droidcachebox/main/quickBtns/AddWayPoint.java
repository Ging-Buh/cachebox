package de.droidcachebox.main.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.views.WaypointView;
import de.droidcachebox.main.AbstractAction;
import de.droidcachebox.main.menuBtn3.ShowMap;

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
        WaypointView.getInstance().addWP();
    }
}
