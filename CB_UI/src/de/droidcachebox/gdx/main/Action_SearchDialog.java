package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.popups.SearchDialog;
import de.droidcachebox.gdx.views.CacheListView;

public class Action_SearchDialog extends AbstractAction {

    private static Action_SearchDialog that;

    private Action_SearchDialog() {
        super("Search", MenuID.AID_SEARCH);
    }

    public static Action_SearchDialog getInstance() {
        if (that == null) that = new Action_SearchDialog();
        return that;
    }

    @Override
    public void Execute() {

        if (!CacheListView.getInstance().isVisible()) {
            Abstract_ShowCacheList.getInstance().Execute();
        }

        if (SearchDialog.that == null) {
            new SearchDialog();
        }
        SearchDialog.that.showNotCloseAutomaticly();
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.lupe.name());
    }
}
