package de.droidcachebox.main.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractShowAction;
import de.droidcachebox.gdx.main.MenuID;
import de.droidcachebox.gdx.views.SolverView;
import de.droidcachebox.main.ViewManager;

public class ShowSolver1 extends AbstractShowAction {

    private static ShowSolver1 that;

    private ShowSolver1() {
        super("Solver", MenuID.AID_SHOW_SOLVER);
    }

    public static ShowSolver1 getInstance() {
        if (that == null) that = new ShowSolver1();
        return that;
    }

    @Override
    public void execute() {
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
