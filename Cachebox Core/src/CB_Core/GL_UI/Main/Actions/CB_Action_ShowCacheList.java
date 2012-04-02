package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.CacheListView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowCacheList extends CB_Action_ShowView
{

	public CB_Action_ShowCacheList()
	{
		super("CacheList", AID_SHOW_CACHELIST);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.cacheListView == null) && (tabMainView != null)) TabMainView.cacheListView = new CacheListView(tabMainView,
				"CacheListView");

		if ((TabMainView.cacheListView != null) && (tab != null)) tab.ShowView(TabMainView.cacheListView);
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
}
