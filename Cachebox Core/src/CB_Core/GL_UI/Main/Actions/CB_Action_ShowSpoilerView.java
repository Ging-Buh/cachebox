package CB_Core.GL_UI.Main.Actions;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Views.SpoilerView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowSpoilerView extends CB_Action_ShowView
{

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
		boolean hasSpoiler = false;
		if (GlobalCore.getSelectedCache() != null) hasSpoiler = GlobalCore.getSelectedCache().SpoilerExists();
		return hasSpoiler;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(18);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.spoilerView;
	}
}
