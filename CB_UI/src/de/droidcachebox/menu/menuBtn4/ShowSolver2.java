package de.droidcachebox.menu.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.views.SolverView2;
import de.droidcachebox.menu.ViewManager;

public class ShowSolver2 extends AbstractShowAction {

    private static ShowSolver2 that;

    private ShowSolver2() {
        super("Solver v2");
    }

    public static ShowSolver2 getInstance() {
        if (that == null) that = new ShowSolver2();
        return that;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(SolverView2.getInstance());
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
