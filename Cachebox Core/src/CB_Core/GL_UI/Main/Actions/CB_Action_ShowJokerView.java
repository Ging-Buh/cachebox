package CB_Core.GL_UI.Main.Actions;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Views.JokerView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowJokerView extends CB_Action_ShowView
{

	public CB_Action_ShowJokerView()
	{
		super("joker", MenuID.AID_SHOW_JOKERS);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.jokerView == null) && (tabMainView != null) && (tab != null)) TabMainView.jokerView = new JokerView(
				tab.getContentRec(), "JokerView");

		if ((TabMainView.jokerView != null) && (tab != null)) tab.ShowView(TabMainView.jokerView);
	}

	@Override
	public boolean getEnabled()
	{
		if (GlobalCore.getSelectedCache().GcCode.startsWith("GC")) // GC-Joker nur zulässig wenn es ein Cache von geocaching.com ist
		{
			return GlobalCore.JokerisOnline();
		}
		else
		{
			return (false);
		}
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(25);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.jokerView;
	}
}
