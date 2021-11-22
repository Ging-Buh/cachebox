package de.droidcachebox.menu.menuBtn1.contextmenus;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.menuBtn1.executes.DeleteDialog;

public class ShowDeleteDialog extends AbstractAction {

    private static ShowDeleteDialog that;

    private ShowDeleteDialog() {
        super("DeleteCaches");
    }

    public static ShowDeleteDialog getInstance() {
        if (that == null) that = new ShowDeleteDialog();
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
        DeleteDialog d = new DeleteDialog();
        d.show();
    }
}
