package de.droidcachebox;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.main.Menu;

/**
 * extends the AbstractAction with getView, an extended CB_View_Base and an optional ContextMenu (hasContextMenu() and getContextMenu())
 * execute and getIcon must be implemented as extended from AbstractAction.
 * there is no default constructor, cause there should always be a text in the menu, that describes the action that will occur on clicking.
 * the translationId string in the constructor makes the language dependant text of the menu line
 * An icon in menu is not necessary (== null)
 * the execute() in an AbstractAction has to perform the wanted action,
 * in this case normally a showView of the instance of a subclass of CB_View_Base (which extends CB_View_Base)
 *
 * by default (if not overwitten) the view has no context menu
 *
 * today we have 15+2 defined ShowActions spread over the 5 main buttons by addAction in the ViewManager.java
 */
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
