package de.droidcachebox.menu.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn4.executes.Solver2;

public class ShowSolver2 extends AbstractShowAction {

    private Solver2 solver2;

    public ShowSolver2() {
        super("Solver v2");
    }

    @Override
    public void execute() {
        solver2 = new Solver2();
        ViewManager.leftTab.showView(solver2);
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
        return solver2;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return solver2.getContextMenu();
    }
}
