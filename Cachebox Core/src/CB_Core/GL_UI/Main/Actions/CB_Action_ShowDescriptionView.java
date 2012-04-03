package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.DescriptionView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowDescriptionView extends CB_Action_ShowView
{

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
}
