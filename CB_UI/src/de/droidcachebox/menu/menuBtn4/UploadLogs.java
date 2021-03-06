package de.droidcachebox.menu.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractAction;
import de.droidcachebox.activities.UploadDraftsOrLogs;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;

public class UploadLogs extends AbstractAction {
    private static UploadLogs that;

    private UploadLogs() {
        super("directLog");
    }

    public static UploadLogs getInstance() {
        if (that == null) that = new UploadLogs();
        return that;
    }

    @Override
    public void execute() {
        new UploadDraftsOrLogs().upload(true);
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.UPLOADFIELDNOTE.name());
    }

}
