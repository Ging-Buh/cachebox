package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.ViewManager;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Action_switch_DayNight extends AbstractAction {

    private static Action_switch_DayNight that;

    private Action_switch_DayNight() {
        super("DayNight", MenuID.AID_DAY_NIGHT);
    }

    public static Action_switch_DayNight getInstance() {
        if (that == null) that = new Action_switch_DayNight();
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
        ViewManager.that.switchDayNight();
    }
}
