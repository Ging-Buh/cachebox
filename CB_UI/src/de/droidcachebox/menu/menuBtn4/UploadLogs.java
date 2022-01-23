package de.droidcachebox.menu.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.menuBtn4.executes.UploadDraftsOrLogs;

public class UploadLogs extends AbstractAction {
    private static UploadLogs instance;

    private UploadLogs() {
        super("UploadLogs");
    }

    public static UploadLogs getInstance() {
        if (instance == null) instance = new UploadLogs();
        return instance;
    }

    @Override
    public void execute() {
        new UploadDraftsOrLogs().upload(true);
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.UPLOADFIELDNOTE.name());
    }

}
