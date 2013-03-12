package CB_Core.GL_UI.Main.Actions;

import CB_Core.Config;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.TrackableListView;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.TbList;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTrackableListView extends CB_Action_ShowView
{
	private CancelWaitDialog wd;

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
		return SpriteCache.Icons.get(38);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.trackableListView;
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{
		final Menu cm = new Menu("TBListContextMenu");

		cm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{

				case MenuID.MI_REFRECH_TB_LIST:

					wd = CancelWaitDialog.ShowWait(Translation.Get("RefreshInventory"), new IcancelListner()
					{

						@Override
						public void isCanceld()
						{
							// TODO Auto-generated method stub

						}
					}, new Runnable()
					{

						@Override
						public void run()
						{
							result = -1;
							TbList searchList = new TbList();
							result = CB_Core.Api.GroundspeakAPI.getMyTbList(Config.GetAccessToken(), searchList);

							if (result == 0)
							{
								searchList.writeToDB();
								TrackableListView.that.reloadTB_List();
							}

							wd.close();
						}
					});
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
