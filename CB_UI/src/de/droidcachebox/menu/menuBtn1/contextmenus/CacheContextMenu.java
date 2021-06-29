package de.droidcachebox.menu.menuBtn1.contextmenus;

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
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.views.GeoCacheListListView;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.menu.menuBtn1.ShowTrackableList;
import de.droidcachebox.menu.menuBtn2.*;
import de.droidcachebox.menu.menuBtn4.ShowDrafts;
import de.droidcachebox.menu.menuBtn4.ShowSolver1;
import de.droidcachebox.menu.menuBtn4.ShowSolver2;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;

import static de.droidcachebox.PlatformUIBase.callUrl;
import static de.droidcachebox.core.GroundspeakAPI.*;
import static de.droidcachebox.gdx.controls.messagebox.MessageBox.BTN_LEFT_POSITIVE;

public class CacheContextMenu {
    private static final String sKlasse = "CacheContextMenu";
    private static CacheContextMenu instance;
    private static CancelWaitDialog wd;
    private boolean forCacheList;
    private Cache geoCache;
    private Menu theMenu;

    public static CacheContextMenu getInstance() {
        if (instance == null) {
            instance = new CacheContextMenu();
            instance.geoCache = null;
        }
        return instance;
    }

    public Menu getCacheContextMenu(boolean _forCacheList) {
        //if (theMenu == null || theMenu.isDisposed() || forCacheList != _forCacheList || geoCache != GlobalCore.getSelectedCache()) {
        forCacheList = _forCacheList;

        theMenu = new Menu("DescriptionViewTitle");
        geoCache = GlobalCore.getSelectedCache();
        boolean selectedCacheIsSet = GlobalCore.isSetSelectedCache();
        boolean selectedCacheIsGC = false;
        if (selectedCacheIsSet) {
            selectedCacheIsGC = geoCache.getGeoCacheCode().startsWith("GC");
        }
        if (forCacheList) {
            theMenu.addCheckableMenuItem("CacheContextMenuShortClickToggle", Config.CacheContextMenuShortClickToggle.getValue(), this::toggleShortClick);
            if (selectedCacheIsSet)
                theMenu.addMoreMenu(ShowDrafts.getInstance().getContextMenu(), Translation.get("DraftsContextMenuTitle"), Translation.get("DraftsContextMenuTitle"));
        }
        if (selectedCacheIsSet) {
            if (selectedCacheIsGC)
                theMenu.addMenuItem("ReloadCacheAPI", Sprites.getSprite(IconName.dayGcLiveIcon.name()), this::reloadSelectedCache);
            theMenu.addMenuItem("Open_Cache_Link", Sprites.getSprite("big" + geoCache.getGeoCacheType().name()), () -> callUrl(geoCache.getUrl()));
            theMenu.addCheckableMenuItem("Favorite", Sprites.getSprite(IconName.favorit.name()), geoCache.isFavorite(), this::toggleAsFavorite);
            theMenu.addMenuItem("MI_EDIT_CACHE", Sprites.getSprite(IconName.noteIcon.name()), () -> new EditCache().update(geoCache));
            if (selectedCacheIsGC) {
                theMenu.addMenuItem("contactOwner", ContactOwner.getInstance().getIcon(), () -> ContactOwner.getInstance().execute());
                theMenu.addMenuItem("GroundSpeakLists", null, () -> ListsAtGroundSpeak.getInstance().execute());
            }
            if (!Config.rememberedGeoCache.getValue().equals(geoCache.getGeoCacheCode()))
                theMenu.addCheckableMenuItem("rememberGeoCache", Config.rememberedGeoCache.getValue().equals(geoCache.getGeoCacheCode()), this::rememberGeoCache);
            theMenu.addMenuItem("MI_DELETE_CACHE", Sprites.getSprite(IconName.DELETE.name()), this::deleteGeoCache);
        }
        if (forCacheList) {
            theMenu.addDivider();
            // cacheContextMenu.addMenuItem("Map", Sprites.getSprite(IconName.map.name()), () -> ShowMap.getInstance().execute());
            // cacheContextMenu.addMenuItem("Description", Sprites.getSprite(IconName.docIcon.name()), () -> ShowDescription.getInstance().execute());
            theMenu.addMenuItem("Waypoints", Sprites.getSprite("big" + GeoCacheType.Trailhead.name()), () -> ShowWaypoint.getInstance().execute());
            theMenu.addMenuItem("hint", Sprites.getSprite(IconName.hintIcon.name()), () -> HintDialog.getInstance().showHint()).setEnabled(geoCache.hasHint());
            theMenu.addMenuItem("spoiler", Sprites.getSprite(IconName.imagesIcon.name()), () -> ShowSpoiler.getInstance().execute());
            theMenu.addMenuItem("ShowLogs", Sprites.getSprite(IconName.listIcon.name()), () -> ShowLogs.getInstance().execute());
            theMenu.addMenuItem("Notes", Sprites.getSprite(IconName.userdata.name()), () -> ShowNotes.getInstance().execute());
            theMenu.addMenuItem("TBList", Sprites.getSprite(IconName.tbListIcon.name()), () -> ShowTrackableList.getInstance().execute());
            theMenu.addMenuItem("Solver", Sprites.getSprite(IconName.solverIcon.name()), () -> ShowSolver1.getInstance().execute());
            theMenu.addMenuItem("Solver v2", Sprites.getSprite("solver-icon-2"), () -> ShowSolver2.getInstance().execute());
            theMenu.addMenuItem("descExt", Sprites.getSprite(IconName.docIcon.name()), () -> StartExternalDescription.getInstance().execute());
        } else {
            theMenu.addDivider();
            theMenu.addMenuItem("TBList", Sprites.getSprite(IconName.tbListIcon.name()), () -> ShowTrackableList.getInstance().execute());
            theMenu.addMenuItem("Solver", Sprites.getSprite(IconName.solverIcon.name()), () -> ShowSolver1.getInstance().execute()).setEnabled(selectedCacheIsGC);
            theMenu.addMenuItem("Solver v2", Sprites.getSprite("solver-icon-2"), () -> ShowSolver2.getInstance().execute());
        }
        //}
        return theMenu;
    }

