/*
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox.menu.menuBtn1.contextmenus;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CacheWithWP;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.Categories;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.controls.dialogs.WaitDialog;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn1.executes.SelectDB;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.utils.log.Log;

public class ShowSelectDB extends AbstractAction {
    private static final String sClass = "ShowSelectDB";

    public ShowSelectDB() {
        super("manageDB");
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

     @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.manageDb.name());
    }

    @Override
    public void execute() {

        if (GlobalCore.isSetSelectedCache()) {
            Settings.lastSelectedCache.setValue(GlobalCore.getSelectedCache().getGeoCacheCode());
            Settings.getInstance().acceptChanges();
        }

        SelectDB selectDB = new SelectDB(new CB_RectF(0, 0, GL.that.getWidth(), GL.that.getHeight()), "SelectDbDialog", false);
        // set selectedCache from last selected Cache
        // get first of list, if none selected till now
        selectDB.setReturnListener(new WaitDialog("Load DB ...", new RunAndReady() {
            @Override
            public void ready() {

            }

            @Override
            public void setIsCanceled() {

            }

            @Override
            public void run() {
                CachesDAO cachesDAO = new CachesDAO();
                CBDB.getInstance().close();
                CBDB.getInstance().startUp(GlobalCore.workPath + "/" + Settings.DatabaseName.getValue());
                Settings.getInstance().readFromDB();
                CoreData.categories = new Categories();
                FilterInstances.setLastFilter(new FilterProperties(Settings.lastFilter.getValue()));
                String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue());
                cachesDAO.updateCacheCountForGPXFilenames();
                synchronized (CBDB.getInstance().cacheList) {
                    cachesDAO.readCacheList(sqlWhere, false, false, Settings.showAllWaypoints.getValue());
                }

                GlobalCore.setSelectedCache(null);
                if (CBDB.getInstance().cacheList.size() > 0) {
                    GlobalCore.setAutoResort(Settings.StartWithAutoSelect.getValue());
                    if (GlobalCore.getAutoResort() && !CBDB.getInstance().cacheList.resortAtWork) {
                        synchronized (CBDB.getInstance().cacheList) {
                            CacheWithWP ret = CBDB.getInstance().cacheList.resort(Locator.getInstance().getValidPosition(null));
                            if (ret != null && ret.getCache() != null) {
                                cachesDAO.loadDetail(ret.getCache());
                                GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
                                GlobalCore.setNearestCache(ret.getCache());
                            }
                        }
                    }

                    if (GlobalCore.getSelectedCache() == null) {
                        // set selectedCache from last selected Cache
                        String lastSelectedCache = Settings.lastSelectedCache.getValue();
                        if (lastSelectedCache != null && lastSelectedCache.length() > 0) {
                            for (int i = 0, n = CBDB.getInstance().cacheList.size(); i < n; i++) {
                                Cache c = CBDB.getInstance().cacheList.get(i);
                                if (c.getGeoCacheCode().equalsIgnoreCase(lastSelectedCache)) {
                                    try {
                                        cachesDAO.loadDetail(c);
                                        GlobalCore.setSelectedCache(c);
                                    } catch (Exception ex) {
                                        Log.err(sClass, "set last selected Cache", ex);
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    // get first of list, if none selected till now
                    if (GlobalCore.getSelectedCache() == null) {
                        Cache c = CBDB.getInstance().cacheList.get(0);
                        cachesDAO.loadDetail(c);
                        GlobalCore.setSelectedCache(c);
                    }
                }

                CacheListChangedListeners.getInstance().fire();
                ViewManager.that.filterSetChanged();
            }
        })::show);
        selectDB.show();
    }
}
