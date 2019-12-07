package de.droidcachebox.main.menuBtn1.contextmenus;

import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.*;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.EditCache;
import de.droidcachebox.gdx.activities.EditFilterSettings;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.main.menuBtn1.ShowTrackableList;
import de.droidcachebox.main.menuBtn2.*;
import de.droidcachebox.main.menuBtn3.ShowMap;
import de.droidcachebox.main.menuBtn4.ShowDrafts;
import de.droidcachebox.main.menuBtn4.ShowSolver1;
import de.droidcachebox.main.menuBtn4.ShowSolver2;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;

import static de.droidcachebox.core.GroundspeakAPI.GeoCacheRelated;
import static de.droidcachebox.core.GroundspeakAPI.updateGeoCache;
import static de.droidcachebox.gdx.controls.messagebox.MessageBox.BTN_LEFT_POSITIVE;

public class CacheContextMenu {
    private static final String ClassName = "CacheContextMenu";
    private static CancelWaitDialog wd;

    public static Menu getCacheContextMenu(boolean forCacheList) {

        Menu cacheContextMenu = new Menu("DescriptionViewTitle");

        boolean selectedCacheIsSet = GlobalCore.isSetSelectedCache();
        boolean selectedCacheIsGC = false;
        if (selectedCacheIsSet) {
            selectedCacheIsGC = GlobalCore.getSelectedCache().getGcCode().startsWith("GC");
        }
        if (forCacheList) {
            cacheContextMenu.addCheckableMenuItem("CacheContextMenuShortClickToggle", Config.CacheContextMenuShortClickToggle.getValue(), CacheContextMenu::toggleShortClick);
            cacheContextMenu.addMoreMenu(ShowDrafts.getInstance().getContextMenu(), Translation.get("DraftsContextMenuTitle"), Translation.get("DraftsContextMenuTitle"));
        }
        cacheContextMenu.addMenuItem("ReloadCacheAPI", Sprites.getSprite(IconName.dayGcLiveIcon.name()), CacheContextMenu::reloadSelectedCache).setEnabled(selectedCacheIsGC);
        cacheContextMenu.addCheckableMenuItem("Favorite", Sprites.getSprite(IconName.favorit.name()), selectedCacheIsSet && GlobalCore.getSelectedCache().isFavorite(), CacheContextMenu::toggleAsFavorite).setEnabled(selectedCacheIsSet);
        cacheContextMenu.addMenuItem("Watchlist", null, CacheContextMenu::watchList).setEnabled(selectedCacheIsGC);
        cacheContextMenu.addMenuItem("MI_EDIT_CACHE", Sprites.getSprite(IconName.noteIcon.name()), () -> new EditCache().update(GlobalCore.getSelectedCache())).setEnabled(selectedCacheIsSet);
        cacheContextMenu.addMenuItem("MI_DELETE_CACHE", Sprites.getSprite(IconName.DELETE.name()), CacheContextMenu::deleteGeoCache).setEnabled(selectedCacheIsSet);
        cacheContextMenu.addCheckableMenuItem("rememberGeoCache", Config.rememberedGeoCache.getValue().equals(GlobalCore.getSelectedCache().getGcCode()), CacheContextMenu::rememberGeoCache).setEnabled(selectedCacheIsSet);
        if (forCacheList) {
            cacheContextMenu.addDivider();
            cacheContextMenu.addMenuItem("Map", Sprites.getSprite(IconName.map.name()), () -> ShowMap.getInstance().execute());
            cacheContextMenu.addMenuItem("Description", Sprites.getSprite(IconName.docIcon.name()), () -> ShowDescription.getInstance().execute());
            cacheContextMenu.addMenuItem("Waypoints", Sprites.getSprite("big" + CacheTypes.Trailhead.name()), () -> ShowWaypoint.getInstance().execute());
            cacheContextMenu.addMenuItem("hint", Sprites.getSprite(IconName.hintIcon.name()), () -> HintDialog.getInstance().showHint()).setEnabled(GlobalCore.getSelectedCache().hasHint());
            cacheContextMenu.addMenuItem("spoiler", Sprites.getSprite(IconName.imagesIcon.name()), () -> ShowSpoiler.getInstance().execute());
            cacheContextMenu.addMenuItem("ShowLogs", Sprites.getSprite(IconName.listIcon.name()), () -> ShowLogs.getInstance().execute());
            cacheContextMenu.addMenuItem("Notes", Sprites.getSprite(IconName.userdata.name()), () -> ShowNotes.getInstance().execute());
            cacheContextMenu.addMenuItem("TBList", Sprites.getSprite(IconName.tbListIcon.name()), () -> ShowTrackableList.getInstance().execute());
            cacheContextMenu.addMenuItem("Solver", Sprites.getSprite(IconName.solverIcon.name()), () -> ShowSolver1.getInstance().execute());
            cacheContextMenu.addMenuItem("Solver v2", Sprites.getSprite("solver-icon-2"), () -> ShowSolver2.getInstance().execute());
            cacheContextMenu.addMenuItem("descExt", Sprites.getSprite(IconName.docIcon.name()), () -> StartExternalDescription.getInstance().execute());
        } else {
            cacheContextMenu.addDivider();
            cacheContextMenu.addMenuItem("TBList", Sprites.getSprite(IconName.tbListIcon.name()), () -> ShowTrackableList.getInstance().execute());
            cacheContextMenu.addMenuItem("Solver", Sprites.getSprite(IconName.solverIcon.name()), () -> ShowSolver1.getInstance().execute()).setEnabled(selectedCacheIsGC);
            cacheContextMenu.addMenuItem("Solver v2", Sprites.getSprite("solver-icon-2"), () -> ShowSolver2.getInstance().execute());
        }

        return cacheContextMenu;
    }

