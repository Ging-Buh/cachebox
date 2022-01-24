package de.droidcachebox.menu.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.EditFilterSettings;

public class ShowEditFilterSettings extends AbstractAction {

    private static ShowEditFilterSettings that;

    private ShowEditFilterSettings() {
        super("Filter");
    }

    public static ShowEditFilterSettings getInstance() {
        if (that == null) that = new ShowEditFilterSettings();
        return that;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.filter.name());
    }

    @Override
    public void execute() {
        new EditFilterSettings("Filter").show();
    }
}
