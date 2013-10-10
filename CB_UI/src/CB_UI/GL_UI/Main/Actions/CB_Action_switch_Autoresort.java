package CB_UI.GL_UI.Main.Actions;

import CB_Core.DB.Database;
import CB_Core.Types.CacheWithWP;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_ActionCommand;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;

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
		return SpriteCacheBase.Icons.get(IconName.autoSelectOff_16.ordinal());
	}

	@Override
	public void Execute()
	{
		GlobalCore.setAutoResort(!(GlobalCore.getAutoResort()));
		if (GlobalCore.getAutoResort())
		{
			synchronized (Database.Data.Query)
			{
				CacheWithWP ret = Database.Data.Query.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(),
						GlobalCore.getSelectedWaypoint()));

				GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
				GlobalCore.NearestCache(ret.getCache());
				ret.dispose();
			}
		}
	}
}
