package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.views.WaypointView;

public class Action_Add_WP extends AbstractAction {

    private static Action_Add_WP that;

    private Action_Add_WP() {
        super("addWP", MenuID.AID_ADD_WP);
    }

    public static Action_Add_WP getInstance() {
        if (that == null) that = new Action_Add_WP();
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
        if (Abstract_ShowMap.getInstance().normalMapView.isVisible()) {
            Abstract_ShowMap.getInstance().normalMapView.createWaypointAtCenter();
            return;
        }
        WaypointView.getInstance().addWP();
    }
}
