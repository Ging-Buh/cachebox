package de.droidcachebox.menu.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractAction;
import de.droidcachebox.CB_UI_Base_Settings;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.settings.SettingsActivity;

public class Settings extends AbstractAction {

    private static Settings that;
    boolean lastNightValue;

    private Settings() {
        super("settings");
    }

    public static Settings getInstance() {
        if (that == null) that = new Settings();
        return that;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.settings.name());
    }

    @Override
    public void execute() {

        SettingsActivity settingsActivity = new SettingsActivity();
        lastNightValue = CB_UI_Base_Settings.nightMode.getValue();

        settingsActivity.show();
    }

}
