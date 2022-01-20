package de.droidcachebox.menu.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.popups.SearchDialog;
import de.droidcachebox.menu.menuBtn1.ShowGeoCaches;
import de.droidcachebox.menu.menuBtn1.executes.GeoCaches;
import de.droidcachebox.utils.log.Log;

public class ShowSearchDialog extends AbstractAction {

    private static ShowSearchDialog that;
    private boolean searchDialogIsRunning;
    private SearchDialog searchDialog;

    private ShowSearchDialog() {
        super("Search");
        searchDialogIsRunning = false;
    }

    public static ShowSearchDialog getInstance() {
        if (that == null) that = new ShowSearchDialog();
        return that;
    }

    @Override
    public void execute() {
        if (!GeoCaches.getInstance().isVisible()) {
            ShowGeoCaches.getInstance().execute();
        }
        searchDialog = new SearchDialog();
        searchDialogIsRunning = true;
        searchDialog.showNotCloseAutomaticly();
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.lupe.name());
    }

    public void closeSearchDialog() {
        if (searchDialogIsRunning) {
            searchDialog.close();
            searchDialogIsRunning = false;
        }
    }

    public void showAgain() {
        if (searchDialog != null && searchDialog.isDisposed()) {
            Log.info("searchDialog show again", "recreation after dispose.");
            execute();
            return;
        }
        if (searchDialogIsRunning) {
            searchDialog.showNotCloseAutomaticly();
        }
        else {
            execute();
        }
    }
}
