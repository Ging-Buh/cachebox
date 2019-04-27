package CB_UI.GL_UI.Main.Actions;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.CacheListChangedEventList;
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
    private static final int MI_RELOAD_CACHE = 1;
    private static final int MI_WAYPOINTS = 2;
    private static final int MI_SHOW_LOGS = 3;
    private static final int MI_HINT = 4;
    private static final int MI_SPOILER = 5;
    private static final int MI_SOLVER = 6;
    private static final int MI_FAVORIT = 7;
    private static final int MI_EDIT_CACHE = 8;
    private static final int MI_DELETE_CACHE = 9;
    private static final int AddToWatchList = 10;
    private static final int RemoveFromWatchList = 11;
    private static Menu cacheContextMenu;
    private static CancelWaitDialog wd;

    public static Menu getCacheContextMenu(boolean forCacheList) {

        cacheContextMenu = new Menu(ClassName);

        boolean selectedCacheIsSet = GlobalCore.isSetSelectedCache();
        boolean selectedCacheIsGC = false;
        if (selectedCacheIsSet) {
            selectedCacheIsGC = GlobalCore.getSelectedCache().getGcCode().startsWith("GC");
        }
        if (forCacheList) {
            // todo description
            cacheContextMenu.addItem(MI_WAYPOINTS, "Waypoints", Sprites.getSprite("big" + CacheTypes.Trailhead.name())).setEnabled(selectedCacheIsGC);
            cacheContextMenu.addItem(MI_HINT, "hint", Sprites.getSprite(IconName.hintIcon.name())).setEnabled(selectedCacheIsGC && GlobalCore.getSelectedCache().hasHint());
            cacheContextMenu.addItem(MI_SPOILER, "spoiler", Sprites.getSprite(IconName.imagesIcon.name())).setEnabled(GlobalCore.selectedCachehasSpoiler());
            cacheContextMenu.addItem(MI_SHOW_LOGS, "ShowLogs", Sprites.getSprite(IconName.listIcon.name())).setEnabled(selectedCacheIsGC);
            // todo notes
            // todo TBList
            // todo external description
        }
        cacheContextMenu.addItem(MI_RELOAD_CACHE, "ReloadCacheAPI", Sprites.getSprite(IconName.dayGcLiveIcon.name())).setEnabled(selectedCacheIsGC);
        MenuItem mi;
        mi = cacheContextMenu.addItem(MI_FAVORIT, "Favorite", Sprites.getSprite(IconName.favorit.name()));
        mi.setCheckable(true);
        mi.setEnabled(selectedCacheIsSet);
        mi.setChecked(selectedCacheIsSet && GlobalCore.getSelectedCache().isFavorite());
        cacheContextMenu.addItem(AddToWatchList, "AddToWatchList").setEnabled(selectedCacheIsGC);
        cacheContextMenu.addItem(RemoveFromWatchList, "RemoveFromWatchList").setEnabled(selectedCacheIsGC);
        cacheContextMenu.addItem(MI_SOLVER, "Solver", Sprites.getSprite(IconName.solverIcon.name())).setEnabled(selectedCacheIsGC);
        // todo solver2
        cacheContextMenu.addItem(MI_EDIT_CACHE, "MI_EDIT_CACHE").setEnabled(selectedCacheIsSet);
        cacheContextMenu.addItem(MI_DELETE_CACHE, "MI_DELETE_CACHE").setEnabled(selectedCacheIsSet);

        cacheContextMenu.addOnItemClickListener((v, x, y, pointer, button) -> {
            // todo CB_UI_Base.GL_UI.Menu.MenuItemDivider cannot be cast to CB_UI_Base.GL_UI.Menu.MenuItem
            switch (((MenuItem) v).getMenuItemId()) {
                case MI_HINT:
                    Action_HintDialog.getInstance().showHint();
                    return true;
                case MI_RELOAD_CACHE:
                    ReloadSelectedCache();
                    return true;
                case MI_WAYPOINTS:
                    CB_Action_ShowWaypointView.getInstance().Execute();
                    return true;
                case MI_SHOW_LOGS:
                        CB_Action_ShowLogView.getInstance().Execute();
                    return true;
                case MI_SPOILER:
                        CB_Action_ShowSpoilerView.getInstance().Execute();
                    return true;
                case MI_SOLVER:
                        CB_Action_ShowSolverView.getInstance().Execute();
                    return true;
                case MI_EDIT_CACHE:
                    new EditCache().update(GlobalCore.getSelectedCache());
                    return true;
                case MI_FAVORIT:
                    GlobalCore.getSelectedCache().setFavorite(!GlobalCore.getSelectedCache().isFavorite());
                    CacheDAO dao = new CacheDAO();
                    dao.UpdateDatabase(GlobalCore.getSelectedCache());
                    // Update cacheList
                    Database.Data.cacheList.GetCacheById(GlobalCore.getSelectedCache().Id).setFavorite(GlobalCore.getSelectedCache().isFavorite());
                    // Update View
                    CB_Action_ShowDescriptionView.getInstance().updateDescriptionView(true);
                    CacheListChangedEventList.Call();
                    return true;
                case AddToWatchList:
                    if (GlobalCore.isSetSelectedCache()) {
                        GL.that.postAsync(() -> {
                            if (GroundspeakAPI.AddToWatchList(GlobalCore.getSelectedCache().getGcCode()) == GroundspeakAPI.OK) {
                                MessageBox.show(Translation.get("ok"), Translation.get("AddToWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                            } else {
                                MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("AddToWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                            }
                        });
                    }
                    return true;
                case RemoveFromWatchList:
                    if (GlobalCore.isSetSelectedCache()) {
                        GL.that.postAsync(() -> {
                            if (GroundspeakAPI.RemoveFromWatchList(GlobalCore.getSelectedCache().getGcCode()) == GroundspeakAPI.OK) {
                                MessageBox.show(Translation.get("ok"), Translation.get("RemoveFromWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                            } else {
                                MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("RemoveFromWatchList"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                            }
                        });
                    }
                    return true;
                case MI_DELETE_CACHE:
                    DeleteSelectedCache.Execute();
                    GlobalCore.setSelectedWaypoint(null, null, true);
                    return true;
                default:
                    return false;
            }
        });
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
                        } catch (InterruptedException e) {
                            Log.err(ClassName, "WriteIntoDB.CachesAndLogsAndImagesIntoDB", e);
                        }

                        // Reload result from DB
                        synchronized (Database.Data.cacheList) {
                            String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
                            CacheListDAO cacheListDAO = new CacheListDAO();
                            cacheListDAO.ReadCacheList(Database.Data.cacheList, sqlWhere, false, Config.ShowAllWaypoints.getValue());
                        }
                        CacheListChangedEventList.Call();
                        //
                        GlobalCore.setSelectedCache(Database.Data.cacheList.GetCacheByGcCode(GCCode));
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
