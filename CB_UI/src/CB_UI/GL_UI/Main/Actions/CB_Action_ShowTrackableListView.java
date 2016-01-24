package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.TrackableListView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTrackableListView extends CB_Action_ShowView
{

	public CB_Action_ShowTrackableListView()
	{
		super("TBList", MenuID.AID_SHOW_TRACKABLELIST);
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
		return SpriteCacheBase.Icons.get(IconName.tbList_38.ordinal());
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.trackableListView;
	}

	@Override
	public boolean hasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{
		final Menu cm = new Menu("TBListContextMenu");

		cm.addOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{

				case MenuID.MI_REFRECH_TB_LIST:

					TrackableListView.that.RefreshTbList();
					return true;
				}
				return false;
			}
		});

		cm.addItem(MenuID.MI_REFRECH_TB_LIST, "RefreshInventory");

		return cm;
	}

	int result;

}
