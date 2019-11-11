package de.droidcachebox.main.menuBtn1.contextmenus;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.gdx.main.MenuID;

public class EditFilterSettings extends AbstractAction {

    private static EditFilterSettings that;

    private EditFilterSettings() {
        super("Filter", MenuID.AID_SHOW_FILTER_DIALOG);
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
    public void Execute() {
        de.droidcachebox.gdx.activities.EditFilterSettings edFi = new de.droidcachebox.gdx.activities.EditFilterSettings(ActivityBase.activityRec(), "Filter");
        edFi.show();

    }
}
