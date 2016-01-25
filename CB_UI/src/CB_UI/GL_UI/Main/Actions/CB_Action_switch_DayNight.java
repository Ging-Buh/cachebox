package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_switch_DayNight extends CB_Action {

    public CB_Action_switch_DayNight() {
	super("DayNight", MenuID.AID_DAY_NIGHT);
    }

    @Override
    public boolean getEnabled() {
	return true;
    }

    @Override
    public Sprite getIcon() {
	return SpriteCacheBase.Icons.get(IconName.DayNight_48.ordinal());
    }

    @Override
    public void Execute() {
	TabMainView.that.switchDayNight();
	// new CB_Action_ShowActivity("DayNight", MenuID.AID_DAY_NIGHT, ViewConst.DAY_NIGHT, SpriteCache.Icons.get(IconName.DayNight_48
	// .ordinal())).Execute();
    }
}
