package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.views.DraftsView;

public class Abstract_ShowDraftsAction extends AbstractShowAction {

    private static Abstract_ShowDraftsAction that;

    private Abstract_ShowDraftsAction() {
        super("Drafts", MenuID.AID_SHOW_DRAFTS);
    }

    public static Abstract_ShowDraftsAction getInstance() {
        if (that == null) that = new Abstract_ShowDraftsAction();
        return that;
    }

    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(DraftsView.getInstance());
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
