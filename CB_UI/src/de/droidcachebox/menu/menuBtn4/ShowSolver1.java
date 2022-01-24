package de.droidcachebox.menu.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn4.executes.Solver;

public class ShowSolver1 extends AbstractShowAction {
    private Solver solver;

    public ShowSolver1() {
        super("Solver");
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
