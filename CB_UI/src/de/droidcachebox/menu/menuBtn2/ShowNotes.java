package de.droidcachebox.menu.menuBtn2;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn2.executes.Notes;

public class ShowNotes extends AbstractShowAction {

    private static ShowNotes showNotes;

    private ShowNotes() {
        super("Notes");
    }

    public static ShowNotes getInstance() {
        if (showNotes == null) showNotes = new ShowNotes();
        return showNotes;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(Notes.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.userdata.name());
    }

    @Override
    public CB_View_Base getView() {
        return Notes.getInstance();
    }
}
