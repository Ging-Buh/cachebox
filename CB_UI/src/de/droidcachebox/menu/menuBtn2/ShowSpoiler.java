package de.droidcachebox.menu.menuBtn2;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.views.SpoilerView;
import de.droidcachebox.menu.ViewManager;

public class ShowSpoiler extends AbstractShowAction {
    private static ShowSpoiler that;
    private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);
    private Sprite SpoilerExistsIcon;
    private Sprite NoSpoilerIcon;
    private Menu contextMenu;

    private ShowSpoiler() {
        super("spoiler");
        // contextMenu = createContextMenu();
        SpoilerExistsIcon = Sprites.getSprite(IconName.imagesIcon.name());
        NoSpoilerIcon = new Sprite(Sprites.getSprite(IconName.imagesIcon.name()));
        NoSpoilerIcon.setColor(DISABLE_COLOR);

    }

    public static ShowSpoiler getInstance() {
        if (that == null) that = new ShowSpoiler();
        return that;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(SpoilerView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true; // GlobalCore.selectedCachehasSpoiler();
    }

    @Override
    public Sprite getIcon() {
        boolean hasSpoiler = GlobalCore.selectedCachehasSpoiler();
        if (hasSpoiler) {
            return SpoilerExistsIcon;
        } else {
            return NoSpoilerIcon;
        }
    }

    @Override
    public CB_View_Base getView() {
        return SpoilerView.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        // if depends on something: call createContextMenu() again
        // todo why are the clickhandlers of the items gone on following calls? temp solution createContextMenu() again
        // has to do with the disposing of the compoundMenu in CB_Button after the Show
        createContextMenu();
        return contextMenu;
    }

    private void createContextMenu() {
        contextMenu = new Menu("SpoilerViewContextMenuTitle");

        contextMenu.addMenuItem("reloadSpoiler", null, () -> {
            GlobalCore.ImportSpoiler(false).setReadyListener(() -> {
                // do after import
                if (GlobalCore.isSetSelectedCache()) {
                    GlobalCore.getSelectedCache().loadSpoilerRessources();
                    SpoilerView.getInstance().ForceReload();
                    ViewManager.leftTab.showView(SpoilerView.getInstance());
                    SpoilerView.getInstance().onShow();
                }
            });
        });

        contextMenu.addMenuItem("LoadLogImages", Sprites.getSprite(IconName.downloadLogImages.name()), () -> {
            GlobalCore.ImportSpoiler(true).setReadyListener(() -> {
                // do after import
                if (GlobalCore.isSetSelectedCache()) {
                    GlobalCore.getSelectedCache().loadSpoilerRessources();
                    SpoilerView.getInstance().ForceReload();
                    ViewManager.leftTab.showView(SpoilerView.getInstance());
                    SpoilerView.getInstance().onShow();
                }
            });
        });

        contextMenu.addMenuItem("startPictureApp", Sprites.getSprite("image-export"), () -> {
            String file = SpoilerView.getInstance().getSelectedFilePath();
            if (file != null) PlatformUIBase.startPictureApp(file);
        });
    }

}
