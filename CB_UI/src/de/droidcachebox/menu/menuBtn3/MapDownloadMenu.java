package de.droidcachebox.menu.menuBtn3;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.Platform;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.menuBtn3.executes.FZKDownload;

public class MapDownloadMenu extends AbstractAction {
    private static MapDownloadMenu mapDownloadMenu;

    private MapDownloadMenu() {
        super("MapDownload");
    }

    public static MapDownloadMenu getInstance() {
        if (mapDownloadMenu == null) mapDownloadMenu = new MapDownloadMenu();
        return mapDownloadMenu;
    }

    @Override
    public void execute() {
        Menu downloadMenu = new Menu("MapDownload");
        downloadMenu.addMenuItem("LoadMapFreizeitkarteMenuTitle", Sprites.getSprite(Sprites.IconName.freizeit.name()), this::callFZKDownload);
        downloadMenu.addMenuItem("LoadMapFromOpenAndroMapsMenuTitle", Sprites.getSprite(Sprites.IconName.mapsforge_logo.name()),
                () -> callMapUrl("https://www.openandromaps.org/downloads/deutschland"));
        downloadMenu.addMenuItem("LoadMapFromFreizeitkarteMenuTitle", Sprites.getSprite(Sprites.IconName.freizeit.name()),
                () -> callMapUrl("https://www.freizeitkarte-osm.de/android/de/mitteleuropa.html"));
        downloadMenu.addMenuItem("LoadMapFromMapsforgeMenuTitle", Sprites.getSprite(Sprites.IconName.mapsforge_logo.name()),
                () -> callMapUrl("https://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/v5/"));
        downloadMenu.show();
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(Sprites.IconName.download.name());
    }

    private void callFZKDownload() {
        FZKDownload instance = FZKDownload.getInstance();
        instance.show();
    }

    private void callMapUrl(String url) {
        try {
            Platform.callUrl(url);
        } catch (Exception ignored) {
        }
    }

}
