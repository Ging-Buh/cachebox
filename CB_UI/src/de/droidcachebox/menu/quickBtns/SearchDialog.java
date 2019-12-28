package de.droidcachebox.menu.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.views.CacheListView;
import de.droidcachebox.menu.menuBtn1.ShowCacheList;

public class SearchDialog extends AbstractAction {

    private static SearchDialog that;

    private SearchDialog() {
        super("Search");
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
