package de.droidcachebox.menu.menuBtn1.contextmenus;

import static de.droidcachebox.Platform.callUrl;
import static de.droidcachebox.core.GroundspeakAPI.GeoCacheRelated;
import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.updateGeoCache;
import static de.droidcachebox.menu.Action.ShowDescription;
import static de.droidcachebox.menu.Action.ShowDrafts;
import static de.droidcachebox.menu.Action.ShowLogs;
import static de.droidcachebox.menu.Action.ShowNotes;
import static de.droidcachebox.menu.Action.ShowSolver1;
import static de.droidcachebox.menu.Action.ShowSolver2;
import static de.droidcachebox.menu.Action.ShowSpoiler;
import static de.droidcachebox.menu.Action.ShowTrackableList;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.database.LogsTableDAO;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.EditCache;
import de.droidcachebox.gdx.activities.EditFilterSettings;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.Action;
import de.droidcachebox.menu.menuBtn1.ShowGeoCaches;
import de.droidcachebox.menu.menuBtn2.ShowDescription;
import de.droidcachebox.menu.menuBtn2.ShowSpoiler;
import de.droidcachebox.menu.menuBtn2.StartExternalDescription;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

public class CacheContextMenu {
    private static final String sClass = "CacheContextMenu";
    private static CacheContextMenu instance;
    private Cache geoCache;

    public static CacheContextMenu getInstance() {
        if (instance == null) {
            instance = new CacheContextMenu();
            instance.geoCache = null;
        }
        return instance;
    }

    public Menu getCacheContextMenu(boolean _forCacheList) {
        //if (theMenu == null || theMenu.isDisposed || forCacheList != _forCacheList || geoCache != GlobalCore.getSelectedCache()) {

        Menu theMenu = new Menu("DescriptionViewTitle");
        geoCache = GlobalCore.getSelectedCache();
        boolean selectedCacheIsSet = GlobalCore.isSetSelectedCache();
        boolean selectedCacheIsGC = false;
        if (selectedCacheIsSet) {
            selectedCacheIsGC = geoCache.getGeoCacheCode().startsWith("GC");
        }
        if (_forCacheList) {
            theMenu.addCheckableMenuItem("CacheContextMenuShortClickToggle", Settings.CacheContextMenuShortClickToggle.getValue(), this::toggleShortClick);
            if (selectedCacheIsSet)
                theMenu.addMoreMenu(ShowDrafts.action.getContextMenu(), Translation.get("DraftsContextMenuTitle"), Translation.get("DraftsContextMenuTitle"));
        }
        if (selectedCacheIsSet) {
            if (selectedCacheIsGC)
                theMenu.addMenuItem("ReloadCacheAPI", Sprites.getSprite(IconName.dayGcLiveIcon.name()), this::reloadSelectedCache);
            theMenu.addMenuItem("Open_Cache_Link", Sprites.getSprite("big" + geoCache.getGeoCacheType().name()), () -> callUrl(geoCache.getUrl()));
            theMenu.addCheckableMenuItem("Favorite", Sprites.getSprite(IconName.favorit.name()), geoCache.isFavorite(), this::toggleAsFavorite);
            theMenu.addMenuItem("MI_EDIT_CACHE", Sprites.getSprite(IconName.noteIcon.name()), () -> new EditCache().update(geoCache));
            if (selectedCacheIsGC) {
                theMenu.addMenuItem("contactOwner", Sprites.getSprite("bigLetterbox"), () -> new ContactOwner().execute());
                theMenu.addMenuItem("GroundSpeakLists", null, () -> new ListsAtGroundSpeak().execute());
            }
            if (!Settings.rememberedGeoCache.getValue().equals(geoCache.getGeoCacheCode()))
                theMenu.addCheckableMenuItem("rememberGeoCache", Settings.rememberedGeoCache.getValue().equals(geoCache.getGeoCacheCode()), this::rememberGeoCache);
            theMenu.addMenuItem("MI_DELETE_CACHE", Sprites.getSprite(IconName.DELETE.name()), this::deleteGeoCache);
        }
        if (_forCacheList) {
            theMenu.addDivider();
            theMenu.addMenuItem("Waypoints", Sprites.getSprite("big" + GeoCacheType.Trailhead.name()), () -> Action.ShowWayPoints.action.execute());
            theMenu.addMenuItem("hint", Sprites.getSprite(IconName.hintIcon.name()), () -> Action.ShowHint.action.execute()).setEnabled(geoCache.hasHint());
            theMenu.addMenuItem("spoiler", Sprites.getSprite(IconName.imagesIcon.name()), () -> ShowSpoiler.action.execute());
            theMenu.addMenuItem("ShowLogs", Sprites.getSprite(IconName.listIcon.name()), () -> ShowLogs.action.execute());
            theMenu.addMenuItem("Notes", Sprites.getSprite(IconName.userdata.name()), () -> ShowNotes.action.execute());
            theMenu.addMenuItem("TBList", Sprites.getSprite(IconName.tbListIcon.name()), () -> ShowTrackableList.action.execute());
            theMenu.addMenuItem("Solver", Sprites.getSprite(IconName.solverIcon.name()), () -> ShowSolver1.action.execute());
            theMenu.addMenuItem("Solver v2", Sprites.getSprite("solver-icon-2"), () -> ShowSolver2.action.execute());
            theMenu.addMenuItem("descExt", Sprites.getSprite(IconName.docIcon.name()), () -> StartExternalDescription.getInstance().execute());
        } else {
            theMenu.addDivider();
            theMenu.addMenuItem("TBList", Sprites.getSprite(IconName.tbListIcon.name()), () -> ShowTrackableList.action.execute());
            theMenu.addMenuItem("Solver", Sprites.getSprite(IconName.solverIcon.name()), () -> ShowSolver1.action.execute()).setEnabled(selectedCacheIsGC);
            theMenu.addMenuItem("Solver v2", Sprites.getSprite("solver-icon-2"), () -> ShowSolver2.action.execute());
        }
        //}
        return theMenu;
    }

