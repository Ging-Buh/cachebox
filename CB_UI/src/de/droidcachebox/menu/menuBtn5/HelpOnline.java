package de.droidcachebox.menu.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;

import static de.droidcachebox.PlatformUIBase.callUrl;

public class HelpOnline extends AbstractAction {

    private static HelpOnline that;

    private HelpOnline() {
        super("Help Online");
    }

    public static HelpOnline getInstance() {
        if (that == null) that = new HelpOnline();
        return that;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.helpIcon.name());
    }

    @Override
    public void execute() {
        callUrl("https://github.com/Ging-Buh/cachebox/wiki/FirstStart#FirstStart");
        // http://www.team-cachebox.de/index.php/de/kurzanleitung
    }

}
