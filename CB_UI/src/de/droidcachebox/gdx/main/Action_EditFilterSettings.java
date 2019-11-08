package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.EditFilterSettings;

public class Action_EditFilterSettings extends AbstractAction {

    private static Action_EditFilterSettings that;

    private Action_EditFilterSettings() {
        super("Filter", MenuID.AID_SHOW_FILTER_DIALOG);
    }

    public static Action_EditFilterSettings getInstance() {
        if (that == null) that = new Action_EditFilterSettings();
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
    public void Execute() {
        EditFilterSettings edFi = new EditFilterSettings(ActivityBase.activityRec(), "Filter");
        edFi.show();

    }
}
