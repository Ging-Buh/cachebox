package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Views.WaypointView;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import com.badlogic.gdx.graphics.g2d.Sprite;

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
        if (CB_Action_ShowMap.getInstance().normalMapView.isVisible()) {
            CB_Action_ShowMap.getInstance().normalMapView.createWaypointAtCenter();
            return;
        }
        WaypointView.getInstance().addWP();
    }
}
