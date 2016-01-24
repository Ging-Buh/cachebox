package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.MapView;
import CB_UI.GL_UI.Views.WaypointView;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_ActionCommand;
import CB_UI_Base.GL_UI.Menu.MenuID;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_add_WP extends CB_ActionCommand
{

	public CB_Action_add_WP()
	{
		super("addWP", MenuID.AID_ADD_WP);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.getThemedSprite("add-wp");
	}

	@Override
	public void Execute()
	{
		// wenn MapView sichtbar und im Modus Free, dann nehme Koordinaten vom Mittelpunkt der Karte
		// ansonsten mit den aktuellen Koordinaten!
		if (MapView.that != null && MapView.that.isVisible())
		{
			MapView.that.createWaypointAtCenter();
			return;
		}

		if ((TabMainView.waypointView == null)) TabMainView.waypointView = new WaypointView(TabMainView.leftTab.getContentRec(),
				"WaypointView");
		WaypointView.that.addWP();
	}
}
