package de.droidcachebox.main.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.main.AbstractAction;

public class EditFilterSettings extends AbstractAction {

    private static EditFilterSettings that;

    private EditFilterSettings() {
        super("Filter");
    }

    public static EditFilterSettings getInstance() {
        if (that == null) that = new EditFilterSettings();
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
        de.droidcachebox.gdx.activities.EditFilterSettings edFi = new de.droidcachebox.gdx.activities.EditFilterSettings(ActivityBase.activityRec(), "Filter");
        edFi.show();

    }
}
