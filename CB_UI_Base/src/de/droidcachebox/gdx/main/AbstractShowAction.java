package de.droidcachebox.gdx.main;

import de.droidcachebox.gdx.CB_View_Base;

public abstract class AbstractShowAction extends AbstractAction {

    public AbstractShowAction(String translationId, int id) {
        super(translationId, id);
    }

    public AbstractShowAction(String translationId, String translationExtension, int id) {
        super(translationId, translationExtension, id);
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
