package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;

import static de.droidcachebox.PlatformUIBase.callUrl;

public class Action_Help extends AbstractAction {

    private static Action_Help that;

    private Action_Help() {
        super("Help Online", MenuID.AID_HELP);
    }

    public static Action_Help getInstance() {
        if (that == null) that = new Action_Help();
        return that;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.helpIcon.name());
    }

    @Override
    public void Execute() {
        callUrl("http://www.team-cachebox.de/index.php/de/kurzanleitung");
    }

}
