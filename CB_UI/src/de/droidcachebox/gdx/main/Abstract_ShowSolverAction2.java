package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.views.SolverView2;

public class Abstract_ShowSolverAction2 extends AbstractShowAction {

    private static Abstract_ShowSolverAction2 that;

    private Abstract_ShowSolverAction2() {
        super("Solver v2", MenuID.AID_SHOW_SOLVER2);
    }

    public static Abstract_ShowSolverAction2 getInstance() {
        if (that == null) that = new Abstract_ShowSolverAction2();
        return that;
    }

    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(SolverView2.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite("solver-icon-2");
    }

    @Override
    public CB_View_Base getView() {
        return SolverView2.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return SolverView2.getInstance().getContextMenu();
    }
}
