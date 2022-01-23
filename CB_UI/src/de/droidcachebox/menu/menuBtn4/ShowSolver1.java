package de.droidcachebox.menu.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn4.executes.Solver;

public class ShowSolver1 extends AbstractShowAction {
    private static ShowSolver1 instance;
    private Solver solver;

    private ShowSolver1() {
        super("Solver");
    }

    public static ShowSolver1 getInstance() {
        if (instance == null) instance = new ShowSolver1();
        return instance;
    }

    @Override
    public void execute() {
        solver = new Solver();
        ViewManager.leftTab.showView(solver);
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
        return solver;
    }
}
