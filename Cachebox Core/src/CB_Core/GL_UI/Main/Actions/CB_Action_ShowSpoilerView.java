package CB_Core.GL_UI.Main.Actions;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.SpoilerView;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowSpoilerView extends CB_Action_ShowView
{
	private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);

	public CB_Action_ShowSpoilerView()
	{
		super("spoiler", MenuID.AID_SHOW_SPOILER);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.spoilerView == null) && (tabMainView != null) && (tab != null)) TabMainView.spoilerView = new SpoilerView(
				tab.getContentRec(), "SpoilerView");

		if ((TabMainView.spoilerView != null) && (tab != null)) tab.ShowView(TabMainView.spoilerView);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	int spoilerState = -1;
	Sprite SpoilerIcon;

	@Override
	public Sprite getIcon()
	{
		boolean hasSpoiler = false;
		if (GlobalCore.getSelectedCache() != null) hasSpoiler = GlobalCore.getSelectedCache().SpoilerExists();

		if (hasSpoiler && spoilerState != 1)
		{
			SpoilerIcon = SpriteCache.Icons.get(18);
			spoilerState = 1;
		}
		else if (!hasSpoiler && spoilerState != 0)
		{
			SpoilerIcon = new Sprite(SpriteCache.Icons.get(18));
			SpoilerIcon.setColor(DISABLE_COLOR);
			spoilerState = 0;
		}

		return SpoilerIcon;
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.spoilerView;
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{
		Menu icm = new Menu("menu_compassView");
		icm.addItemClickListner(onItemClickListner);
		MenuItem mi;

		mi = icm.addItem(MenuID.MI_RELOAD_SPOILER, "reloadSpoiler");

		return icm;
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{

			switch (((MenuItem) v).getMenuItemId())
			{
			case MenuID.MI_RELOAD_SPOILER:
				// TODO implement Reload Spoiler
				return true;
			}
			return false;
		}
	};
}
