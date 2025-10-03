package de.droidcachebox.menu.menuBtn1;

import static de.droidcachebox.gdx.activities.EditFilterSettings.applyFilter;
import static de.droidcachebox.menu.Action.ShowEditFilterSettings;
import static de.droidcachebox.menu.Action.ShowSearchDialog;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CacheWithWP;
import de.droidcachebox.database.Database_Core;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.EditCache;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.MenuItem;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn1.contextmenus.ShowImportMenu;
import de.droidcachebox.menu.menuBtn1.contextmenus.ShowSelectDB;
import de.droidcachebox.menu.menuBtn1.executes.DeleteDialog;
import de.droidcachebox.menu.menuBtn1.executes.GeoCachesView;
import de.droidcachebox.menu.quickBtns.ShowSearchDialog;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

public class ShowGeoCaches extends AbstractShowAction {

    boolean isExecuting;
    private EditCache editCache;
    private GeoCachesView geoCachesView;

    public ShowGeoCaches() {
        super("cacheList", "  (" + CBDB.cacheList.size() + ")");
        editCache = null;
        geoCachesView = null;
        isExecuting = false;
    }

    @Override
    public void execute() {
        if (Platform.isGPSon())
            Platform.request_getLocationIfInBackground();
        if (geoCachesView == null)
            geoCachesView = new GeoCachesView();
        ViewManager.leftTab.showView(geoCachesView);
        isExecuting = true;
    }

    @Override
    public CB_View_Base getView() {
        return geoCachesView;
    }

