package de.droidcachebox.menu.menuBtn2;

import static de.droidcachebox.core.GroundspeakAPI.LastAPIError;
import static de.droidcachebox.core.GroundspeakAPI.OK;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
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
import de.droidcachebox.menu.menuBtn2.executes.SpoilerView;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ReadyListener;

public class ShowSpoiler extends AbstractShowAction {
    private final Sprite SpoilerExistsIcon;
    private final Sprite NoSpoilerIcon;
    private SpoilerView spoilerView;

    public ShowSpoiler() {
        super("spoiler");
        SpoilerExistsIcon = Sprites.getSprite(IconName.imagesIcon.name());
        NoSpoilerIcon = new Sprite(Sprites.getSprite(IconName.imagesIcon.name()));
        NoSpoilerIcon.setColor(new Color(0.2f, 0.2f, 0.2f, 0.2f));
    }

    @Override
    public void execute() {
        if (spoilerView == null)
            spoilerView = new SpoilerView();
        ViewManager.leftTab.showView(spoilerView);
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
        return spoilerView;
    }

    @Override
    public void viewIsHiding() {
        spoilerView = null;
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
        return createContextMenu();
    }

    private Menu createContextMenu() {
        if (spoilerView == null)
            spoilerView = new SpoilerView();
        Menu contextMenu = new Menu("SpoilerViewContextMenuTitle");

        contextMenu.addMenuItem("reloadSpoiler", null,
                () -> importSpoiler(false, isCanceled -> {
                    // do after import
                    if (!isCanceled) {
                        if (GlobalCore.isSetSelectedCache()) {
                            GlobalCore.getSelectedCache().loadSpoilerRessources();
                            spoilerView.forceReload();
                            ViewManager.leftTab.showView(spoilerView);
                            spoilerView.onShow();
                        }
                    }
                }));

        contextMenu.addMenuItem("LoadLogImages", Sprites.getSprite(IconName.downloadLogImages.name()), () -> importSpoiler(true, isCanceled -> {
            // do after import
            if (!isCanceled) {
                if (GlobalCore.isSetSelectedCache()) {
                    GlobalCore.getSelectedCache().loadSpoilerRessources();
                    spoilerView.forceReload();
                    ViewManager.leftTab.showView(spoilerView);
                    spoilerView.onShow();
                }
            }
        }));

        contextMenu.addMenuItem("startPictureApp", Sprites.getSprite("image-export"), () -> {
            String file = spoilerView.getSelectedFilePath();
            if (file != null) Platform.startPictureApp(file);
        });

        return contextMenu;
    }

    public void importSpoiler(boolean withLogImages, ReadyListener readyListener) {
        AtomicBoolean isCanceled = new AtomicBoolean(false);
        new CancelWaitDialog(Translation.get("downloadSpoiler"), new DownloadAnimation(),
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
                        if (result != OK) {
                            GL.that.toast(LastAPIError);
                        }
                    }

                    @Override
                    public void setIsCanceled() {
                        isCanceled.set(true);
                    }

                }).show();
    }

    public void forceReloadSpoiler() {
        if (spoilerView != null)
            spoilerView.forceReload();
    }


}
