package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.FieldNotesView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowFieldNotesView extends CB_Action_ShowView {

    private static CB_Action_ShowFieldNotesView that;

    private CB_Action_ShowFieldNotesView() {
        super("Fieldnotes", MenuID.AID_SHOW_FIELDNOTES);
        tabMainView = TabMainView.that;
        tab = TabMainView.leftTab;
    }

    public static CB_Action_ShowFieldNotesView getInstance() {
        if (that == null) that = new CB_Action_ShowFieldNotesView();
        return that;
    }

    @Override
    public void Execute() {
        if ((TabMainView.fieldNotesView == null) && (tabMainView != null) && (tab != null))
            TabMainView.fieldNotesView = new FieldNotesView(tab.getContentRec(), "FieldNotesView");

        if ((TabMainView.fieldNotesView != null) && (tab != null))
            tab.ShowView(TabMainView.fieldNotesView);
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.fieldnoteListIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return TabMainView.fieldNotesView;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return FieldNotesView.that.getContextMenu();
    }
}
