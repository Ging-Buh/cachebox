package CB_Core.GL_UI.Main.Actions;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Menu.MenuID;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_switch_Autoresort extends CB_ActionCommand
{

	public CB_Action_switch_Autoresort()
	{
		super("AutoResort", MenuID.AID_AUTO_RESORT);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(16);
	}

	@Override
	public void Execute()
	{
		GlobalCore.autoResort = !(GlobalCore.autoResort);
		Config.settings.AutoResort.setValue(GlobalCore.autoResort);
		if (GlobalCore.autoResort)
		{
			synchronized (Database.Data.Query)
			{
				Database.Data.Query.Resort();
			}
		}
	}
}
