package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Main.Actions.MenuActions.CB_Action_TrackList_Create;
import CB_Core.GL_UI.Main.Actions.MenuActions.CB_Action_TrackList_Delete;
import CB_Core.GL_UI.Main.Actions.MenuActions.CB_Action_TrackList_Load;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.TrackableListView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTrackableListView extends CB_Action_ShowView
{
	public CB_Action_ShowTrackableListView()
	{
		super("TBList", AID_SHOW_TRACKABLELIST);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.trackableListView == null) && (tabMainView != null) && (tab != null)) TabMainView.trackableListView = new TrackableListView(
				tab.getContentRec(), "TrackableListView");

		if ((TabMainView.trackableListView != null) && (tab != null)) tab.ShowView(TabMainView.trackableListView);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(38);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.trackableListView;
	}

	private static final int GENERATE = 1;
	private static final int LOAD = 2;
	private static final int DELETE = 3;

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
				case GENERATE:
					new CB_Action_TrackList_Create().Execute();
					return true;
				case LOAD:
					new CB_Action_TrackList_Load().Execute();
					return true;
				case DELETE:
					new CB_Action_TrackList_Delete().Execute();
					return true;

				}
				return false;
			}
		});

		MenuItem mi;

		mi = cm.addItem(GENERATE, "generate");
		mi = cm.addItem(LOAD, "load");
		mi = cm.addItem(DELETE, "delete");

		cm.show();

		return true;
	}

}
