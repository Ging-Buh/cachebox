package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Controls.Dialogs.DeleteDialog;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Action_DeleteDialog extends AbstractAction {

    private static Action_DeleteDialog that;

    private Action_DeleteDialog() {
        super("DeleteCaches", MenuID.AID_SHOW_DELETE_DIALOG);
    }

    public static Action_DeleteDialog getInstance() {
        if (that == null) that = new Action_DeleteDialog();
        return that;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.DELETE.name());
    }

    @Override
    public void Execute() {
        DeleteDialog d = new DeleteDialog();
        d.Show();
    }
}
