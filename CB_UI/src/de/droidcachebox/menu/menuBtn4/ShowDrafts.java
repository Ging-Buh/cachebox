package de.droidcachebox.menu.menuBtn4;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.LogType;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn4.executes.DraftsView;
import de.droidcachebox.translation.Translation;

public class ShowDrafts extends AbstractShowAction {

    private static ShowDrafts instance;
    private DraftsView draftsView;

    private ShowDrafts() {
        super("Drafts");
    }

    public static ShowDrafts getInstance() {
        if (instance == null) instance = new ShowDrafts();
        return instance;
    }

    @Override
    public void execute() {
        if (draftsView == null || draftsView.isDisposed()) {
            draftsView = new DraftsView();
        }
        ViewManager.leftTab.showView(draftsView);
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.fieldnoteListIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return draftsView;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        final Menu cm = new Menu("DraftsContextMenuTitle");
        Cache cache = GlobalCore.getSelectedCache();
        boolean iAmTheOwner = false;
        // depending on selection of cache
        if (cache != null) {
            iAmTheOwner = cache.iAmTheOwner();
            // depending on GeoCacheType
            if (cache.getGeoCacheType() != null) {
                if (cache.isEvent()) {
                    cm.addMenuItem("will-attended", Sprites.getSprite("log8icon"), () -> addNewDraft(LogType.will_attend));
                    cm.addMenuItem("attended", Sprites.getSprite("log9icon"), () -> addNewDraft(LogType.attended));
                } else if (cache.getGeoCacheType() == GeoCacheType.Camera) {
                    cm.addMenuItem("webCamFotoTaken", Sprites.getSprite("log10icon"), () -> addNewDraft(LogType.webcam_photo_taken));
                } else {
                    cm.addMenuItem("found", Sprites.getSprite("log0icon"), () -> addNewDraft(LogType.found));
                }
                cm.addMenuItem("DNF", Sprites.getSprite("log1icon"), () -> addNewDraft(LogType.didnt_find));
            }
            // Cache is from geocaching.com: more menu entries
            if (cache.getGeoCacheCode().toLowerCase().startsWith("gc")) {
                cm.addMenuItem("maintenance", Sprites.getSprite("log5icon"), () -> addNewDraft(LogType.needs_maintenance));
                cm.addMenuItem("writenote", Sprites.getSprite("log2icon"), () -> addNewDraft(LogType.note));
            }
        }

        cm.addDivider();

        // independent from cache-selection
        cm.addMenuItem("uploadDrafts", UploadDrafts.getInstance().getIcon(), () -> UploadDrafts.getInstance().execute());
        cm.addMenuItem("directLog", UploadLogs.getInstance().getIcon(), () -> UploadLogs.getInstance().execute());
        cm.addMenuItem("DeleteAllDrafts", Sprites.getSprite(IconName.DELETE.name()), this::deleteAllDrafts);

        // extensions for owner
        if (iAmTheOwner) {
            Menu ownerLogTypesTitleMenu = new Menu("OwnerLogTypesTitle");
            ownerLogTypesTitleMenu.addMenuItem("enabled", Sprites.getSprite("log4icon"), () -> addNewDraft(LogType.enabled));
            ownerLogTypesTitleMenu.addMenuItem("temporarilyDisabled", Sprites.getSprite("log6icon"), () -> addNewDraft(LogType.temporarily_disabled));
            ownerLogTypesTitleMenu.addMenuItem("ownerMaintenance", Sprites.getSprite("log7icon"), () -> addNewDraft(LogType.owner_maintenance));
            cm.addMoreMenu(ownerLogTypesTitleMenu, Translation.get("defaultLogTypes"), Translation.get("ownerLogTypes"));
        }

        return cm;
    }

    private void addNewDraft(LogType logType) {
        if (draftsView == null || draftsView.isDisposed()) {
            draftsView = new DraftsView();
        }
        draftsView.addNewDraft(logType, true);
    }

    private void deleteAllDrafts() {
        if (draftsView == null || draftsView.isDisposed()) {
            draftsView = new DraftsView();
        }
        draftsView.deleteAllDrafts();
    }

    /*
       for QuickDraft
     */
    public void addNewDraft(LogType logType, boolean andEdit) {
        if (draftsView == null || draftsView.isDisposed()) {
            draftsView = new DraftsView();
        }
        draftsView.addNewDraft(logType, andEdit); // andEdit will always be false, if called from QuickDraft
    }
}
