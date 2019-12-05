package de.droidcachebox.main.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.gdx.main.MenuID;

import static de.droidcachebox.PlatformUIBase.callUrl;

public class HelpOnline extends AbstractAction {

    private static HelpOnline that;

    private HelpOnline() {
        super("Help Online", MenuID.AID_HELP);
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
        callUrl("http://www.team-cachebox.de/index.php/de/kurzanleitung");
    }

}
