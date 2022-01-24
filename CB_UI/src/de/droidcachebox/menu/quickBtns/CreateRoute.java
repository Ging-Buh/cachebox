package de.droidcachebox.menu.quickBtns;

import static de.droidcachebox.menu.Action.ShowMap;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.settings.Settings;

public class CreateRoute extends AbstractAction {

    public CreateRoute() {
        super("generateRoute");
    }

    @Override
    public boolean getEnabled() {
        return ((ShowMap) ShowMap.action).openRouter();
    }

    @Override
    public void execute() {
        if (((ShowMap) ShowMap.action).openRouter())
            ((ShowMap) ShowMap.action).setRoutingTrack();
    }

    @Override
    public Sprite getIcon() {
        switch (Settings.routeProfile.getValue()) {
            case 0:
                return Sprites.getSprite("pedestrian");
            case 1:
                return Sprites.getSprite("bicycle");
            default:
                return Sprites.getSprite("car");
        }
    }

}
