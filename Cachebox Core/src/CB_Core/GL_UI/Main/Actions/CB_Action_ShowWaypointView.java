package CB_Core.GL_UI.Main.Actions;

import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.MenuItemConst;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.WaypointView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowWaypointView extends CB_Action_ShowView
{

	private final static int MI_ADD_WAYPOINT = 0;
	private final static int MI_PROJEKTION = 1;
	private final static int MI_FROM_GPS = 2;
	private final static int MI_EDIT = 3;
	private final static int MI_DELETE = 4;

	public CB_Action_ShowWaypointView()
	{
		super("Waypoints", AID_SHOW_WAYPOINTS);
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
		return SpriteCache.Icons.get(4);
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
	public boolean ShowContextMenu()
	{
		platformConector.menuItemClicked(MenuItemConst.SHOW_WP_CONTEXT_MENU);
		return true;
	}
}
