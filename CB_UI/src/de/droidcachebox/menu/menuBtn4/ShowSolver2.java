package de.droidcachebox.menu.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn4.executes.Solver2View;

public class ShowSolver2 extends AbstractShowAction {

    private Solver2View solver2View;

    public ShowSolver2() {
        super("Solver v2");
    }

    @Override
    public void execute() {
        solver2View = new Solver2View();
        ViewManager.leftTab.showView(solver2View);
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
        return solver2View;
    }

    @Override
    public void viewIsHiding() {
        solver2View = null;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return solver2View.getContextMenu();
    }
}
