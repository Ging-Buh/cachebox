package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Views.CompassView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowCompassView extends CB_Action_ShowView
{
	public final int MI_TEST1 = 1;
	public final int MI_TEST2 = 2;

	public CB_Action_ShowCompassView()
	{
		super("Compass", MenuID.AID_SHOW_COMPASS);
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
		return SpriteCache.Icons.get(6);
	}

}
