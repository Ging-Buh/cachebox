package CB_UI.GL_UI.Main.Actions;

import CB_Core.CB_Core_Settings;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Types.CacheWithWP;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.EditCache;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Activitys.SyncActivity;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.CacheListView;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_Utils.Log.Log;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.cb.sqlite.Database_Core;

import java.util.Timer;
import java.util.TimerTask;

public class CB_Action_ShowCacheList extends CB_Action_ShowView {
    private static final int MI_FAVORIT = 164;
    private static CB_Action_ShowCacheList that;
    private EditCache editCache;

    private CB_Action_ShowCacheList() {
        super("cacheList", "  (" + Database.Data.cacheList.size() + ")", MenuID.AID_SHOW_CACHELIST);
        editCache = null;
    }

    public static CB_Action_ShowCacheList getInstance() {
        if (that == null) that = new CB_Action_ShowCacheList();
        return that;
    }

    @Override
    public void Execute() {
        TabMainView.leftTab.ShowView(CacheListView.getInstance());
    }

    @Override
    public CB_View_Base getView() {
        return CacheListView.getInstance();
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.cacheListIcon.name());
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu cm = new Menu("CacheListContextMenu");

        cm.addOnClickListener((v, x, y, pointer, button) -> {
            boolean checked;
            switch (((MenuItem) v).getMenuItemId()) {
                case MenuID.MI_RESORT:
                    synchronized (Database.Data.cacheList) {
                        CacheWithWP nearstCacheWp = Database.Data.cacheList.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));

                        if (nearstCacheWp != null)
                            GlobalCore.setSelectedWaypoint(nearstCacheWp.getCache(), nearstCacheWp.getWaypoint());
                        CacheListView.getInstance().setSelectedCacheVisible();
                    }
                    return true;
                case MenuID.MI_FilterSet:
                    checked = ((MenuItem) v).isChecked();
                    if (((MenuItem) v).isCheckboxClicked(x))
                        checked = !checked;
                    if (checked) {
                        CB_Action_ShowFilterSettings.getInstance().Execute();
                    } else {
                        FilterInstances.setLastFilter(new FilterProperties());
                        EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());
                    }
                    return true;
                case MI_FAVORIT:
                    checked = ((MenuItem) v).isChecked();
                    if (((MenuItem) v).isCheckboxClicked(x))
                        checked = !checked;
                    Database.Data.sql.beginTransaction();
                    Database_Core.Parameters args = new Database_Core.Parameters();
                    args.put("Favorit", checked ? 1 : 0);
                    Database.Data.sql.update("Caches", args, FilterInstances.getLastFilter().getSqlWhere(CB_Core_Settings.GcLogin.getValue()), null);
                    Database.Data.sql.setTransactionSuccessful();
                    Database.Data.sql.endTransaction();
                    TabMainView.reloadCacheList();
                    GlobalCore.checkSelectedCacheValid();
                    return true;
                case MenuID.MI_SEARCH_LIST:

                    if (SearchDialog.that == null) {
                        new SearchDialog();
                    }

                    SearchDialog.that.showNotCloseAutomaticly();

                    return true;
                case MenuID.AID_SHOW_IMPORT_MENU:
                    CB_Action_ShowImportMenu.getInstance().Execute();
                    return true;
                case MenuID.MI_SYNC:
                    SyncActivity sync = new SyncActivity();
                    sync.show();
                    return true;
                case MenuID.MI_MANAGE_DB:
                    CB_Action_Show_SelectDB_Dialog.getInstance().Execute();
                    return true;
                case MenuID.MI_AUTO_RESORT:
                    GlobalCore.setAutoResort(!(GlobalCore.getAutoResort()));
                    if (GlobalCore.getAutoResort()) {
                        synchronized (Database.Data.cacheList) {
                            Database.Data.cacheList.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));
                        }
                    }
                    return true;
                case MenuID.MI_CB_Action_chkState:
                    // todo remove the following code | is duplicate in CB_Action_ShowImportMenu
                    GL.that.postAsync(() -> {
                        // First check API-Key with visual Feedback
                        Log.debug("MI_CHK_STATE_API", "chkAPiLogInWithWaitDialog");
                        GlobalCore.chkAPiLogInWithWaitDialog(isAccessTokenInvalid -> {
                            Log.debug("checkReady", "isAccessTokenInvalid: " + isAccessTokenInvalid);
                            if (!isAccessTokenInvalid) {
                                TimerTask tt = new TimerTask() {
                                    @Override
                                    public void run() {
                                        GL.that.postAsync(() -> new CB_Action_chkState().Execute());
                                    }
                                };
                                Timer t = new Timer();
                                t.schedule(tt, 100);
                            }
                        });
                    });
                    return true;
                case MenuID.MI_NEW_CACHE:
                    if (editCache == null) editCache = new EditCache();
                    if (editCache.isDisposed()) editCache = new EditCache();
                    editCache.create();
                    return true;

                case MenuID.AID_SHOW_DELETE_DIALOG:
                    CB_Action_Show_Delete_Dialog.getInstance().Execute();
                    return true;
            }
            return false;
        });

        String DBName = Config.DatabaseName.getValue();
        try {
            int Pos = DBName.lastIndexOf(".");
            DBName = DBName.substring(0, Pos);
        } catch (Exception e) {
            DBName = "???";
        }

        MenuItem mi;
        cm.addItem(MenuID.MI_RESORT, "ResortList", Sprites.getSprite(IconName.sortIcon.name()));
        mi = cm.addItem(MenuID.MI_FilterSet, "setOrResetFilter", Sprites.getSprite(IconName.filter.name()));
        mi.setCheckable(true);
        mi.setChecked(true); // todo perhaps init with isfiltered
        cm.addItem(MenuID.MI_SEARCH_LIST, "Search", Sprites.getSprite(IconName.lupe.name()));
        cm.addItem(MenuID.AID_SHOW_IMPORT_MENU, "importExport", Sprites.getSprite(IconName.importIcon.name()));
        mi = cm.addItem(MI_FAVORIT, "setOrResetFavorites", Sprites.getSprite(IconName.favorit.name()));
        mi.setCheckable(true);
        mi.setChecked(true); // default is to mark as Favorite
        if (SyncActivity.RELEASED)
            cm.addItem(MenuID.MI_SYNC, "sync", Sprites.getSprite(IconName.importIcon.name()));
        cm.addItem(MenuID.MI_MANAGE_DB, "manage", "  (" + DBName + ")", Sprites.getSprite(IconName.manageDb.name()));
        mi = cm.addItem(MenuID.MI_AUTO_RESORT, "AutoResort");
        mi.setCheckable(true);
        mi.setChecked(GlobalCore.getAutoResort());
        // cm.addItem(MenuID.MI_CHK_STATE_API, "chkState", Sprites.getSprite(IconName.dayGcLiveIcon.name()));
        cm.addItem(MenuID.MI_NEW_CACHE, "MI_NEW_CACHE", Sprites.getSprite(IconName.addCacheIcon.name()));
        cm.addItem(MenuID.AID_SHOW_DELETE_DIALOG, "DeleteCaches", Sprites.getSprite(IconName.DELETE.name()));

        return cm;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public void setNameExtension(String newExtension) {
        this.nameExtension = newExtension;
    }
}
