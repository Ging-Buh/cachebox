package CB_UI.GL_UI.Menu;

import CB_Core.Api.SearchGC;
import CB_Core.CacheListChangedEventList;
import CB_Core.CacheTypes;
import CB_Core.Types.CacheDAO;
import CB_Core.Types.CacheListDAO;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.DeleteSelectedCache;
import CB_UI.GL_UI.Activitys.EditCache;
import CB_UI.GL_UI.Controls.Dialogs.HintDialog;
import CB_UI.GL_UI.Main.Actions.QuickButton.QuickButtonItem;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GlobalCore;
import CB_UI.WriteIntoDB;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Lists.CB_List;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class CB_AllContextMenuHandler {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(QuickButtonItem.class);
    static CancelWaitDialog wd;
    private static OnClickListener onItemClickListener = new OnClickListener() {

        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            EditCache editCache = null;
            CacheDAO dao = null;
            switch (((MenuItem) v).getMenuItemId()) {
                case MenuID.MI_HINT:
                    HintDialog.show();
                    return true;

                case MenuID.MI_RELOAD_CACHE_INFO:
                    wd = CancelWaitDialog.ShowWait(Translation.Get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

                        @Override
                        public void isCanceled() {

                        }
                    }, new cancelRunnable() {

                        @Override
                        public void run() {
                            SearchGC searchC = new SearchGC(GlobalCore.getSelectedCache().getGcCode());

                            searchC.number = 1;

                            CB_List<Cache> apiCaches = new CB_List<Cache>();
                            ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
                            ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();

                            CB_UI.SearchForGeocaches.getInstance().SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, GlobalCore.getSelectedCache().getGPXFilename_ID(), this);

                            try {
                                WriteIntoDB.CachesAndLogsAndImagesIntoDB(apiCaches, apiLogs, apiImages);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // Reload result from DB
                            synchronized (Database.Data.Query) {
                                String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
                                CacheListDAO cacheListDAO = new CacheListDAO();
                                cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
                            }

                            CacheListChangedEventList.Call();

                            wd.close();
                        }

                        @Override
                        public boolean cancel() {
                            // TODO Handle cancel
                            return false;
                        }
                    });

                    return true;

                case MenuID.MI_WAYPOINTS:
                    if (TabMainView.actionShowWaypointView != null)
                        TabMainView.actionShowWaypointView.Execute();
                    return true;

                case MenuID.MI_SHOW_LOGS:
                    if (TabMainView.actionShowLogView != null)
                        TabMainView.actionShowLogView.Execute();
                    return true;

                case MenuID.MI_SPOILER:
                    if (TabMainView.actionShowSpoilerView != null)
                        TabMainView.actionShowSpoilerView.Execute();
                    return true;

                case MenuID.MI_SOLVER:
                    if (TabMainView.actionShowSolverView != null)
                        TabMainView.actionShowSolverView.Execute();
                    return true;

                case MenuID.MI_EDIT_CACHE:
                    if (editCache == null)
                        editCache = new EditCache(ActivityBase.ActivityRec(), "editCache");
                    editCache.update(GlobalCore.getSelectedCache());
                    return true;

                case MenuID.MI_FAVORIT:
                    if (GlobalCore.isSetSelectedCache()) {
                        GlobalCore.getSelectedCache().setFavorite(!GlobalCore.getSelectedCache().isFavorite());
                        if (dao == null)
                            dao = new CacheDAO();
                        dao.UpdateDatabase(GlobalCore.getSelectedCache());
                        CacheListChangedEventList.Call();
                    }
                    return true;

                case MenuID.MI_DELETE_CACHE:
                    DeleteSelectedCache.Execute();
                    return true;

                default:
                    return false;

            }

        }
    };

    public static void showBtnCacheContextMenu() {

        boolean selectedCacheIsNull = (GlobalCore.getSelectedCache() == null);

        boolean selectedCacheIsNoGC = false;

        if (!selectedCacheIsNull) {
            selectedCacheIsNoGC = !GlobalCore.getSelectedCache().getGcCode().startsWith("GC");
        }

        Menu icm = new Menu("BtnCacheContextMenu");
        icm.addOnClickListener(onItemClickListener);
        MenuItem mi;

        mi = icm.addItem(MenuID.MI_RELOAD_CACHE_INFO, "ReloadCacheAPI", Sprites.getSprite(IconName.dayGcLiveIcon.name()));
        if (selectedCacheIsNull)
            mi.setEnabled(false);
        if (selectedCacheIsNoGC)
            mi.setEnabled(false);

        mi = icm.addItem(MenuID.MI_WAYPOINTS, "Waypoints", Sprites.getSprite("big" + CacheTypes.Trailhead.name())); //16
        if (selectedCacheIsNull)
            mi.setEnabled(false);

        mi = icm.addItem(MenuID.MI_SHOW_LOGS, "ShowLogs", Sprites.getSprite(IconName.listIcon.name()));
        if (selectedCacheIsNull)
            mi.setEnabled(false);

        mi = icm.addItem(MenuID.MI_HINT, "hint");
        boolean enabled = false;
        if (!selectedCacheIsNull && (GlobalCore.getSelectedCache().hasHint()))
            enabled = true;
        mi.setEnabled(enabled);
        mi.setIcon(new SpriteDrawable(Sprites.getSprite(IconName.hintIcon.name())));

        mi = icm.addItem(MenuID.MI_SPOILER, "spoiler", Sprites.getSprite(IconName.imagesIcon.name()));
        mi.setEnabled(GlobalCore.selectedCachehasSpoiler());

        mi = icm.addItem(MenuID.MI_SOLVER, "Solver", Sprites.getSprite(IconName.solverIcon.name()));
        if (selectedCacheIsNull)
            mi.setEnabled(false);

        if (GlobalCore.JokerisOnline()) {
            mi = icm.addItem(MenuID.MI_JOKER, "joker", Sprites.getSprite(IconName.jokerPhone.name()));
            // Menu Item Telefonjoker enabled / disabled abhänging von gcJoker MD5

            if (mi != null) {
                enabled = false;
                if (GlobalCore.JokerisOnline())
                    enabled = true;
                mi.setEnabled(enabled);
            }

        }

        mi = icm.addItem(MenuID.MI_EDIT_CACHE, "MI_EDIT_CACHE");
        if (selectedCacheIsNull)
            mi.setEnabled(false);

        mi = icm.addItem(MenuID.MI_FAVORIT, "Favorite", Sprites.getSprite(IconName.favorit.name()));
        mi.setCheckable(true);
        if (selectedCacheIsNull)
            mi.setEnabled(false);
        else
            mi.setChecked(GlobalCore.getSelectedCache().isFavorite());

        mi = icm.addItem(MenuID.MI_DELETE_CACHE, "MI_DELETE_CACHE");
        if (selectedCacheIsNull)
            mi.setEnabled(false);

        icm.Show();

    }

}
