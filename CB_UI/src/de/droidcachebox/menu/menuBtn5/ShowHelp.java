package de.droidcachebox.menu.menuBtn5;

import static de.droidcachebox.Platform.callUrl;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;

public class ShowHelp extends AbstractAction {

    private static ShowHelp instance;

    private ShowHelp() {
        super("Help Online");
    }

    public static ShowHelp getInstance() {
        if (instance == null) instance = new ShowHelp();
        return instance;
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
