package de.droidcachebox.menu.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.menuBtn5.executes.SettingsActivity;
import de.droidcachebox.settings.CB_UI_Base_Settings;

public class SettingsAction extends AbstractAction {

    private static SettingsAction that;
    boolean lastNightValue;

    private SettingsAction() {
        super("settings");
    }

    public static SettingsAction getInstance() {
        if (that == null) that = new SettingsAction();
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