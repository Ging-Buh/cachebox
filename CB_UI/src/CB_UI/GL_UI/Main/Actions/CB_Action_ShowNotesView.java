package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GL_UI.Views.NotesView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowNotesView extends CB_Action_ShowView {

    private static CB_Action_ShowNotesView that;

    private CB_Action_ShowNotesView() {
        super("Notes", MenuID.AID_SHOW_NOTES);
    }

    public static CB_Action_ShowNotesView getInstance() {
        if (that == null) that = new CB_Action_ShowNotesView();
        return that;
    }

    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(NotesView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.userdata.name());
    }

    @Override
    public CB_View_Base getView() {
        return NotesView.getInstance();
    }
}
