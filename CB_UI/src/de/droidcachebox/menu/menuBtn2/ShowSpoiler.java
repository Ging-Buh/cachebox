package de.droidcachebox.menu.menuBtn2;

import static de.droidcachebox.core.GroundspeakAPI.LastAPIError;
import static de.droidcachebox.core.GroundspeakAPI.OK;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.ex_import.DescriptionImageGrabber;
import de.droidcachebox.ex_import.ImportProgress;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn2.executes.Spoiler;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ReadyListener;

public class ShowSpoiler extends AbstractShowAction {
    private static ShowSpoiler showSpoiler;
    private static CancelWaitDialog wd;
    private final Sprite SpoilerExistsIcon;
    private final Sprite NoSpoilerIcon;
    private Menu contextMenu;

    private ShowSpoiler() {
        super("spoiler");
        // contextMenu = createContextMenu();
        SpoilerExistsIcon = Sprites.getSprite(IconName.imagesIcon.name());
        NoSpoilerIcon = new Sprite(Sprites.getSprite(IconName.imagesIcon.name()));
        Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);
        NoSpoilerIcon.setColor(DISABLE_COLOR);

    }

    public static ShowSpoiler getInstance() {
        if (showSpoiler == null) showSpoiler = new ShowSpoiler();
        return showSpoiler;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(Spoiler.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true; // GlobalCore.selectedCacheHasSpoiler();
    }

    @Override
    public Sprite getIcon() {
        boolean hasSpoiler = GlobalCore.selectedCacheHasSpoiler();
        if (hasSpoiler) {
            return SpoilerExistsIcon;
        } else {
            return NoSpoilerIcon;
        }
    }

    @Override
    public CB_View_Base getView() {
        return Spoiler.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        // if depends on something: call createContextMenu() again
        // todo why are the click_handlers of the items gone on following calls? temp solution createContextMenu() again
        // has to do with the disposing of the compoundMenu in CB_Button after the Show
        createContextMenu();
        return contextMenu;
    }

    private void createContextMenu() {
        contextMenu = new Menu("SpoilerViewContextMenuTitle");

        contextMenu.addMenuItem("reloadSpoiler", null,
                () -> importSpoiler(false, isCanceled -> {
                    // do after import
                    if (!isCanceled) {
                        if (GlobalCore.isSetSelectedCache()) {
                            GlobalCore.getSelectedCache().loadSpoilerRessources();
                            Spoiler.getInstance().ForceReload();
                            ViewManager.leftTab.showView(Spoiler.getInstance());
                            Spoiler.getInstance().onShow();
                        }
                    }
                }));

        contextMenu.addMenuItem("LoadLogImages", Sprites.getSprite(IconName.downloadLogImages.name()), () -> importSpoiler(true, isCanceled -> {
            // do after import
            if (!isCanceled) {
                if (GlobalCore.isSetSelectedCache()) {
                    GlobalCore.getSelectedCache().loadSpoilerRessources();
                    Spoiler.getInstance().ForceReload();
                    ViewManager.leftTab.showView(Spoiler.getInstance());
                    Spoiler.getInstance().onShow();
                }
            }
        }));

        contextMenu.addMenuItem("startPictureApp", Sprites.getSprite("image-export"), () -> {
            String file = Spoiler.getInstance().getSelectedFilePath();
            if (file != null) PlatformUIBase.startPictureApp(file);
        });
    }

    public void importSpoiler(boolean withLogImages, ReadyListener readyListener) {
        AtomicBoolean isCanceled = new AtomicBoolean(false);
        wd = new CancelWaitDialog(Translation.get("downloadSpoiler"), new DownloadAnimation(),
                new RunAndReady() {
                    @Override
                    public void ready() {
                        readyListener.isReady(isCanceled.get());
                    }

                    @Override
                    public void run() {
                        ImportProgress importProgress = new ImportProgress((message, progressMessage, progress) -> {
                            // todo show progress
                        });
                        int result = GroundspeakAPI.ERROR;
                        if (GlobalCore.getSelectedCache() != null)
                            result = DescriptionImageGrabber.grabImagesSelectedByCache(importProgress, true, false, GlobalCore.getSelectedCache().generatedId, GlobalCore.getSelectedCache().getGeoCacheCode(), "", "", withLogImages);
                        wd.close();
                        if (result != OK) {
                            GL.that.toast(LastAPIError);
                        }
                    }

                    @Override
                    public void setIsCanceled() {
                        isCanceled.set(true);
                    }

                });
        wd.show();
    }


}
