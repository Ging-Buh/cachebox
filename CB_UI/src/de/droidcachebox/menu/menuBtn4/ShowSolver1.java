package de.droidcachebox.menu.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn4.executes.SolverView;

public class ShowSolver1 extends AbstractShowAction {

    private static ShowSolver1 that;

    private ShowSolver1() {
        super("Solver");
    }

    public static ShowSolver1 getInstance() {
        if (that == null) that = new ShowSolver1();
        return that;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(SolverView.getInstance());
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
