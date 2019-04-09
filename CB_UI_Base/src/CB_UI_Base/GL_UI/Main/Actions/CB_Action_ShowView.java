package CB_UI_Base.GL_UI.Main.Actions;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Menu.Menu;

public abstract class CB_Action_ShowView extends CB_Action {

    public CB_Action_ShowView(String translationId, int id) {
        super(translationId, id);
    }

    public CB_Action_ShowView(String translationId, String translationExtension, int id) {
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
