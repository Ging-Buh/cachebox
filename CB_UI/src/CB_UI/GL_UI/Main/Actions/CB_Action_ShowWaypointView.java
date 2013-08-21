package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.SpriteCacheBase;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Menu.Menu;
import CB_UI.GL_UI.Menu.MenuID;
import CB_UI.GL_UI.SpriteCacheBase.IconName;
import CB_UI.GL_UI.Views.WaypointView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowWaypointView extends CB_Action_ShowView
{

	public CB_Action_ShowWaypointView()
	{
		super("Waypoints", MenuID.AID_SHOW_WAYPOINTS);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.waypointView == null) && (tabMainView != null) && (tab != null)) TabMainView.waypointView = new WaypointView(
				tab.getContentRec(), "WaypointView");

		if ((TabMainView.waypointView != null) && (tab != null)) tab.ShowView(TabMainView.waypointView);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.Icons.get(IconName.waypointList_56.ordinal());
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.waypointView;
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{

		if (WaypointView.that != null) return WaypointView.that.getContextMenu();
		return null;
	}
}
