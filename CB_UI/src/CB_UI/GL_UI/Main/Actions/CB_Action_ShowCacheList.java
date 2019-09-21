package CB_UI.GL_UI.Main.Actions;

import CB_Core.CB_Core_Settings;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Types.CacheWithWP;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.EditCache;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GL_UI.Views.CacheListView;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.cb.sqlite.Database_Core;

public class CB_Action_ShowCacheList extends CB_Action_ShowView {
    private static CB_Action_ShowCacheList that;
    private EditCache editCache;
    private MessageBox gL_MsgBox;

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
        ViewManager.leftTab.ShowView(CacheListView.getInstance());
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
        Menu cm = new Menu("CacheListViewTitle");

        String DBName = Config.DatabaseName.getValue();
        try {
            int Pos = DBName.lastIndexOf(".");
            DBName = DBName.substring(0, Pos);
        } catch (Exception e) {
            DBName = "???";
        }

        MenuItem mi;
        cm.addMenuItem("ResortList", Sprites.getSprite(IconName.sortIcon.name()), () -> {
            synchronized (Database.Data.cacheList) {
                CacheWithWP nearstCacheWp = Database.Data.cacheList.resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));
                if (nearstCacheWp != null)
                    GlobalCore.setSelectedWaypoint(nearstCacheWp.getCache(), nearstCacheWp.getWaypoint());
                CacheListView.getInstance().setSelectedCacheVisible();
            }
        });
        mi = cm.addMenuItem("setOrResetFilter", "", Sprites.getSprite(IconName.filter.name()), (v, x, y, pointer, button) -> {
            cm.close();
            boolean checked = ((MenuItem) v).isChecked();
            if (((MenuItem) v).isCheckboxClicked(x))
                checked = !checked;
            if (checked) {
                Action_EditFilterSettings.getInstance().Execute();
            } else {
                FilterInstances.setLastFilter(new FilterProperties());
                EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());
            }
            return true;
        });
        mi.setCheckable(true);
        mi.setChecked(true);
        if (!FilterInstances.isLastFilterSet())
            mi.setCheckable(false);
        cm.addMenuItem("Search", Sprites.getSprite(IconName.lupe.name()), () -> {
            if (SearchDialog.that == null) {
                new SearchDialog();
            }
            SearchDialog.that.showNotCloseAutomaticly();
        });
        cm.addMenuItem("importExport", Sprites.getSprite(IconName.importIcon.name()), () -> CB_Action_ShowImportMenu.getInstance().Execute());
        mi = cm.addMenuItem("setOrResetFavorites", "", Sprites.getSprite(IconName.favorit.name()), (v, x, y, pointer, button) -> {
            cm.close();
            boolean checked = ((MenuItem) v).isChecked();
            if (((MenuItem) v).isCheckboxClicked(x))
                checked = !checked;
            String msgText;
            if (checked) {
                msgText = "askSetFavorites";
            } else {
                msgText = "askResetFavorites";
            }
            final boolean finalchecked = checked;
            gL_MsgBox = MessageBox.show(Translation.get(msgText), Translation.get("Favorites"), MessageBoxButtons.OKCancel, MessageBoxIcon.Question, (which, data) -> {
                gL_MsgBox_close();
                if (which == MessageBox.BUTTON_POSITIVE) {
                    Database.Data.sql.beginTransaction();
                    Database_Core.Parameters args = new Database_Core.Parameters();
                    args.put("Favorit", finalchecked ? 1 : 0);
                    Database.Data.sql.update("Caches", args, FilterInstances.getLastFilter().getSqlWhere(CB_Core_Settings.GcLogin.getValue()), null);
                    Database.Data.sql.setTransactionSuccessful();
                    Database.Data.sql.endTransaction();
                    ViewManager.reloadCacheList();
                    GlobalCore.checkSelectedCacheValid();
                }
                return true;
            });
            return true;
        });
        mi.setCheckable(true);
        mi.setChecked(true); // default is to mark as Favorite
        cm.addMenuItem("manage", "  (" + DBName + ")", Sprites.getSprite(IconName.manageDb.name()), () -> Action_SelectDBDialog.getInstance().Execute());
        mi = cm.addMenuItem("AutoResort", null, () -> {
            GlobalCore.setAutoResort(!(GlobalCore.getAutoResort()));
            if (GlobalCore.getAutoResort()) {
                synchronized (Database.Data.cacheList) {
                    Database.Data.cacheList.resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));
                }
            }
        });
        mi.setCheckable(true);
        mi.setChecked(GlobalCore.getAutoResort());
        cm.addMenuItem("MI_NEW_CACHE", Sprites.getSprite(IconName.addCacheIcon.name()), () -> {
            if (editCache == null) editCache = new EditCache();
            if (editCache.isDisposed()) editCache = new EditCache();
            editCache.create();
        });
        cm.addMenuItem("DeleteCaches", Sprites.getSprite(IconName.DELETE.name()), () -> Action_DeleteDialog.getInstance().Execute());

        return cm;
    }

    private void gL_MsgBox_close() {
        gL_MsgBox.close();
    }

    public void setNameExtension(String newExtension) {
        this.titleExtension = newExtension;
    }
}
