package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Activitys.UploadDrafts;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

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
