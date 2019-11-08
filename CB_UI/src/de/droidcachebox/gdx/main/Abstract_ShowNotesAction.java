package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.views.NotesView;

public class Abstract_ShowNotesAction extends AbstractShowAction {

    private static Abstract_ShowNotesAction that;

    private Abstract_ShowNotesAction() {
        super("Notes", MenuID.AID_SHOW_NOTES);
    }

    public static Abstract_ShowNotesAction getInstance() {
        if (that == null) that = new Abstract_ShowNotesAction();
        return that;
    }

    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(NotesView.getInstance());
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
