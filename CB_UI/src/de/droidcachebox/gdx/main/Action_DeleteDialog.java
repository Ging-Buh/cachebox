package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.dialogs.DeleteDialog;

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
        d.show();
    }
}
