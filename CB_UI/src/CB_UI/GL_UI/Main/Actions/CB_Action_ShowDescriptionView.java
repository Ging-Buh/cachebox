package CB_UI.GL_UI.Main.Actions;

import java.util.ArrayList;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.GL_View_Base;
import CB_UI.GL_UI.SpriteCacheBase;
import CB_UI.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListner;
import CB_UI.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI.GL_UI.GL_View_Base.OnClickListener;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Menu.Menu;
import CB_UI.GL_UI.Menu.MenuID;
import CB_UI.GL_UI.Menu.MenuItem;
import CB_UI.GL_UI.SpriteCacheBase.IconName;
import CB_UI.GL_UI.Views.DescriptionView;

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
		return SpriteCacheBase.Icons.get(IconName.doc_2.ordinal());
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

					wd = CancelWaitDialog.ShowWait(Translation.Get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), new IcancelListner()
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
							CB_UI.Api.SearchForGeocaches.SearchGC searchC = new CB_UI.Api.SearchForGeocaches.SearchGC();
							searchC.gcCode = GlobalCore.getSelectedCache().GcCode;

							searchC.number = 1;

							ArrayList<Cache> apiCaches = new ArrayList<Cache>();
							ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
							ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

							CB_UI.Api.SearchForGeocaches.SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages,
									GlobalCore.getSelectedCache().GPXFilename_ID);

							try
							{
								GroundspeakAPI.WriteCachesLogsImages_toDB(apiCaches, apiLogs, apiImages);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}

							// Reload result from DB
							synchronized (Database.Data.Query)
							{
								String sqlWhere = GlobalCore.LastFilter.getSqlWhere(Config.settings.GcLogin.getValue());
								CacheListDAO cacheListDAO = new CacheListDAO();
								cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere);
							}

							CachListChangedEventList.Call();

							CachListChangedEventList.Call();
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

		mi = cm.addItem(MenuID.MI_FAVORIT, "Favorite", SpriteCacheBase.Icons.get(IconName.favorit_42.ordinal()));
		mi.setCheckable(true);
		if (isSelected)
		{
			mi.setChecked(GlobalCore.getSelectedCache().Favorit());
		}
		else
		{
			mi.setEnabled(false);
		}

		mi = cm.addItem(MenuID.MI_RELOAD_CACHE, "ReloadCacheAPI", SpriteCacheBase.Icons.get(IconName.GCLive_35.ordinal()));
		mi.setEnabled(isSelected);
		return cm;
	}

}
