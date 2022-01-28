package de.droidcachebox.menu.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn4.executes.Solver1View;

public class ShowSolver1 extends AbstractShowAction {
    private Solver1View solver1View;

    public ShowSolver1() {
        super("Solver");
    }

    @Override
    public void execute() {
        if (solver1View == null)
            solver1View = new Solver1View();
        ViewManager.leftTab.showView(solver1View);
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
        return solver1View;
    }

    @Override
    public void viewIsHiding() {
        solver1View = null;
    }
}
