package de.droidcachebox.menu.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.popups.SearchDialog;

public class ShowSearchDialog extends AbstractAction {

    // this will only be hidden(removed from View) and never be disposed because of showNotCloseAutomatically
    SearchDialog searchDialog;

    public ShowSearchDialog() {
        super("Search");
        searchDialog = new SearchDialog();
    }

    @Override
    public void execute() {
        searchDialog.showNotCloseAutomatically();
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.lupe.name());
    }

    public void doSearchOnline(final String searchPattern, final SearchDialog.SearchMode searchMode) {
        searchDialog.doSearchOnline(searchPattern, searchMode);
    }

    public float getHeightOfSearchDialog() {
        return searchDialog.getHeight();
    }
}
