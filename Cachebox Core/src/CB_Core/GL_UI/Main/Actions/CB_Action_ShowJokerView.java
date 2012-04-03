package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.JokerView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowJokerView extends CB_Action_ShowView
{

	public CB_Action_ShowJokerView()
	{
		super("Jokers", AID_SHOW_JOKERS);
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
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(25);
	}
}
