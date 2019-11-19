package de.droidcachebox.main.menuBtn1;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CacheWithWP;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.Database_Core;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.EditCache;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.controls.popups.SearchDialog;
import de.droidcachebox.gdx.main.AbstractShowAction;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.MenuID;
import de.droidcachebox.gdx.main.MenuItem;
import de.droidcachebox.gdx.views.CacheListView;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.main.menuBtn1.contextmenus.DeleteDialog;
import de.droidcachebox.main.menuBtn1.contextmenus.EditFilterSettings;
import de.droidcachebox.main.menuBtn1.contextmenus.SelectDBDialog;
import de.droidcachebox.main.menuBtn1.contextmenus.ShowImportMenu;
import de.droidcachebox.translation.Translation;

public class ShowCacheList extends AbstractShowAction {
    private static ShowCacheList that;
    private EditCache editCache;
    private MessageBox gL_MsgBox;

    private ShowCacheList() {
        super("cacheList", "  (" + Database.Data.cacheList.size() + ")", MenuID.AID_SHOW_CACHELIST);
        editCache = null;
    }

    public static ShowCacheList getInstance() {
        if (that == null) that = new ShowCacheList();
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
                EditFilterSettings.getInstance().Execute();
            } else {
                FilterInstances.setLastFilter(new FilterProperties());
                de.droidcachebox.gdx.activities.EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());
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
        cm.addMenuItem("importExport", Sprites.getSprite(IconName.importIcon.name()), () -> ShowImportMenu.getInstance().Execute());
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
            gL_MsgBox = MessageBox.create(Translation.get(msgText), Translation.get("Favorites"), MessageBoxButtons.OKCancel, MessageBoxIcon.Question, (which, data) -> {
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
            gL_MsgBox.show();
            return true;
        });
        mi.setCheckable(true);
        mi.setChecked(true); // default is to mark as Favorite
        cm.addMenuItem("manage", "  (" + DBName + ")", Sprites.getSprite(IconName.manageDb.name()), () -> SelectDBDialog.getInstance().Execute());
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
        cm.addMenuItem("DeleteCaches", Sprites.getSprite(IconName.DELETE.name()), () -> DeleteDialog.getInstance().Execute());

        return cm;
    }

    private void gL_MsgBox_close() {
        gL_MsgBox.close();
    }

    public void setNameExtension(String newExtension) {
        this.titleExtension = newExtension;
    }
}
