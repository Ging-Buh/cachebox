package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Views.TrackableListView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTrackableListView extends CB_Action_ShowView
{
	public CB_Action_ShowTrackableListView()
	{
		super("TBList", AID_SHOW_TRACKABLELIST);
	}

	@Override
	public void Execute()
	{
		if ((TabMainView.trackableListView == null) && (tabMainView != null) && (tab != null)) TabMainView.trackableListView = new TrackableListView(
				tab.getContentRec(), "TrackableListView");

		if ((TabMainView.trackableListView != null) && (tab != null)) tab.ShowView(TabMainView.trackableListView);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(38);
	}

	@Override
	public CB_View_Base getView()
	{
		return TabMainView.trackableListView;
	}

}
