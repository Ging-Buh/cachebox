package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.GL_UI.Views.MapView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowMap extends CB_Action_ShowView
{
	public final int MI_LAYER = 1;
	public final int MI_ROTATE = 2;

	public CB_Action_ShowMap()
	{
		super("Map", AID_SHOW_MAP);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.mapView == null) && (tabMainView != null) && (tab != null)) TabMainView.mapView = new MapView(tab.getContentRec(),
				"MapView");

		if ((TabMainView.mapView != null) && (tab != null)) tab.ShowView(TabMainView.mapView);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.mapView;
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
	public boolean HasContextMenu()
	{
		return true;
	}

	@Override
	public boolean ShowContextMenu()
	{
		Menu cm = new Menu("MapViewContextMenu");

		cm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MI_ROTATE:
					TabMainView.mapView.SetAlignToCompass(!TabMainView.mapView.GetAlignToCompass());
					return true;

				}
				return false;
			}
		});

		MenuItem mi;

		mi = cm.addItem(MI_LAYER, "Layer");
		mi = cm.addItem(MI_ROTATE, "Rotate Map");
		mi.setCheckable(true);
		mi.setChecked(TabMainView.mapView.GetAlignToCompass());

		cm.show();
		return true;
	}

}
