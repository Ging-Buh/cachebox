package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Views.CreditsView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowCreditsView extends CB_Action_ShowView
{
	public CB_Action_ShowCreditsView()
	{
		super("Credits", MenuID.AID_SHOW_CREDITS);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.creditsView == null) && (tabMainView != null) && (tab != null)) TabMainView.creditsView = new CreditsView(
				tab.getContentRec(), "CreditsView");

		if ((TabMainView.creditsView != null) && (tab != null)) tab.ShowView(TabMainView.creditsView);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(IconName.cb_49.ordinal());
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.creditsView;
	}

}
