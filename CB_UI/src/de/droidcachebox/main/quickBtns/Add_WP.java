package de.droidcachebox.main.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.gdx.main.MenuID;
import de.droidcachebox.gdx.views.WaypointView;
import de.droidcachebox.main.menuBtn3.ShowMap;

public class Add_WP extends AbstractAction {

    private static Add_WP that;

    private Add_WP() {
        super("addWP", MenuID.AID_ADD_WP);
    }

    public static Add_WP getInstance() {
        if (that == null) that = new Add_WP();
        return that;
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
    public void Execute() {
        // wenn MapView sichtbar und im Modus Free, dann nehme Koordinaten vom Mittelpunkt der Karte
        // ansonsten mit den aktuellen Koordinaten!
        if (ShowMap.getInstance().normalMapView.isVisible()) {
            ShowMap.getInstance().normalMapView.createWaypointAtCenter();
            return;
        }
        WaypointView.getInstance().addWP();
    }
}
