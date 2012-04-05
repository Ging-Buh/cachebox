package CB_Core.GL_UI.Main.Actions;

import CB_Core.Events.platformConector;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowSettings extends CB_Action_ShowView
{

	public CB_Action_ShowSettings()
	{
		super("settings", AID_SHOW_SETTINGS);
	}

	@Override
	public void Execute()
	{
		platformConector.showView(ViewConst.SETTINGS, 0, 0, 0, 0);
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
	public CB_View_Base getView()
	{
		return null;
	}
}
