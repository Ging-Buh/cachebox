package de.droidcachebox.main.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.gdx.main.MenuID;
import de.droidcachebox.gdx.views.CacheListView;
import de.droidcachebox.main.menuBtn1.ShowCacheList;

public class SearchDialog extends AbstractAction {

    private static SearchDialog that;

    private SearchDialog() {
        super("Search", MenuID.AID_SEARCH);
    }

    public static SearchDialog getInstance() {
        if (that == null) that = new SearchDialog();
        return that;
    }

    @Override
    public void execute() {

        if (!CacheListView.getInstance().isVisible()) {
            ShowCacheList.getInstance().execute();
        }

        if (de.droidcachebox.gdx.controls.popups.SearchDialog.that == null) {
            new de.droidcachebox.gdx.controls.popups.SearchDialog();
        }
        de.droidcachebox.gdx.controls.popups.SearchDialog.that.showNotCloseAutomaticly();
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
