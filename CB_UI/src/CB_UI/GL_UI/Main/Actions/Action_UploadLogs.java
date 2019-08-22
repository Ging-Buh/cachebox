package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Activitys.UploadDrafts;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Action_UploadLogs  extends AbstractAction {
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
