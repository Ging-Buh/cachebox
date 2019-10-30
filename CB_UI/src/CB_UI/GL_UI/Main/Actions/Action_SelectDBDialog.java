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
package CB_UI.GL_UI.Main.Actions;

import CB_Core.*;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheListDAO;
import CB_Core.Types.Categories;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.SelectDB;
import CB_UI.GL_UI.Activitys.SelectDB.IReturnListener;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.Controls.Dialogs.WaitDialog;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Log.Log;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Action_SelectDBDialog extends AbstractAction {
    private static final String log = "Action_SelectDBDialog";
    private static Action_SelectDBDialog that;
    WaitDialog wd;

    private Action_SelectDBDialog() {
        super("manageDB", MenuID.AID_SHOW_SELECT_DB_DIALOG);
    }

    public static Action_SelectDBDialog getInstance() {
        if (that == null) that = new Action_SelectDBDialog();
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
    public void Execute() {

        if (GlobalCore.isSetSelectedCache()) {
            // speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
            Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGcCode());
            Config.AcceptChanges();
            Log.debug(log, "LastSelectedCache = " + GlobalCore.getSelectedCache().getGcCode());
        }

        SelectDB selectDBDialog = new SelectDB(new CB_RectF(0, 0, GL.that.getWidth(), GL.that.getHeight()), "SelectDbDialog", false);
        selectDBDialog.setReturnListener(new IReturnListener() {
            @Override
            public void back() {
                returnFromSelectDB();
            }
        });
        selectDBDialog.show();
        selectDBDialog = null;
    }

    private void returnFromSelectDB() {
        wd = WaitDialog.ShowWait("Load DB ...");

        Log.debug(log, "\r\nSwitch DB");
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Database.Data.cacheList.clear();
                Database.Data.sql.close();
                Database.Data.StartUp(Config.mWorkPath + "/" + Config.DatabaseName.getValue());

                Config.settings.ReadFromDB();

                CoreSettingsForward.Categories = new Categories();

                FilterInstances.setLastFilter(new FilterProperties(Config.FilterNew.getValue()));

                String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
                Database.Data.GPXFilenameUpdateCacheCount();

                synchronized (Database.Data.cacheList) {
                    CacheListDAO cacheListDAO = new CacheListDAO();
                    cacheListDAO.ReadCacheList(Database.Data.cacheList, sqlWhere, false, Config.ShowAllWaypoints.getValue());
                }

                // set selectedCache from lastselected Cache
                GlobalCore.setSelectedCache(null);
                String sGc = Config.LastSelectedCache.getValue();
                if (sGc != null && sGc.length() > 0) {
                    for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++) {
                        Cache c = Database.Data.cacheList.get(i);

                        if (c.getGcCode().equalsIgnoreCase(sGc)) {
                            try {
                                Log.debug(log, "returnFromSelectDB:Set selectedCache to " + c.getGcCode() + " from lastSaved.");
                                c.loadDetail();
                                GlobalCore.setSelectedCache(c);
                            } catch (Exception ex) {
                                Log.err(log, "set last selected Cache", ex);
                            }
                            break;
                        }
                    }
                }
                // Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
                if ((GlobalCore.getSelectedCache() == null) && (Database.Data.cacheList.size() > 0)) {
                    Log.debug(log, "Set selectedCache to " + Database.Data.cacheList.get(0).getGcCode() + " from firstInDB");
                    GlobalCore.setSelectedCache(Database.Data.cacheList.get(0));
                }

                GlobalCore.setAutoResort(Config.StartWithAutoSelect.getValue());

                CacheListChangedListeners.getInstance().cacheListChanged();

                ViewManager.that.filterSetChanged();

                wd.dismis();
            }
        });

        thread.start();

    }
}
