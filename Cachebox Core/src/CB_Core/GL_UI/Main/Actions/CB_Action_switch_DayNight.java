package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Main.TabMainView;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_switch_DayNight extends CB_ActionCommand
{

	public CB_Action_switch_DayNight()
	{
		super("DayNight", AID_DAY_NIGHT);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(48);
	}

	@Override
	public void Execute()
	{
		TabMainView.that.switchDayNight();
		new CB_Action_ShowActivity("DayNight", CB_Action.AID_DAY_NIGHT, ViewConst.DAY_NIGHT, SpriteCache.Icons.get(48)).Execute();
	}
}
