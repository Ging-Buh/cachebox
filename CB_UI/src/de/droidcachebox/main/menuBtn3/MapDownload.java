package de.droidcachebox.main.menuBtn3;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.activities.FZKDownload;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.gdx.main.Menu;

import static de.droidcachebox.gdx.main.MenuID.AID_MAP_DOWNOAD;

public class MapDownload extends AbstractAction {
    private static MapDownload that;

    private MapDownload() {
        super("MapDownload", AID_MAP_DOWNOAD);
    }

    public static MapDownload getInstance() {
        if (that == null) that = new MapDownload();
        return that;
    }

    @Override
    public void execute() {
        Menu downloadMenu = new Menu("MapDownload");
        downloadMenu.addMenuItem("LoadMapFreizeitkarteMenuTitle", Sprites.getSprite(Sprites.IconName.freizeit.name()), FZKDownload.getInstance()::show);
        downloadMenu.addMenuItem("LoadMapFromOpenAndroMapsMenuTitle", Sprites.getSprite(Sprites.IconName.mapsforge_logo.name()),
                () -> callMapUrl("https://www.openandromaps.org/downloads/deutschland"));
        downloadMenu.addMenuItem("LoadMapFromFreizeitkarteMenuTitle", Sprites.getSprite(Sprites.IconName.freizeit.name()),
                () -> callMapUrl("https://www.freizeitkarte-osm.de/android/de/mitteleuropa.html"));
        downloadMenu.show();
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(Sprites.IconName.download.name());
    }

    public void callMapUrl(String url) {
        try {
            PlatformUIBase.callUrl(url);
        } catch (Exception ignored) {
        }
    }

}
