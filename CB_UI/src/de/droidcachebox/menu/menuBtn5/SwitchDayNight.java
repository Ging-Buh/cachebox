package de.droidcachebox.menu.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.ViewManager;

public class SwitchDayNight extends AbstractAction {

    private static SwitchDayNight instance;

    private SwitchDayNight() {
        super("DayNight");
    }

    public static SwitchDayNight getInstance() {
        if (instance == null) instance = new SwitchDayNight();
        return instance;
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
    public void execute() {
        ViewManager.that.switchDayNight();
    }
}
