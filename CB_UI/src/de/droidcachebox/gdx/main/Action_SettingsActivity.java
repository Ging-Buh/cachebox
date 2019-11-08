package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.CB_UI_Base_Settings;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.settings.SettingsActivity;

public class Action_SettingsActivity extends AbstractAction {

    private static Action_SettingsActivity that;
    boolean lastNightValue;

    private Action_SettingsActivity() {
        super("settings", MenuID.AID_SHOW_SETTINGS);
    }

    public static Action_SettingsActivity getInstance() {
        if (that == null) that = new Action_SettingsActivity();
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
    public void Execute() {

        SettingsActivity settingsActivity = new SettingsActivity();
        lastNightValue = CB_UI_Base_Settings.nightMode.getValue();

        settingsActivity.show();
    }

}
