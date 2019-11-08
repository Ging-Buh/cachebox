package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.views.SolverView;

public class Abstract_ShowSolverAction extends AbstractShowAction {

    private static Abstract_ShowSolverAction that;

    private Abstract_ShowSolverAction() {
        super("Solver", MenuID.AID_SHOW_SOLVER);
    }

    public static Abstract_ShowSolverAction getInstance() {
        if (that == null) that = new Abstract_ShowSolverAction();
        return that;
    }

    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(SolverView.getInstance());
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