    private void rememberGeoCache() {
        if (GlobalCore.isSetSelectedCache()) {
            ButtonDialog mb = new ButtonDialog(Translation.get("rememberThisOrSelectRememberedGeoCache"), Translation.get("rememberGeoCacheTitle"), MsgBoxButton.AbortRetryIgnore, MsgBoxIcon.Question);
            mb.setButtonClickHandler((btnNumber, data) -> {
                if (btnNumber == ButtonDialog.BTN_LEFT_POSITIVE) {
                    Settings.rememberedGeoCache.setValue(GlobalCore.getSelectedCache().getGeoCacheCode());
                    Settings.getInstance().acceptChanges();
                } else if (btnNumber == ButtonDialog.BTN_MIDDLE_NEUTRAL) {
                    Cache rememberedCache = CBDB.getInstance().cacheList.getCacheByGcCodeFromCacheList(Settings.rememberedGeoCache.getValue());
                    if (rememberedCache != null) GlobalCore.setSelectedCache(rememberedCache);
                } else {
                    Settings.rememberedGeoCache.setValue("");
                    Settings.getInstance().acceptChanges();
                }
                return true;
            });
            mb.setButtonText("rememberGeoCache", "selectGeoCache", "forgetGeoCache");
            mb.show();
        }
    }

    public void reloadSelectedCache() {
        if (GlobalCore.isSetSelectedCache()) {
            AtomicBoolean isCanceled = new AtomicBoolean(false);
            new CancelWaitDialog(Translation.get("ReloadCacheAPI"), new DownloadAnimation(), new RunAndReady() {
                @Override
                public void ready() {

                }

                @Override
                public void run() {
                    CachesDAO cachesDAO = new CachesDAO();

                    String GCCode = GlobalCore.getSelectedCache().getGeoCacheCode();
                    ArrayList<GeoCacheRelated> geoCacheRelateds = updateGeoCache(GlobalCore.getSelectedCache());
                    if (geoCacheRelateds.size() > 0) {
                        try {
                            cachesDAO.writeCachesAndLogsAndImagesIntoDB(geoCacheRelateds, null);
                        } catch (InterruptedException ex) {
                            Log.err(sClass, "WriteIntoDB.writeCachesAndLogsAndImagesIntoDB", ex);
                        }

                        // Reload result from DB
                        synchronized (CBDB.getInstance().cacheList) {
                            String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue());
                            cachesDAO.readCacheList(sqlWhere, false, false, Settings.showAllWaypoints.getValue());
                            GlobalCore.setSelectedCache(CBDB.getInstance().cacheList.getCacheByGcCodeFromCacheList(GCCode));
                            CacheListChangedListeners.getInstance().fire();
                        }

                        ((ShowSpoiler) ShowSpoiler.action).importSpoiler(false,
                                isCanceled -> {
                                    // do after import
                                    if (!isCanceled) {
                                        if (GlobalCore.isSetSelectedCache()) {
                                            GlobalCore.getSelectedCache().loadSpoilerRessources();
                                        }
                                    }
                                });

                        GL.that.runOnGL(() -> {
                            ((ShowDescription) ShowDescription.action).updateDescriptionView(true);
                            GL.that.renderOnce();
                        });

                    } else {
                        if (GroundspeakAPI.APIError != OK) {
                            new ButtonDialog(GroundspeakAPI.LastAPIError, Translation.get("ReloadCacheAPI"), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                        }
                    }
                }

                @Override
                public void setIsCanceled() {
                    isCanceled.set(true);
                }

            }).show();
        } else {
            new ButtonDialog(Translation.get("NoCacheSelect"), Translation.get("Error"), MsgBoxButton.OK, MsgBoxIcon.Error).show();
        }
    }

