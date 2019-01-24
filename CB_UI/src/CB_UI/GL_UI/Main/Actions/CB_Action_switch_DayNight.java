package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_switch_DayNight extends CB_Action {

    private static CB_Action_switch_DayNight that;

    private CB_Action_switch_DayNight() {
        super("DayNight", MenuID.AID_DAY_NIGHT);
    }

    public static CB_Action_switch_DayNight getInstance() {
        if (that == null) that = new CB_Action_switch_DayNight();
        return that;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.DayNight.name());
    }

    @Override
    public void Execute() {
        TabMainView.that.switchDayNight();
    }
}
