package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.TrackListView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTrackListView extends CB_Action_ShowView
{

	public CB_Action_ShowTrackListView()
	{
		super("TrackList", AID_SHOW_TRACKLIST);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.trackListView == null) && (tabMainView != null) && (tab != null)) TabMainView.trackListView = new TrackListView(
				tab.getContentRec(), "TrackListView");

		if ((TabMainView.trackListView != null) && (tab != null)) tab.ShowView(TabMainView.trackListView);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(8);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.trackListView;
	}
}
