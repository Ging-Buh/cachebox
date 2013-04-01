package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Main.TabMainView;
import CB_Core.GL_UI.Menu.MenuID;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_switch_DayNight extends CB_ActionCommand
{

	public CB_Action_switch_DayNight()
	{
		super("DayNight", MenuID.AID_DAY_NIGHT);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(IconName.DayNight_48.ordinal());
	}

	@Override
	public void Execute()
	{
		TabMainView.that.switchDayNight();
		new CB_Action_ShowActivity("DayNight", MenuID.AID_DAY_NIGHT, ViewConst.DAY_NIGHT, SpriteCache.Icons.get(IconName.DayNight_48
				.ordinal())).Execute();
	}
}