    @Override
    public void viewIsHiding() {
        geoCachesView = null;
        isExecuting = false;
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

        String DBName = Settings.DatabaseName.getValue();
        try {
            int Pos = DBName.lastIndexOf(".");
            DBName = DBName.substring(0, Pos);
        } catch (Exception e) {
            DBName = "???";
        }

        MenuItem mi;
        cm.addMenuItem("ResortList", Sprites.getSprite(IconName.sortIcon.name()), () -> {
            if (!CBDB.cacheList.resortAtWork) {
                synchronized (CBDB.cacheList) {
                    Log.debug("ShowCacheList", "sort CacheList by Menu ResortList");
                    CacheWithWP nearestCacheWp = CBDB.cacheList.resort(Locator.getInstance().getValidPosition(GlobalCore.getSelectedCache().getCoordinate()));
                    if (nearestCacheWp != null && nearestCacheWp.getCache() != null) {
                        GlobalCore.setSelectedWaypoint(nearestCacheWp.getCache(), nearestCacheWp.getWaypoint());
                        GlobalCore.setNearestCache(nearestCacheWp.getCache());
                    }
                    setSelectedCacheVisible();
                }
            }
        });
        cm.addMenuItem("ResortListWithDistance", Sprites.getSprite(IconName.sortIcon.name()), () -> {
            if (!CBDB.cacheList.resortAtWork) {
                synchronized (CBDB.cacheList) {
                    Log.debug("ShowCacheList", "sort CacheList by Menu ResortList 5km");
                    FilterInstances.getLastFilter().isDistance = true;
                    CacheWithWP nearestCacheWp = CBDB.cacheList.resort(Locator.getInstance().getValidPosition(GlobalCore.getSelectedCache().getCoordinate()), 5000.);
                    if (nearestCacheWp != null && nearestCacheWp.getCache() != null) {
                        GlobalCore.setSelectedWaypoint(nearestCacheWp.getCache(), nearestCacheWp.getWaypoint());
                        GlobalCore.setNearestCache(nearestCacheWp.getCache());
                    }
                    setSelectedCacheVisible();
                }
            }
        });
        mi = cm.addMenuItem("setOrResetFilter", "", Sprites.getSprite(IconName.filter.name()), (v, x, y, pointer, button) -> {
            cm.close();
            boolean checked = ((MenuItem) v).isChecked();
            if (((MenuItem) v).isCheckboxClicked(x))
                checked = !checked;
            if (checked) {
                ShowEditFilterSettings.action.execute();
            } else {
                FilterInstances.setLastFilter(new FilterProperties());
                applyFilter(FilterInstances.getLastFilter());
            }
            return true;
        });
        mi.setCheckable(true);
        mi.setChecked(true);
        if (!FilterInstances.isLastFilterSet())
            mi.setCheckable(false);
        cm.addMenuItem("Search", Sprites.getSprite(IconName.lupe.name()), () -> {
            cm.close();
            // to ensure that cm is really closed and so the SearchDialog is a child of geoCachesView (else empty space is shown)
            // there is no event in geoCachesView that can be triggered for ShowSearchDialog.action.execute() (popup)
            GL.that.runOnGL(() -> {
                geoCachesView.setHeightOfSearchDialog(((ShowSearchDialog) ShowSearchDialog.action).getHeightOfSearchDialog());
                ShowSearchDialog.action.execute();
            });
        });
        cm.addMenuItem("importExport", Sprites.getSprite(IconName.importIcon.name()), () -> new ShowImportMenu().execute());
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
            final boolean finalChecked = checked;
            ButtonDialog bd = new ButtonDialog(Translation.get(msgText), Translation.get("Favorites"), MsgBoxButton.OKCancel, MsgBoxIcon.Question);
            bd.setButtonClickHandler((which, data) -> {
                if (which == ButtonDialog.BTN_LEFT_POSITIVE) {
                    CBDB.getInstance().beginTransaction();
                    Database_Core.Parameters args = new Database_Core.Parameters();
                    args.put("Favorit", finalChecked ? 1 : 0);
                    CBDB.getInstance().update("Caches", args, FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue()), null);
                    CBDB.getInstance().setTransactionSuccessful();
                    CBDB.getInstance().endTransaction();
                    ViewManager.reloadCacheList();
                    GlobalCore.checkSelectedCacheValid();
                }
                return true;
            });
            bd.show();
            return true;
        });
        mi.setCheckable(true);
        mi.setChecked(true); // default is to mark as Favorite
        cm.addMenuItem("manage", "  (" + DBName + ")", Sprites.getSprite(IconName.manageDb.name()), () -> new ShowSelectDB().execute());
        mi = cm.addMenuItem("AutoResort", null, () -> {
            GlobalCore.setAutoResort(!(GlobalCore.getAutoResort()));
            if (GlobalCore.getAutoResort()) {
                if (!CBDB.cacheList.resortAtWork) {
                    synchronized (CBDB.cacheList) {
                        Log.debug("ShowCacheList", "sort CacheList by Menu AutoResort");
                        CBDB.cacheList.resort(Locator.getInstance().getValidPosition(GlobalCore.getSelectedCache().getCoordinate()));
                    }
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
        cm.addMenuItem("DeleteCaches", Sprites.getSprite(IconName.DELETE.name()), () -> new DeleteDialog().show());
        cm.addMenuItem("ClearHistory", Sprites.getSprite("HISTORY"), this::clearHistory);

        return cm;
    }

    private void clearHistory() {
        CoreData.cacheHistory = "";
        if (FilterInstances.getLastFilter().isHistory) {
            applyFilter(FilterInstances.ALL);
            FilterInstances.setLastFilter(FilterInstances.ALL);
        }
    }

    public void setSelectedCacheVisible() {
        if (geoCachesView != null)
            geoCachesView.setSelectedCacheVisible();
    }

    public float getYPositionForSearchDialog(float heightOfSearchDialog) {
        if (geoCachesView != null)
            return geoCachesView.getYPositionForSearchDialog(heightOfSearchDialog);
        return 0;
    }

    public void resetHeightForSearchDialog() {
        if (geoCachesView != null)
            geoCachesView.resetHeightForSearchDialog();
    }

    public void setNameExtension(String newExtension) {
        titleExtension = newExtension;
    }
}
