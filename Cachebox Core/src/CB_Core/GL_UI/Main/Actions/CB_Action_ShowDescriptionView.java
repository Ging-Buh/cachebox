package CB_Core.GL_UI.Main.Actions;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CacheDAO;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_Core.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.DescriptionView;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowDescriptionView extends CB_Action_ShowView
{

	public CB_Action_ShowDescriptionView()
	{
		super("Description", MenuID.AID_SHOW_DESCRIPTION);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.descriptionView == null) && (tabMainView != null) && (tab != null)) TabMainView.descriptionView = new DescriptionView(
				tab.getContentRec(), "DescriptionView");

		if ((TabMainView.descriptionView != null) && (tab != null)) tab.ShowView(TabMainView.descriptionView);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(2);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.descriptionView;
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	CancelWaitDialog wd = null;

	@Override
	public Menu getContextMenu()
	{
		Menu cm = new Menu("CacheListContextMenu");

		cm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MenuID.MI_FAVORIT:
					if (GlobalCore.getSelectedCache() == null)
					{
						GL_MsgBox.Show(Translation.Get("NoCacheSelect"), Translation.Get("Error"), MessageBoxIcon.Error);
						return true;
					}

					GlobalCore.getSelectedCache().setFavorit(!GlobalCore.getSelectedCache().Favorit());
					CacheDAO dao = new CacheDAO();
					dao.UpdateDatabase(GlobalCore.getSelectedCache());

					return true;
				case MenuID.MI_RELOAD_CACHE:

					if (GlobalCore.getSelectedCache() == null)
					{
						GL_MsgBox.Show(Translation.Get("NoCacheSelect"), Translation.Get("Error"), MessageBoxIcon.Error);
						return true;
					}

					wd = CancelWaitDialog.ShowWait(Translation.Get("ReloadCacheAPI"), new IcancelListner()
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
							String accessToken = Config.GetAccessToken();

							CB_Core.Api.SearchForGeocaches.SearchGC searchC = new CB_Core.Api.SearchForGeocaches.SearchGC();
							searchC.gcCode = GlobalCore.getSelectedCache().GcCode;

							searchC.number = 1;

							ArrayList<Cache> apiCaches = new ArrayList<Cache>();
							ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
							ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

							CB_Core.Api.SearchForGeocaches.SearchForGeocachesJSON(accessToken, searchC, apiCaches, apiLogs, apiImages,
									GlobalCore.getSelectedCache().GPXFilename_ID);

							try
							{
								GroundspeakAPI.WriteCachesLogsImages_toDB(apiCaches, apiLogs, apiImages);
							}
							catch (InterruptedException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							wd.close();
						}
					});
					return true;
				}
				return false;
			}
		});

		MenuItem mi;

		boolean isSelected = (GlobalCore.getSelectedCache() != null);

		mi = cm.addItem(MenuID.MI_FAVORIT, "Favorite", SpriteCache.Icons.get(42));
		mi.setCheckable(true);
		if (isSelected)
		{
			mi.setChecked(GlobalCore.getSelectedCache().Favorit());
		}
		else
		{
			mi.setEnabled(false);
		}

		mi = cm.addItem(MenuID.MI_RELOAD_CACHE, "ReloadCacheAPI", SpriteCache.Icons.get(35));
		mi.setEnabled(isSelected);
		return cm;
	}

}
