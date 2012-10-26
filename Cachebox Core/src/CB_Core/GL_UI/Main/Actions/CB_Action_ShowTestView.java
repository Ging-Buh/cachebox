package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Views.TestViews.TestView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTestView extends CB_Action_ShowView
{

	public CB_Action_ShowTestView()
	{
		super("TestView", MenuID.AID_TEST_VIEW);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.testView == null) && (tabMainView != null) && (tab != null)) TabMainView.testView = new TestView(
				tab.getContentRec(), "TestView");

		if ((TabMainView.testView != null) && (tab != null)) tab.ShowView(TabMainView.testView);
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

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.testView;
	}
}
