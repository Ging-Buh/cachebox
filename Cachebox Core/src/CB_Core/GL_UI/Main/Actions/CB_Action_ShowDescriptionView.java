package CB_Core.GL_UI.Main.Actions;

import CB_Core.GlobalCore;
import CB_Core.DAO.CacheDAO;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.DescriptionView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowDescriptionView extends CB_Action_ShowView
{

	private static final int MI_FAVORIT = 0;
	private static final int MI_RELOAD_CACHE = 1;

	public CB_Action_ShowDescriptionView()
	{
		super("Description", AID_SHOW_DESCRIPTION);
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
		return SpriteCache.Icons.get(0);
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
				case MI_FAVORIT:
					GlobalCore.SelectedCache().setFavorit(!GlobalCore.SelectedCache().Favorit());
					CacheDAO dao = new CacheDAO();
					dao.UpdateDatabase(GlobalCore.SelectedCache());

					return true;
				case MI_RELOAD_CACHE:
					new CB_Action_ShowActivity("reload_CacheInfo", MI_RELOAD_CACHE, ViewConst.RELOAD_CACHE, SpriteCache.Icons.get(35))
							.Execute();
					return true;
				}
				return false;
			}
		});

		MenuItem mi;

		mi = cm.addItem(MI_FAVORIT, "favorit", SpriteCache.Icons.get(42));
		mi.setCheckable(true);
		mi.setChecked(GlobalCore.SelectedCache().Favorit());
		cm.addItem(MI_RELOAD_CACHE, "chkState", SpriteCache.Icons.get(35));

		cm.show();

		return true;
	}
}
