package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;

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
