package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.SpriteCacheBase;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Menu.Menu;
import CB_UI.GL_UI.Menu.MenuID;
import CB_UI.GL_UI.Views.SolverView2;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowSolverView2 extends CB_Action_ShowView
{

	public CB_Action_ShowSolverView2()
	{
		super("Solver v2", MenuID.AID_SHOW_SOLVER2);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.solverView2 == null) && (tabMainView != null) && (tab != null)) TabMainView.solverView2 = new SolverView2(
				tab.getContentRec(), "SolverView2");

		if ((TabMainView.solverView2 != null) && (tab != null)) tab.ShowView(TabMainView.solverView2);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.getThemedSprite("solver-icon-2");
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.solverView2;
	}

	@Override
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public Menu getContextMenu()
	{
		return TabMainView.solverView2.getContextMenu();
	}
}