    private static void rememberGeoCache() {
        if (GlobalCore.isSetSelectedCache()) {
            MessageBox mb = MessageBox.show(Translation.get("rememberThisOrSelectRememberedGeoCache"), Translation.get("rememberGeoCacheTitle"), MessageBoxButtons.AbortRetryIgnore, MessageBoxIcon.Question, null);
            mb.setPositiveClickListener((v, x, y, pointer, button) -> {
                Config.rememberedGeoCache.setValue(GlobalCore.getSelectedCache().getGcCode());
                Config.AcceptChanges();
                return mb.finish();
            });
            mb.setMiddleNeutralClickListener((v, x, y, pointer, button) -> {
                Cache rememberedCache = Database.Data.cacheList.getCacheByGcCodeFromCacheList(CB_Core_Settings.rememberedGeoCache.getValue());
                if (rememberedCache != null) GlobalCore.setSelectedCache(rememberedCache);
                return mb.finish();
            });
            mb.setRightNegativeClickListener((v, x, y, pointer, button) -> {
                Config.rememberedGeoCache.setValue("");
                Config.AcceptChanges();
                return mb.finish();
            });
            mb.setButtonText("rememberGeoCache", "selectGeoCache", "forgetGeoCache");
        }
    }

    public static void reloadSelectedCache() {
        if (GlobalCore.isSetSelectedCache()) {

            wd = CancelWaitDialog.ShowWait(Translation.get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), () -> {
            }, new ICancelRunnable() {

                @Override
                public void run() {
                    String GCCode = GlobalCore.getSelectedCache().getGcCode();
                    ArrayList<GeoCacheRelated> geoCacheRelateds = updateGeoCache(GlobalCore.getSelectedCache());
                    if (geoCacheRelateds.size() > 0) {
                        try {
                            WriteIntoDB.CachesAndLogsAndImagesIntoDB(geoCacheRelateds, null);
                        } catch (InterruptedException ex) {
                            Log.err(ClassName, "WriteIntoDB.CachesAndLogsAndImagesIntoDB", ex);
                        }

                        // Reload result from DB
                        synchronized (Database.Data.cacheList) {
                            String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
                            Database.Data.cacheList = CacheListDAO.getInstance().readCacheList(sqlWhere, false, false, Config.showAllWaypoints.getValue());
                            CacheListChangedListeners.getInstance().cacheListChanged();
                            GlobalCore.setSelectedCache(Database.Data.cacheList.getCacheByGcCodeFromCacheList(GCCode));
                        }
                        GL.that.RunOnGL(() -> {
                            ShowDescription.getInstance().updateDescriptionView(true);
                            GL.that.renderOnce();
                        });
                    }
                    wd.close();
                }

                @Override
                public boolean doCancel() {
                    return false;
                }
            });
        } else {
            MessageBox.show(Translation.get("NoCacheSelect"), Translation.get("Error"), MessageBoxIcon.Error);
        }
    }

    private static void deleteSelectedCache() {
        ArrayList<String> GcCodeList = new ArrayList<>();
        GcCodeList.add(GlobalCore.getSelectedCache().getGcCode());
        CacheListDAO.getInstance().delCacheImages(GcCodeList, CB_Core_Settings.SpoilerFolder.getValue(), CB_Core_Settings.SpoilerFolderLocal.getValue(), CB_Core_Settings.DescriptionImageFolder.getValue(), CB_Core_Settings.DescriptionImageFolderLocal.getValue());

        for (int i = 0, n = GlobalCore.getSelectedCache().waypoints.size(); i < n; i++) {
            Waypoint wp = GlobalCore.getSelectedCache().waypoints.get(i);
            Database.deleteFromDatabase(wp);
        }

        Database.Data.sql.delete("Caches", "GcCode='" + GlobalCore.getSelectedCache().getGcCode() + "'", null);

        LogDAO logdao = new LogDAO();
        //logdao.ClearOrphanedLogs(); // doit when you have more time
        logdao.deleteLogs(GlobalCore.getSelectedCache().Id);
        EditFilterSettings.applyFilter(FilterInstances.getLastFilter());

        GlobalCore.setSelectedCache(null);

        CacheListChangedListeners.getInstance().cacheListChanged();
    }

    private static void toggleShortClick() {
        MessageBox.show(Translation.get("CacheContextMenuShortClickToggleQuestion"), Translation.get("CacheContextMenuShortClickToggleTitle"), MessageBoxButtons.YesNo, MessageBoxIcon.Question,
                (btnNumber, data) -> {
                    if (btnNumber == BTN_LEFT_POSITIVE)
                        Config.CacheContextMenuShortClickToggle.setValue(false);
                    else
                        Config.CacheContextMenuShortClickToggle.setValue(true);
                    Config.AcceptChanges();
                    return true;
                });
    }

    private static void toggleAsFavorite() {
        GlobalCore.getSelectedCache().setFavorite(!GlobalCore.getSelectedCache().isFavorite());
        CacheDAO dao = new CacheDAO();
        dao.UpdateDatabase(GlobalCore.getSelectedCache());
        // Update cacheList
        Database.Data.cacheList.getCacheByIdFromCacheList(GlobalCore.getSelectedCache().Id).setFavorite(GlobalCore.getSelectedCache().isFavorite());
        // Update View
        ShowDescription.getInstance().updateDescriptionView(true);
        CacheListChangedListeners.getInstance().cacheListChanged();
    }

    private static void watchList() {
        MessageBox mb = MessageBox.show(Translation.get("WatchlistMessage"), Translation.get("Watchlist"), MessageBoxButtons.AbortRetryIgnore, MessageBoxIcon.Question,
                (btnNumber, data) -> {
                    if (btnNumber == BTN_LEFT_POSITIVE)
                        addToWatchList();
                    else if (btnNumber == MessageBox.BTN_MIDDLE_NEUTRAL)
                        removeFromWatchList();
                    return true;
                });
        mb.setButtonText("append", "remove", "cancel");
    }

    private static void addToWatchList() {
        if (GlobalCore.isSetSelectedCache()) {
            GL.that.postAsync(() -> {
                if (GroundspeakAPI.AddToWatchList(GlobalCore.getSelectedCache().getGcCode()) == GroundspeakAPI.OK) {
                    MessageBox.show(Translation.get("ok"), Translation.get("AddToWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                } else {
                    MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("AddToWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                }
            });
        }
    }

    private static void removeFromWatchList() {
        if (GlobalCore.isSetSelectedCache()) {
            GL.that.postAsync(() -> {
                if (GroundspeakAPI.RemoveFromWatchList(GlobalCore.getSelectedCache().getGcCode()) == GroundspeakAPI.OK) {
                    MessageBox.show(Translation.get("ok"), Translation.get("RemoveFromWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                } else {
                    MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("RemoveFromWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                }
            });
        }
    }

    private static void deleteGeoCache() {
        MessageBox.show(Translation.get("sure"), Translation.get("question"), MessageBoxButtons.OKCancel, MessageBoxIcon.Question,
                (which, data) -> {
                    if (which == MessageBox.BTN_LEFT_POSITIVE) {
                        deleteSelectedCache();
                        GlobalCore.setSelectedWaypoint(null, null, true);
                    }
                    return true;
                });
    }
}