    private void rememberGeoCache() {
        if (GlobalCore.isSetSelectedCache()) {
            MessageBox mb = MessageBox.show(Translation.get("rememberThisOrSelectRememberedGeoCache"), Translation.get("rememberGeoCacheTitle"), MessageBoxButton.AbortRetryIgnore, MessageBoxIcon.Question, null);
            mb.setPositiveClickListener((v, x, y, pointer, button) -> {
                Config.rememberedGeoCache.setValue(GlobalCore.getSelectedCache().getGeoCacheCode());
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

    public void reloadSelectedCache() {
        if (GlobalCore.isSetSelectedCache()) {

            wd = CancelWaitDialog.ShowWait(Translation.get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), () -> {
            }, new ICancelRunnable() {

                @Override
                public void run() {
                    String GCCode = GlobalCore.getSelectedCache().getGeoCacheCode();
                    ArrayList<GeoCacheRelated> geoCacheRelateds = updateGeoCache(GlobalCore.getSelectedCache());
                    if (geoCacheRelateds.size() > 0) {
                        try {
                            WriteIntoDB.writeCachesAndLogsAndImagesIntoDB(geoCacheRelateds, null);
                        } catch (InterruptedException ex) {
                            Log.err(sKlasse, "WriteIntoDB.writeCachesAndLogsAndImagesIntoDB", ex);
                        }

                        // Reload result from DB
                        synchronized (Database.Data.cacheList) {
                            String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
                            Database.Data.cacheList = CacheListDAO.getInstance().readCacheList(sqlWhere, false, false, Config.showAllWaypoints.getValue());
                            GlobalCore.setSelectedCache(Database.Data.cacheList.getCacheByGcCodeFromCacheList(GCCode));
                            CacheListChangedListeners.getInstance().cacheListChanged();
                        }

                        ShowSpoiler.getInstance().ImportSpoiler(false).setReadyListener(() -> {
                            // do after import
                            if (GlobalCore.isSetSelectedCache()) {
                                GlobalCore.getSelectedCache().loadSpoilerRessources();
                            }
                        });

                        GL.that.RunOnGL(() -> {
                            ShowDescription.getInstance().updateDescriptionView(true);
                            GL.that.renderOnce();
                        });

                    } else {
                        if (GroundspeakAPI.APIError != OK) {
                            GL.that.RunOnGL(() -> MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("ReloadCacheAPI"), MessageBoxButton.OK, MessageBoxIcon.Information, null));
                        }
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

    private void deleteSelectedCache() {
        ArrayList<String> GcCodeList = new ArrayList<>();
        Coordinate referenceCoordinate = GlobalCore.getSelectedCache().getCoordinate();
        GcCodeList.add(GlobalCore.getSelectedCache().getGeoCacheCode());
        CacheListDAO.getInstance().delCacheImages(GcCodeList, CB_Core_Settings.SpoilerFolder.getValue(), CB_Core_Settings.SpoilerFolderLocal.getValue(), CB_Core_Settings.DescriptionImageFolder.getValue(), CB_Core_Settings.DescriptionImageFolderLocal.getValue());

        for (int i = 0, n = GlobalCore.getSelectedCache().getWayPoints().size(); i < n; i++) {
            Waypoint wp = GlobalCore.getSelectedCache().getWayPoints().get(i);
            Database.deleteFromDatabase(wp);
        }

        Database.Data.sql.delete("Caches", "GcCode='" + GlobalCore.getSelectedCache().getGeoCacheCode() + "'", null);

        LogDAO logdao = new LogDAO();
        //logdao.ClearOrphanedLogs(); // doit when you have more time
        logdao.deleteLogs(GlobalCore.getSelectedCache().generatedId);
        EditFilterSettings.applyFilter(FilterInstances.getLastFilter());

        GlobalCore.setSelectedCache(null);
        CacheListChangedListeners.getInstance().cacheListChanged();
        GeoCacheListListView.getInstance().setSelectedCacheVisible();

    }

    private void toggleShortClick() {
        MessageBox.show(Translation.get("CacheContextMenuShortClickToggleQuestion"), Translation.get("CacheContextMenuShortClickToggleTitle"), MessageBoxButton.YesNo, MessageBoxIcon.Question,
                (btnNumber, data) -> {
                    if (btnNumber == BTN_LEFT_POSITIVE)
                        Config.CacheContextMenuShortClickToggle.setValue(false);
                    else
                        Config.CacheContextMenuShortClickToggle.setValue(true);
                    Config.AcceptChanges();
                    return true;
                });
    }

    private void toggleAsFavorite() {
        GlobalCore.getSelectedCache().setFavorite(!GlobalCore.getSelectedCache().isFavorite());
        CacheDAO dao = new CacheDAO();
        dao.UpdateDatabase(GlobalCore.getSelectedCache());
        // Update cacheList
        Database.Data.cacheList.getCacheByIdFromCacheList(GlobalCore.getSelectedCache().generatedId).setFavorite(GlobalCore.getSelectedCache().isFavorite());
        // Update View
        ShowDescription.getInstance().updateDescriptionView(true);
        CacheListChangedListeners.getInstance().cacheListChanged();
    }

    private void deleteGeoCache() {
        MessageBox.show(Translation.get("sure"), Translation.get("question"), MessageBoxButton.OKCancel, MessageBoxIcon.Question,
                (which, data) -> {
                    if (which == MessageBox.BTN_LEFT_POSITIVE) {
                        deleteSelectedCache();
                        Log.debug(sKlasse, "deleteSelectedCache");
                        GlobalCore.setSelectedWaypoint(null, null, true);
                        Log.debug(sKlasse, "GlobalCore.setSelectedWaypoint");
                    }
                    return true;
                });
    }
}
