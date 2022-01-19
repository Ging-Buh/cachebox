package de.droidcachebox.menu.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.menuBtn5.executes.SettingsActivity;

public class SettingsAction extends AbstractAction {

    private static SettingsAction that;
    public boolean isExecuting;

    private SettingsAction() {
        super("settings");
    }

    public static SettingsAction getInstance() {
        if (that == null) {
            that = new SettingsAction();
        }
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
        isExecuting = true;
        new SettingsActivity().show();
    }

}
