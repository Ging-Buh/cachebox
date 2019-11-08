package de.droidcachebox.gdx.main;

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
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog.IcancelListener;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.log.Log;

import java.util.ArrayList;

import static de.droidcachebox.core.GroundspeakAPI.GeoCacheRelated;
import static de.droidcachebox.core.GroundspeakAPI.updateGeoCache;

public class CacheContextMenu {
    private static final String ClassName = "CacheContextMenu";
    private static Menu cacheContextMenu;
    private static CancelWaitDialog wd;

    public static Menu getCacheContextMenu(boolean forCacheList) {

        cacheContextMenu = new Menu("DescriptionViewTitle");

        boolean selectedCacheIsSet = GlobalCore.isSetSelectedCache();
        boolean selectedCacheIsGC = false;
        if (selectedCacheIsSet) {
            selectedCacheIsGC = GlobalCore.getSelectedCache().getGcCode().startsWith("GC");
        }
        if (forCacheList) {
            // todo description
            cacheContextMenu.addMenuItem("Waypoints", Sprites.getSprite("big" + CacheTypes.Trailhead.name()), () -> Abstract_ShowWaypointAction.getInstance().Execute()).setEnabled(selectedCacheIsGC);
            cacheContextMenu.addMenuItem("hint", Sprites.getSprite(IconName.hintIcon.name()), () -> Action_HintDialog.getInstance().showHint()).setEnabled(selectedCacheIsGC && GlobalCore.getSelectedCache().hasHint());
            cacheContextMenu.addMenuItem("spoiler", Sprites.getSprite(IconName.imagesIcon.name()), () -> Abstract_ShowSpoilerAction.getInstance().Execute()).setEnabled(GlobalCore.selectedCachehasSpoiler());
            cacheContextMenu.addMenuItem("ShowLogs", Sprites.getSprite(IconName.listIcon.name()), () -> Abstract_ShowLogAction.getInstance().Execute()).setEnabled(selectedCacheIsGC);
            // todo notes
            // todo TBList
            // todo external description
        }
        cacheContextMenu.addMenuItem("ReloadCacheAPI", Sprites.getSprite(IconName.dayGcLiveIcon.name()), CacheContextMenu::reloadSelectedCache).setEnabled(selectedCacheIsGC);
        MenuItem mi;
        mi = cacheContextMenu.addMenuItem("Favorite", Sprites.getSprite(IconName.favorit.name()), () -> {
            GlobalCore.getSelectedCache().setFavorite(!GlobalCore.getSelectedCache().isFavorite());
            CacheDAO dao = new CacheDAO();
            dao.UpdateDatabase(GlobalCore.getSelectedCache());
            // Update cacheList
            Database.Data.cacheList.getCacheByIdFromCacheList(GlobalCore.getSelectedCache().Id).setFavorite(GlobalCore.getSelectedCache().isFavorite());
            // Update View
            Abstract_ShowDescriptionAction.getInstance().updateDescriptionView(true);
            CacheListChangedListeners.getInstance().cacheListChanged();
        });
        mi.setEnabled(selectedCacheIsSet);
        mi.setCheckable(true);
        mi.setChecked(selectedCacheIsSet && GlobalCore.getSelectedCache().isFavorite());
        cacheContextMenu.addMenuItem("AddToWatchList", null, () -> {
            if (GlobalCore.isSetSelectedCache()) {
                GL.that.postAsync(() -> {
                    if (GroundspeakAPI.AddToWatchList(GlobalCore.getSelectedCache().getGcCode()) == GroundspeakAPI.OK) {
                        MessageBox.show(Translation.get("ok"), Translation.get("AddToWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                    } else {
                        MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("AddToWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                    }
                });
            }
        }).setEnabled(selectedCacheIsGC);
        cacheContextMenu.addMenuItem("RemoveFromWatchList", null, () -> {
            if (GlobalCore.isSetSelectedCache()) {
                GL.that.postAsync(() -> {
                    if (GroundspeakAPI.RemoveFromWatchList(GlobalCore.getSelectedCache().getGcCode()) == GroundspeakAPI.OK) {
                        MessageBox.show(Translation.get("ok"), Translation.get("RemoveFromWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                    } else {
                        MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("RemoveFromWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                    }
                });
            }
        }).setEnabled(selectedCacheIsGC);
        cacheContextMenu.addMenuItem("Solver", Sprites.getSprite(IconName.solverIcon.name()), () -> Abstract_ShowSolverAction.getInstance().Execute()).setEnabled(selectedCacheIsGC);
        // todo solver2
        cacheContextMenu.addMenuItem("MI_EDIT_CACHE", null, () -> new EditCache().update(GlobalCore.getSelectedCache())).setEnabled(selectedCacheIsSet);
        cacheContextMenu.addMenuItem("MI_DELETE_CACHE", null, () -> {
            deleteSelectedCache();
            GlobalCore.setSelectedWaypoint(null, null, true);
        }).setEnabled(selectedCacheIsSet);
        return cacheContextMenu;
    }

    public static void reloadSelectedCache() {
        if (GlobalCore.isSetSelectedCache()) {

            wd = CancelWaitDialog.ShowWait(Translation.get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

                @Override
                public void isCanceled() {
                    // TODO handle cancel
                }
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
                            CacheListDAO cacheListDAO = new CacheListDAO();
                            cacheListDAO.ReadCacheList(Database.Data.cacheList, sqlWhere, false, Config.ShowAllWaypoints.getValue());
                        }
                        CacheListChangedListeners.getInstance().cacheListChanged();
                        //
                        GlobalCore.setSelectedCache(Database.Data.cacheList.getCacheByGcCodeFromCacheList(GCCode));
                        GL.that.RunOnGL(() -> {
                            Abstract_ShowDescriptionAction.getInstance().updateDescriptionView(true);
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

    public static void deleteSelectedCache() {
        ArrayList<String> GcCodeList = new ArrayList<>();
        GcCodeList.add(GlobalCore.getSelectedCache().getGcCode());
        CacheListDAO dao = new CacheListDAO();
        dao.delCacheImages(GcCodeList, CB_Core_Settings.SpoilerFolder.getValue(), CB_Core_Settings.SpoilerFolderLocal.getValue(), CB_Core_Settings.DescriptionImageFolder.getValue(), CB_Core_Settings.DescriptionImageFolderLocal.getValue());

        for (int i = 0, n = GlobalCore.getSelectedCache().waypoints.size(); i < n; i++) {
            Waypoint wp = GlobalCore.getSelectedCache().waypoints.get(i);
            Database.DeleteFromDatabase(wp);
        }

        Database.Data.sql.delete("Caches", "GcCode='" + GlobalCore.getSelectedCache().getGcCode() + "'", null);

        LogDAO logdao = new LogDAO();
        //logdao.ClearOrphanedLogs(); // doit when you have more time
        logdao.deleteLogs(GlobalCore.getSelectedCache().Id);
        EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());

        GlobalCore.setSelectedCache(null);

        CacheListChangedListeners.getInstance().cacheListChanged();
    }
}
