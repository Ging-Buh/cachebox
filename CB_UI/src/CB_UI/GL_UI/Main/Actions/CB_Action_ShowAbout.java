package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.SpriteCacheBase;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Menu.Menu;
import CB_UI.GL_UI.Menu.MenuID;
import CB_UI.GL_UI.SpriteCacheBase.IconName;
import CB_UI.GL_UI.Views.AboutView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowAbout extends CB_Action_ShowView
{

	public CB_Action_ShowAbout()
	{
		super("about", MenuID.AID_SHOW_ABOUT);
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
		return SpriteCacheBase.Icons.get(IconName.cb_49.ordinal());
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.aboutView;
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{
		return null;
	}
}
