package de.droidcachebox.main.menuBtn1.contextmenus;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.gdx.main.MenuID;

public class DeleteDialog extends AbstractAction {

    private static DeleteDialog that;

    private DeleteDialog() {
        super("DeleteCaches", MenuID.AID_SHOW_DELETE_DIALOG);
    }

    public static DeleteDialog getInstance() {
        if (that == null) that = new DeleteDialog();
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
    public void execute() {
        de.droidcachebox.gdx.controls.dialogs.DeleteDialog d = new de.droidcachebox.gdx.controls.dialogs.DeleteDialog();
        d.show();
    }
}
