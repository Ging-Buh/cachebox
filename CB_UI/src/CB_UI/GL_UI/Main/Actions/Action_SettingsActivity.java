package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Activitys.settings.SettingsActivity;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import com.badlogic.gdx.graphics.g2d.Sprite;

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