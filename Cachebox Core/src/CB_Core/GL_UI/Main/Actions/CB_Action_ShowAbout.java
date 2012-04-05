package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.AboutView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowAbout extends CB_Action_ShowView
{

	public CB_Action_ShowAbout()
	{
		super("Map", AID_SHOW_MAP);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.aboutView == null) && (tabMainView != null) && (tab != null)) TabMainView.aboutView = new AboutView(
				tab.getContentRec(), "AboutView");

		if ((TabMainView.aboutView != null) && (tab != null)) tab.ShowView(TabMainView.aboutView);
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
	public CB_View_Base getView()
	{
		return TabMainView.aboutView;
	}
}
