package de.droidcachebox.main.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.gdx.main.MenuID;
import de.droidcachebox.main.ViewManager;

public class SwitchDayNight extends AbstractAction {

    private static SwitchDayNight that;

    private SwitchDayNight() {
        super("DayNight", MenuID.AID_DAY_NIGHT);
    }

    public static SwitchDayNight getInstance() {
        if (that == null) that = new SwitchDayNight();
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
