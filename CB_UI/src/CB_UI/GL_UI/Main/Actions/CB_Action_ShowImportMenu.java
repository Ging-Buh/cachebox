package CB_UI.GL_UI.Main.Actions;

import CB_UI.Config;
import CB_UI.GL_UI.Activitys.Import;
import CB_UI.GL_UI.Activitys.SearchOverPosition;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.runOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_Utils.StringH;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowImportMenu extends CB_Action_ShowView
{

	public CB_Action_ShowImportMenu()
	{
		super("ImportMenu", MenuID.AID_SHOW_IMPORT_MENU);
	}

	@Override
	public void Execute()
	{
		getContextMenu().Show();
	}

	@Override
	public CB_View_Base getView()
	{
		// don't return a view.
		// show menu direct.
		GL.that.RunOnGL(new runOnGL()
		{
			@Override
			public void run()
			{
				Execute();
			}
		});

		return null;
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.Icons.get(IconName.cacheList_7.ordinal());
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{
		Menu icm = new Menu("CacheListShowImportMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GS)
				{
					showImportMenu_GS();
				}
				else if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_CBS)
				{
					// Menü noch nicht zeigen da darin nur 1 Befehl ist
					// showImportMenu_CBS();
					import_CBS();
				}
				else if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GPX)
				{
					// Menü nicht zeigen da darin nur 1 Befehl ist
					// showImportMenu_GPX();
					import_GPX();
				}
				else if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GCV)
				{
					// Menü nicht zeigen da darin nur 1 Befehl ist
					// showImportMenu_GCV();
					import_GCV();
				}
				else if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT)
				{
					Import imp = new Import();
					imp.show();
				}
				return true;
			}
		});
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_IMPORT_GS, "Groundspeak API");
		if (!StringH.isEmpty(Config.CBS_IP.getValue())) mi = icm.addItem(MenuID.MI_IMPORT_CBS, "CB-Server");
		mi = icm.addItem(MenuID.MI_IMPORT_GPX, "GPX");
		mi = icm.addItem(MenuID.MI_IMPORT_GCV, "GC Vote");
		mi = icm.addItem(MenuID.MI_IMPORT, "Import");
		return icm;
	}

	protected void showImportMenu_GCV()
	{
		Menu icm = new Menu("CacheListShowImportMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GCV)
				{
					import_GCV();
				}
				return true;
			}
		});
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_IMPORT_GCV, "GC-Vote Import");

		icm.Show();
	}

	private void import_GCV()
	{
		Import imp = new Import(MenuID.MI_IMPORT_GCV);
		imp.show();
	}

	protected void showImportMenu_GPX()
	{
		Menu icm = new Menu("CacheListShowImportMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GPX)
				{
					import_GPX();
				}
				return true;
			}
		});
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_IMPORT_GPX, "GPX Import");

		icm.Show();
	}

	private void import_GPX()
	{
		Import imp = new Import(MenuID.MI_IMPORT_GPX);
		imp.show();
	}

	protected void showImportMenu_CBS()
	{
		Menu icm = new Menu("CacheListShowImportMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_CBS)
				{
					import_CBS();
				}
				return true;
			}
		});
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_IMPORT_CBS, "CB-Server");

		icm.Show();
	}

	private void import_CBS()
	{
		Import imp = new Import(MenuID.MI_IMPORT_CBS);
		imp.show();
	}

	private void showImportMenu_GS()
	{
		Menu icm = new Menu("CacheListShowImportMenu");

		icm.addItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GS_PQ)
				{
					Import imp = new Import(MenuID.MI_IMPORT_GS_PQ);
					imp.show();
				}
				else if (((MenuItem) v).getMenuItemId() == MenuID.MI_IMPORT_GS_API)
				{
					new SearchOverPosition().show();
				}
				return true;
			}
		});
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_IMPORT_GS_PQ, "Pocket Query");
		mi = icm.addItem(MenuID.MI_IMPORT_GS_API, "Umkreissuche");

		icm.Show();
	}

}
