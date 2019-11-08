package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.activities.UploadDrafts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;

public class Action_UploadDrafts extends AbstractAction {
    private static Action_UploadDrafts that;

    private Action_UploadDrafts() {
        super("uploadDrafts", MenuID.AID_UPLOAD_DRAFT);
    }

    public static Action_UploadDrafts getInstance() {
        if (that == null) that = new Action_UploadDrafts();
        return that;
    }

    @Override
    public void Execute() {
        new UploadDrafts().upload(false);
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.UPLOADFIELDNOTE.name());
    }

}
