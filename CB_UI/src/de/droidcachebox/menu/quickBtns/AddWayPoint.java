package de.droidcachebox.menu.quickBtns;

import static de.droidcachebox.menu.Action.ShowMap;
import static de.droidcachebox.menu.Action.ShowWayPoints;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.menu.menuBtn3.ShowMap;

public class AddWayPoint extends AbstractAction {

    public AddWayPoint() {
        super("addWP");
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
        if (((ShowMap) ShowMap.action).normalMapView.isVisible()) {
            ((ShowMap) ShowMap.action).normalMapView.createWaypointAtCenter();
            return;
        }
        ((de.droidcachebox.menu.menuBtn2.ShowWayPoints) ShowWayPoints.action).addWayPoint();
    }
}
