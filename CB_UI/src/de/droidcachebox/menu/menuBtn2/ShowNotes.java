package de.droidcachebox.menu.menuBtn2;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.views.NotesView;
import de.droidcachebox.menu.ViewManager;

public class ShowNotes extends AbstractShowAction {

    private static ShowNotes that;

    private ShowNotes() {
        super("Notes");
    }

    public static ShowNotes getInstance() {
        if (that == null) that = new ShowNotes();
        return that;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(NotesView.getInstance());
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
        return NotesView.getInstance();
    }
}