    private void deleteSelectedCache() {
        ArrayList<String> GcCodeList = new ArrayList<>();
        GcCodeList.add(GlobalCore.getSelectedCache().getGeoCacheCode());
        new CachesDAO().delCacheImages(GcCodeList, Settings.SpoilerFolder.getValue(), Settings.SpoilerFolderLocal.getValue(), Settings.DescriptionImageFolder.getValue(), Settings.DescriptionImageFolderLocal.getValue());

        for (int i = 0, n = GlobalCore.getSelectedCache().getWayPoints().size(); i < n; i++) {
            Waypoint wp = GlobalCore.getSelectedCache().getWayPoints().get(i);
            WaypointDAO.getInstance().deleteFromDatabase(wp);
        }

        CBDB.getInstance().delete("Caches", "GcCode='" + GlobalCore.getSelectedCache().getGeoCacheCode() + "'", null);

        // ClearOrphanedLogs(); // do it when you have more time
        LogsTableDAO.getInstance().deleteLogs(GlobalCore.getSelectedCache().generatedId);
        EditFilterSettings.applyFilter(FilterInstances.getLastFilter());

        GlobalCore.setSelectedCache(null);
        CacheListChangedListeners.getInstance().fire();
        ((ShowGeoCaches) Action.ShowGeoCaches.action).getGeoCachesView().setSelectedCacheVisible();

    }

    private void toggleShortClick() {
        ButtonDialog bd = new ButtonDialog(Translation.get("CacheContextMenuShortClickToggleQuestion"), Translation.get("CacheContextMenuShortClickToggleTitle"), MsgBoxButton.YesNo, MsgBoxIcon.Question);
        bd.setButtonClickHandler((btnNumber, data) -> {
            if (btnNumber == ButtonDialog.BTN_LEFT_POSITIVE)
                Settings.CacheContextMenuShortClickToggle.setValue(false);
            else
                Settings.CacheContextMenuShortClickToggle.setValue(true);
            Settings.getInstance().acceptChanges();
            return true;
        });
        bd.show();
    }

    private void toggleAsFavorite() {
        GlobalCore.getSelectedCache().setFavorite(!GlobalCore.getSelectedCache().isFavorite());
        CachesDAO cachesDAO = new CachesDAO();
        cachesDAO.updateDatabase(GlobalCore.getSelectedCache());
        // Update cacheList
        CBDB.getInstance().cacheList.getCacheByIdFromCacheList(GlobalCore.getSelectedCache().generatedId).setFavorite(GlobalCore.getSelectedCache().isFavorite());
        // Update View
        ((ShowDescription) ShowDescription.action).updateDescriptionView(true);
        CacheListChangedListeners.getInstance().fire();
    }

    private void deleteGeoCache() {
        ButtonDialog bd = new ButtonDialog(Translation.get("sure"), Translation.get("question"), MsgBoxButton.OKCancel, MsgBoxIcon.Question);
        bd.setButtonClickHandler((which, data) -> {
            if (which == ButtonDialog.BTN_LEFT_POSITIVE) {
                deleteSelectedCache();
                Log.debug(sClass, "deleteSelectedCache");
                GlobalCore.setSelectedWaypoint(null, null, true);
                Log.debug(sClass, "GlobalCore.setSelectedWaypoint");
            }
            return true;
        });
        bd.show();
    }
}
