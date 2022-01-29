package de.droidcachebox.menu.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.popups.SearchDialog;

public class ShowSearchDialog extends AbstractAction {

    private boolean searchDialogIsRunning;
    private SearchDialog searchDialog;

    public ShowSearchDialog() {
        super("Search");
        searchDialogIsRunning = false;
    }

    @Override
    public void execute() {
        if (searchDialog == null || searchDialog.isDisposed())
            searchDialog = new SearchDialog();
        searchDialog.showNotCloseAutomaticly();
        searchDialogIsRunning = true;
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
            searchDialog.close(); // will implicit dispose searchDialog
            searchDialogIsRunning = false;
        }
    }

    public void showAgain() {
        if (searchDialogIsRunning) {
            searchDialog.showNotCloseAutomaticly();
        } else {
            execute();
        }
    }
}
