package de.droidcachebox.main.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.activities.UploadDraftsOrLogs;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.gdx.main.MenuID;

public class UploadLogs extends AbstractAction {
    private static UploadLogs that;

    private UploadLogs() {
        super("directLog", MenuID.AID_UPLOAD_LOGS);
    }

    public static UploadLogs getInstance() {
        if (that == null) that = new UploadLogs();
        return that;
    }

    @Override
    public void Execute() {
        new UploadDraftsOrLogs().upload(true);
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.UPLOADFIELDNOTE.name());
    }

}
