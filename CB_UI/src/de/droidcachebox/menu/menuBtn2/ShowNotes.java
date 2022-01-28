package de.droidcachebox.menu.menuBtn2;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn2.executes.NotesView;

public class ShowNotes extends AbstractShowAction {
    private NotesView notesView;

    public ShowNotes() {
        super("Notes");
    }

    @Override
    public void execute() {
        if (notesView == null)
            notesView = new NotesView();
        ViewManager.leftTab.showView(notesView);
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
        return notesView;
    }

    @Override
    public void viewIsHiding() {
        notesView = null;
    }
}
