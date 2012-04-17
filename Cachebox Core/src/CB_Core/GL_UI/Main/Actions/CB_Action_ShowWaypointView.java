package CB_Core.GL_UI.Main.Actions;

import CB_Core.DB.Database;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
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
		Menu cm = new Menu("CacheListContextMenu");

		cm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MI_ADD_WAYPOINT:
					Database.Data.Query.Resort();
					return true;
				case MI_PROJEKTION:
					Database.Data.Query.Resort();
					return true;
				case MI_FROM_GPS:
					Database.Data.Query.Resort();
					return true;

				}
				return false;
			}
		});

		MenuItem mi;
		cm.addItem(MI_ADD_WAYPOINT, "addWaypoint", SpriteCache.Icons.get(39));
		cm.addItem(MI_PROJEKTION, "projection", SpriteCache.Icons.get(13));
		cm.addItem(MI_FROM_GPS, "fromGps", SpriteCache.Icons.get(12));

		cm.show();

		return true;
	}
}
