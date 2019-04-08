package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.SolverView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowSolverView extends CB_Action_ShowView {

    private static CB_Action_ShowSolverView that;

    public CB_Action_ShowSolverView() {
        super("Solver", MenuID.AID_SHOW_SOLVER);
    }

    public static CB_Action_ShowSolverView getInstance() {
        if (that == null) that = new CB_Action_ShowSolverView();
        return that;
    }

    @Override
    public void Execute() {
        TabMainView.leftTab.ShowView(SolverView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.solverIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return SolverView.getInstance();
    }
}
