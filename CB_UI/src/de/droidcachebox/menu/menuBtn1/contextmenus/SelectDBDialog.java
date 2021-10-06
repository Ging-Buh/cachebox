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
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.CacheListDAO;
import de.droidcachebox.database.CacheWithWP;
import de.droidcachebox.database.Categories;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.SelectDB;
import de.droidcachebox.gdx.controls.dialogs.WaitDialog;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.utils.log.Log;

public class SelectDBDialog extends AbstractAction {
    private static final String log = "SelectDBDialog";
    private static SelectDBDialog that;
    private WaitDialog wd;

    private SelectDBDialog() {
        super("manageDB");
    }

    public static SelectDBDialog getInstance() {
        if (that == null) that = new SelectDBDialog();
        return that;
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
            // speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
            Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGeoCacheCode());
            Config.acceptChanges();
            Log.debug(log, "LastSelectedCache = " + GlobalCore.getSelectedCache().getGeoCacheCode());
        }

        SelectDB selectDBDialog = new SelectDB(new CB_RectF(0, 0, GL.that.getWidth(), GL.that.getHeight()), "SelectDbDialog", false);
        selectDBDialog.setReturnListener(this::returnFromSelectDB);
        selectDBDialog.show();
    }

    private void returnFromSelectDB() {

        wd = WaitDialog.ShowWait("Load DB ...");
        Log.debug(log, "\r\nSwitch DB " + Config.DatabaseName.getValue());

        Thread thread = new Thread(() -> {
            CBDB.getInstance().close();
            CBDB.getInstance().startUp(Config.workPath + "/" + Config.DatabaseName.getValue());

            Config.settings.ReadFromDB();

            CoreData.categories = new Categories();

            FilterInstances.setLastFilter(new FilterProperties(Config.FilterNew.getValue()));

            String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
            CacheDAO.getInstance().updateCacheCountForGPXFilenames();

            synchronized (CBDB.getInstance().cacheList) {
                CacheListDAO.getInstance().readCacheList(sqlWhere, false, false, Config.showAllWaypoints.getValue());
            }

            GlobalCore.setSelectedCache(null);

            if (CBDB.getInstance().cacheList.size() > 0) {
                GlobalCore.setAutoResort(Config.StartWithAutoSelect.getValue());
                if (GlobalCore.getAutoResort() && !CBDB.getInstance().cacheList.resortAtWork) {
                    synchronized (CBDB.getInstance().cacheList) {
                        Log.debug(log, "sort CacheList");
                        CacheWithWP ret = CBDB.getInstance().cacheList.resort(Locator.getInstance().getValidPosition(null));
                        // null -- GlobalCore.getSelectedCache().getCoordinate()
                        if (ret != null && ret.getCache() != null) {
                            Log.debug(log, "returnFromSelectDB:Set selectedCache to " + ret.getCache().getGeoCacheCode() + " from valid position.");
                            ret.getCache().loadDetail();
                            GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
                            GlobalCore.setNearestCache(ret.getCache());
                        }
                    }
                }

                if (GlobalCore.getSelectedCache() == null) {
                    // set selectedCache from lastselected Cache
                    String lastSelectedCache = Config.LastSelectedCache.getValue();
                    if (lastSelectedCache != null && lastSelectedCache.length() > 0) {
                        for (int i = 0, n = CBDB.getInstance().cacheList.size(); i < n; i++) {
                            Cache c = CBDB.getInstance().cacheList.get(i);

                            if (c.getGeoCacheCode().equalsIgnoreCase(lastSelectedCache)) {
                                try {
                                    Log.debug(log, "returnFromSelectDB:Set selectedCache to " + c.getGeoCacheCode() + " from lastSaved.");
                                    c.loadDetail();
                                    GlobalCore.setSelectedCache(c);
                                } catch (Exception ex) {
                                    Log.err(log, "set last selected Cache", ex);
                                }
                                break;
                            }
                        }
                    }
                }

                // Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
                if (GlobalCore.getSelectedCache() == null) {
                    Cache c = CBDB.getInstance().cacheList.get(0);
                    Log.debug(log, "returnFromSelectDB:Set selectedCache to " + c.getGeoCacheCode() + " from firstInDB");
                    c.loadDetail();
                    GlobalCore.setSelectedCache(c);
                }
                Log.debug(log, "selected cache: " + GlobalCore.getSelectedCache().getGeoCacheName() + " (" + GlobalCore.getSelectedCache().getGeoCacheCode() + ")");
            }

            CacheListChangedListeners.getInstance().cacheListChanged();
            ViewManager.that.filterSetChanged();

            wd.dismis();
        });

        thread.start();

    }
}
