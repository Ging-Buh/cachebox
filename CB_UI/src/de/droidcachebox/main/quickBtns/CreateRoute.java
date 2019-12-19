package de.droidcachebox.main.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.main.AbstractAction;
import de.droidcachebox.main.menuBtn3.ShowMap;

import static de.droidcachebox.gdx.main.MenuID.AID_CreateRoute;

public class CreateRoute extends AbstractAction {
    private static CreateRoute createRoute;

    private CreateRoute() {
        super("generateRoute", AID_CreateRoute);
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
