package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.CompassView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowCompassView extends CB_Action_ShowView
{
	public final int MI_TEST1 = 1;
	public final int MI_TEST2 = 2;

	public CB_Action_ShowCompassView()
	{
		super("Compass", AID_SHOW_COMPASS);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.compassView == null) && (tabMainView != null) && (tab != null)) TabMainView.compassView = new CompassView(
				tab.getContentRec(), "CompassView");

		if ((TabMainView.compassView != null) && (tab != null)) tab.ShowView(TabMainView.compassView);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.compassView;
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(5);
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public boolean ShowContextMenu()
	{
		Menu cm = new Menu("CompassContextMenu");

		cm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MI_TEST1:
					return true;
				case MI_TEST2:
					return true;
				}
				return false;
			}
		});

		MenuItem mi;

		mi = cm.addItem(MI_TEST1, "Test 1");
		mi = cm.addItem(MI_TEST2, "Test 2");

		cm.show();
		return true;
	}
}
