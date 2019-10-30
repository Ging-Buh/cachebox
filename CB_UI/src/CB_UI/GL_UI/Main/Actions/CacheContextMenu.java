package CB_UI.GL_UI.Main.Actions;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.CacheListChangedListeners;
import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.Types.CacheDAO;
import CB_Core.Types.CacheListDAO;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.DeleteSelectedCache;
import CB_UI.GL_UI.Activitys.EditCache;
import CB_UI.GlobalCore;
import CB_UI.WriteIntoDB;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Log.Log;

import java.util.ArrayList;

import static CB_Core.Api.GroundspeakAPI.GeoCacheRelated;
import static CB_Core.Api.GroundspeakAPI.updateGeoCache;

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
            cacheContextMenu.addMenuItem("Waypoints", Sprites.getSprite("big" + CacheTypes.Trailhead.name()), () -> CB_Action_ShowWaypointView.getInstance().Execute()).setEnabled(selectedCacheIsGC);
            cacheContextMenu.addMenuItem("hint", Sprites.getSprite(IconName.hintIcon.name()), () -> Action_HintDialog.getInstance().showHint()).setEnabled(selectedCacheIsGC && GlobalCore.getSelectedCache().hasHint());
            cacheContextMenu.addMenuItem("spoiler", Sprites.getSprite(IconName.imagesIcon.name()), () -> CB_Action_ShowSpoilerView.getInstance().Execute()).setEnabled(GlobalCore.selectedCachehasSpoiler());
            cacheContextMenu.addMenuItem("ShowLogs", Sprites.getSprite(IconName.listIcon.name()), () -> CB_Action_ShowLogView.getInstance().Execute()).setEnabled(selectedCacheIsGC);
            // todo notes
            // todo TBList
            // todo external description
        }
        cacheContextMenu.addMenuItem("ReloadCacheAPI", Sprites.getSprite(IconName.dayGcLiveIcon.name()), CacheContextMenu::ReloadSelectedCache).setEnabled(selectedCacheIsGC);
        MenuItem mi;
        mi = cacheContextMenu.addMenuItem("Favorite", Sprites.getSprite(IconName.favorit.name()), () -> {
            GlobalCore.getSelectedCache().setFavorite(!GlobalCore.getSelectedCache().isFavorite());
            CacheDAO dao = new CacheDAO();
            dao.UpdateDatabase(GlobalCore.getSelectedCache());
            // Update cacheList
            Database.Data.cacheList.getCacheByIdFromCacheList(GlobalCore.getSelectedCache().Id).setFavorite(GlobalCore.getSelectedCache().isFavorite());
            // Update View
            CB_Action_ShowDescriptionView.getInstance().updateDescriptionView(true);
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
        cacheContextMenu.addMenuItem("Solver", Sprites.getSprite(IconName.solverIcon.name()), () -> CB_Action_ShowSolverView.getInstance().Execute()).setEnabled(selectedCacheIsGC);
        // todo solver2
        cacheContextMenu.addMenuItem("MI_EDIT_CACHE", null, () -> new EditCache().update(GlobalCore.getSelectedCache())).setEnabled(selectedCacheIsSet);
        cacheContextMenu.addMenuItem("MI_DELETE_CACHE", null, () -> {
            DeleteSelectedCache.Execute();
            GlobalCore.setSelectedWaypoint(null, null, true);
        }).setEnabled(selectedCacheIsSet);
        return cacheContextMenu;
    }

    public static void ReloadSelectedCache() {
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
                            CB_Action_ShowDescriptionView.getInstance().updateDescriptionView(true);
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

}
