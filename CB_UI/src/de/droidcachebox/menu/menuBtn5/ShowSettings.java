package de.droidcachebox.menu.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.menuBtn5.executes.ManageSettings;

public class ShowSettings extends AbstractAction {

    private static ShowSettings instance;
    private boolean isExecuting;
    private ManageSettings manageSettings;

    private ShowSettings() {
        super("settings");
    }

    public static ShowSettings getInstance() {
        if (instance == null) {
            instance = new ShowSettings();
        }
        return instance;
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
        manageSettings = new ManageSettings();
        manageSettings.show();
    }

    public void onHide() {
        isExecuting = false;
        manageSettings = null;
    }

    public void returnFromFetchingApiKey() {
        if (isExecuting) {
            GL.that.runOnGL(() -> {
                manageSettings.resortList();
                manageSettings.loginHead.expand();
                GL.that.renderOnce(true);
            });
        }
    }

}
