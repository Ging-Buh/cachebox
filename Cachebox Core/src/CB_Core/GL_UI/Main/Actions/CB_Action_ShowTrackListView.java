package CB_Core.GL_UI.Main.Actions;

import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.MenuItemConst;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.TrackListView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTrackListView extends CB_Action_ShowView
{

	public CB_Action_ShowTrackListView()
	{
		super("Tracks", AID_SHOW_TRACKLIST);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.trackListView == null) && (tabMainView != null) && (tab != null)) TabMainView.trackListView = new TrackListView(
				tab.getContentRec(), "TrackListView");

		if ((TabMainView.trackListView != null) && (tab != null)) tab.ShowView(TabMainView.trackListView);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(8);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.trackListView;
	}

	private static final int GENERATE = 1;
	private static final int RENAME = 2;
	private static final int LOAD = 3;
	private static final int SAVE = 4;
	private static final int DELETE = 5;
	private static final int P2P = 6;
	private static final int PROJECT = 7;
	private static final int CIRCLE = 8;

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public boolean ShowContextMenu()
	{
		Menu cm = new Menu("TrackListContextMenu");

		cm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case GENERATE:
					showMenuCreate();
					return true;
				case RENAME:
					// ;
					return true;
				case LOAD:

					platformConector.menuItemClicked(MenuItemConst.TRACK_LIST_LOAD);

					// platformConector.getFile(Config.settings.TrackFolder.getValue(), "*.gpx", new IgetFileReturnListner()
					// {
					// @Override
					// public void getFieleReturn(String Path)
					// {
					// // TODO Load Track from Path
					// if (Path != null)
					// {
					// Logger.LogCat("Load Track :" + Path);
					// }
					// }
					// });

					return true;
				case SAVE:
					return true;
				case DELETE:
					platformConector.menuItemClicked(MenuItemConst.TRACK_LIST_DELETE);
					return true;

				}
				return false;
			}
		});

		cm.addItem(GENERATE, "generate");
		cm.addItem(RENAME, "rename");
		cm.addItem(LOAD, "load");
		cm.addItem(SAVE, "save");
		cm.addItem(DELETE, "delete");

		cm.show();

		return true;
	}

	private void showMenuCreate()
	{
		Menu cm2 = new Menu("TrackListCreateContextMenu");
		cm2.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case P2P:
					platformConector.menuItemClicked(MenuItemConst.TRACK_LIST_P2P);
					return true;
				case PROJECT:
					platformConector.menuItemClicked(MenuItemConst.TRACK_LIST_PROJECT);
					return true;
				case CIRCLE:
					platformConector.menuItemClicked(MenuItemConst.TRACK_LIST_CIRCLE);
					return true;
				}
				return false;
			}
		});
		cm2.addItem(P2P, "Point2Point");
		cm2.addItem(PROJECT, "Projection");
		cm2.addItem(CIRCLE, "Circle");

		cm2.show();
	}
}
