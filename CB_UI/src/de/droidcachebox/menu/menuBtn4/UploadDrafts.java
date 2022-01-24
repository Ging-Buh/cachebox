package de.droidcachebox.menu.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.menuBtn4.executes.UploadDraftsOrLogs;

public class UploadDrafts extends AbstractAction {

    public UploadDrafts() {
        super("uploadDrafts");
    }

    @Override
    public void execute() {
        new UploadDraftsOrLogs().upload(false);
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.UPLOADFIELDNOTE.name());
    }

}
