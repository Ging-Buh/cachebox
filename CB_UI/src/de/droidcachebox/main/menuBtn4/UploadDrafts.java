package de.droidcachebox.main.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.activities.UploadDraftsOrLogs;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.gdx.main.MenuID;

public class UploadDrafts extends AbstractAction {
    private static UploadDrafts that;

    private UploadDrafts() {
        super("uploadDrafts", MenuID.AID_UPLOAD_DRAFT);
    }

    public static UploadDrafts getInstance() {
        if (that == null) that = new UploadDrafts();
        return that;
    }

    @Override
    public void Execute() {
        new UploadDraftsOrLogs().upload(false);
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.UPLOADFIELDNOTE.name());
    }

}
