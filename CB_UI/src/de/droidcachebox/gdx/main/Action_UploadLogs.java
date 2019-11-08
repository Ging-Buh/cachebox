package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.activities.UploadDrafts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;

public class Action_UploadLogs extends AbstractAction {
    private static Action_UploadLogs that;

    private Action_UploadLogs() {
        super("directLog", MenuID.AID_UPLOAD_LOGS);
    }

    public static Action_UploadLogs getInstance() {
        if (that == null) that = new Action_UploadLogs();
        return that;
    }

    @Override
    public void Execute() {
        new UploadDrafts().upload(true);
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.UPLOADFIELDNOTE.name());
    }

}
