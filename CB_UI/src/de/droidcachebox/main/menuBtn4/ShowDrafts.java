package de.droidcachebox.main.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.views.DraftsView;
import de.droidcachebox.main.AbstractShowAction;
import de.droidcachebox.main.ViewManager;

public class ShowDrafts extends AbstractShowAction {

    private static ShowDrafts that;

    private ShowDrafts() {
        super("Drafts");
    }

    public static ShowDrafts getInstance() {
        if (that == null) that = new ShowDrafts();
        return that;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(DraftsView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.fieldnoteListIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return DraftsView.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return DraftsView.getInstance().getContextMenu();
    }
}
