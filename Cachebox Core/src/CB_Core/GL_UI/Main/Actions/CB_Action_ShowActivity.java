package CB_Core.GL_UI.Main.Actions;

import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewID;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowActivity extends CB_Action_ShowView
{
	private ViewID viewConst;

	public CB_Action_ShowActivity(String Name, int ID, ViewID ViewConst)
	{
		super(Name, ID);
		viewConst = ViewConst;
	}

	@Override
	public void Execute()
	{
		platformConector.showView(viewConst, 0, 0, 0, 0);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(13);
	}

	@Override
	public CB_View_Base getView()
	{
		return null;
	}
}
