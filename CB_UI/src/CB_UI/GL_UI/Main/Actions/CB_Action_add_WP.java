package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GL_UI.Views.WaypointView;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_add_WP extends CB_Action {

	public CB_Action_add_WP() {
		super("addWP", MenuID.AID_ADD_WP);
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return SpriteCacheBase.getThemedSprite("add-wp");
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
