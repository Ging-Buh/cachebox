package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.CacheListView;
import CB_UI.GL_UI.Views.MapView;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Show_Search extends CB_Action
{

	public CB_Action_Show_Search()
	{
		super("search", MenuID.AID_SEARCH);
	}

	@Override
	public void Execute()
	{

		if (MapView.that == null || !MapView.that.isVisible())
		{
			if (CacheListView.that == null || !CacheListView.that.isVisible())
			{
				TabMainView.actionShowCacheList.Execute();
			}
		}

		if (SearchDialog.that == null)
		{
			new SearchDialog();
		}

		SearchDialog.that.showNotCloseAutomaticly();

	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.Icons.get(IconName.lupe_27.ordinal());
	}
}