package de.droidcachebox.menu.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractAction;
import de.droidcachebox.menu.menuBtn3.ShowMap;

public class CreateRoute extends AbstractAction {
    private static CreateRoute createRoute;

    private CreateRoute() {
        super("generateRoute");
    }

    public static CreateRoute getInstance() {
        if (createRoute == null) createRoute = new CreateRoute();
        return createRoute;
    }

    @Override
    public boolean getEnabled() {
        return ShowMap.getInstance().openRouter();
    }

    @Override
    public void execute() {
        if (ShowMap.getInstance().openRouter())
            ShowMap.getInstance().setRoutingTrack();
    }

    @Override
    public Sprite getIcon() {
        return ShowMap.getInstance().getRouterIcon();
    }
}
