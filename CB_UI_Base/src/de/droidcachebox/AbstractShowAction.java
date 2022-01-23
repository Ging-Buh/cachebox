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
 * <p>
 * by default (if not overwritten) the view has no context menu
 * <p>
 * today we have 15+2 defined ShowActions spread over the 5 main buttons by addAction in the ViewManager.java
 */
public abstract class AbstractShowAction extends AbstractAction {

    public AbstractShowAction(String translationId) {
        super(translationId);
    }

    public AbstractShowAction(String translationId, String translationExtension) {
        this(translationId);
        titleExtension = translationExtension;
    }

    /**
     * returns the instance of the view<br>
     * if not null it will be rendered (may be null)
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
     * returns the ContextMenu of this View
     *
     * @return the Menu
     */
    public Menu getContextMenu() {
        return null;
    }
}
