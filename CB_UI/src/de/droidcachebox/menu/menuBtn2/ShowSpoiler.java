package de.droidcachebox.menu.menuBtn2;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.ex_import.DescriptionImageGrabber;
import de.droidcachebox.ex_import.ImporterProgress;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.views.SpoilerView;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ICancelRunnable;

import static de.droidcachebox.core.GroundspeakAPI.LastAPIError;
import static de.droidcachebox.core.GroundspeakAPI.OK;

public class ShowSpoiler extends AbstractShowAction {
    private static ShowSpoiler showSpoiler;
    private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);
    private Sprite SpoilerExistsIcon;
    private Sprite NoSpoilerIcon;
    private Menu contextMenu;
    private static CancelWaitDialog wd;

    private ShowSpoiler() {
        super("spoiler");
        // contextMenu = createContextMenu();
        SpoilerExistsIcon = Sprites.getSprite(IconName.imagesIcon.name());
        NoSpoilerIcon = new Sprite(Sprites.getSprite(IconName.imagesIcon.name()));
        NoSpoilerIcon.setColor(DISABLE_COLOR);

    }

    public static ShowSpoiler getInstance() {
        if (showSpoiler == null) showSpoiler = new ShowSpoiler();
        return showSpoiler;
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
            ImportSpoiler(false).setReadyListener(() -> {
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
            ImportSpoiler(true).setReadyListener(() -> {
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


    public CancelWaitDialog ImportSpoiler(boolean withLogImages) {
        wd = CancelWaitDialog.ShowWait(Translation.get("downloadSpoiler"), DownloadAnimation.GetINSTANCE(), () -> {
            // canceled
        }, new ICancelRunnable() {
            @Override
            public void run() {
                // Importer importer = new Importer();
                ImporterProgress ip = new ImporterProgress();
                int result = GroundspeakAPI.ERROR;
                if (GlobalCore.getSelectedCache() != null)
                    result = DescriptionImageGrabber.GrabImagesSelectedByCache(ip, true, false, GlobalCore.getSelectedCache().generatedId, GlobalCore.getSelectedCache().getGeoCacheCode(), "", "", withLogImages);
                wd.close();
                if (result != OK) {
                    GL.that.Toast(LastAPIError);
                }
            }

            @Override
            public boolean doCancel() {
                return false;
            }
        });
        return wd;
    }


}
