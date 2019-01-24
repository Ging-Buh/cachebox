package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GL_UI.Views.WaypointView;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_add_WP extends CB_Action {

    private static CB_Action_add_WP that;

    private CB_Action_add_WP() {
        super("addWP", MenuID.AID_ADD_WP);
    }

    public static CB_Action_add_WP getInstance() {
        if (that == null) that = new CB_Action_add_WP();
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
        if (MapView.that != null && MapView.that.isVisible()) {
            MapView.that.createWaypointAtCenter();
            return;
        }

        if ((TabMainView.waypointView == null))
            TabMainView.waypointView = new WaypointView(TabMainView.leftTab.getContentRec(), "WaypointView");
        WaypointView.that.addWP();
    }
}
