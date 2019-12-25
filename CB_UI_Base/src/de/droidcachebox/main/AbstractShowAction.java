package de.droidcachebox.main;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.main.Menu;

public abstract class AbstractShowAction extends AbstractAction {

    public AbstractShowAction(String translationId) {
        super(translationId);
    }

    public AbstractShowAction(String translationId, String translationExtension) {
        super(translationId, translationExtension);
    }

    /**
     * returns the instance of the view<br>
     *
     * @return CB_View_Base
     */
    public abstract CB_View_Base getView();

    /**
     * @return if has
     */
    public boolean hasContextMenu() {
        return false;
    }

    /**
     * gibt das ContextMenu dieser View zurück
     *
     * @return the Menu
     */
    public Menu getContextMenu() {
        return null;
    }
}
