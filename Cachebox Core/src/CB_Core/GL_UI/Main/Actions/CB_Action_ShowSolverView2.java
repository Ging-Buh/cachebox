package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.SolverView2;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowSolverView2 extends CB_Action_ShowView
{
	public final int MI_TEST_1 = 1;
	public final int MI_CHANGE_LINE = 2;
	public final int MI_DELETE_LINE = 3;
	public final int MI_INSERT_LINE = 4;
	public final int MI_SET_AS_WAYPOINT = 5;
	public final int MI_SET_AS_MAPCENTER = 6;

	public CB_Action_ShowSolverView2()
	{
		super("Solver v2", AID_SHOW_SOLVER2);
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
		return SpriteCache.Icons.get(17);
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
	public boolean ShowContextMenu()
	{
		Menu cm = new Menu("CacheListContextMenu");

		cm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MI_CHANGE_LINE:
					TabMainView.solverView2.ChangeLine();
					return true;
				case MI_INSERT_LINE:
					TabMainView.solverView2.InsertLine();
					return true;
				case MI_DELETE_LINE:
					TabMainView.solverView2.DeleteLine();
					return true;
				case MI_SET_AS_WAYPOINT:
					TabMainView.solverView2.SetAsWaypoint();
					break;
				case MI_SET_AS_MAPCENTER:
					TabMainView.solverView2.SetAsMapCenter();
					break;
				}
				return false;
			}
		});

		MenuItem mi;
		cm.addItem(MI_CHANGE_LINE, "Zeile ändern", SpriteCache.Icons.get(13));
		cm.addItem(MI_INSERT_LINE, "Zeile einfügen", SpriteCache.Icons.get(13));
		cm.addItem(MI_DELETE_LINE, "Zeile löschen", SpriteCache.Icons.get(13));
		cm.addItem(MI_SET_AS_WAYPOINT, "Waypoint einfügen", SpriteCache.Icons.get(13));
		cm.addItem(MI_SET_AS_MAPCENTER, "Map-Center setzen", SpriteCache.Icons.get(13));
		cm.show();

		return true;
	}
}
