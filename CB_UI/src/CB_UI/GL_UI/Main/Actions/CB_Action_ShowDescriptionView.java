/*
 * Copyright (C) 2015 team-cachebox.de
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

import CB_Core.CacheListChangedEventList;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheDAO;
import CB_Core.Types.CacheListDAO;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.DeleteSelectedCache;
import CB_UI.GL_UI.Activitys.EditCache;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.DescriptionView;
import CB_UI.GlobalCore;
import CB_UI.WriteIntoDB;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Log.Log;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;

import static CB_Core.Api.GroundspeakAPI.GeoCacheRelated;
import static CB_Core.Api.GroundspeakAPI.updateGeoCache;

public class CB_Action_ShowDescriptionView extends CB_Action_ShowView {

    private static final String log = "CB_Action_ShowDescriptionView";
    CancelWaitDialog wd = null;
    EditCache editCache = null;

    public CB_Action_ShowDescriptionView() {
        super("Description", MenuID.AID_SHOW_DESCRIPTION);
    }

    @Override
    public void Execute() {
        if ((TabMainView.descriptionView == null) && (tabMainView != null) && (tab != null))
            TabMainView.descriptionView = new DescriptionView(tab.getContentRec(), "DescriptionView");

        if ((TabMainView.descriptionView != null) && (tab != null))
            tab.ShowView(TabMainView.descriptionView);
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.docIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return TabMainView.descriptionView;
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu cm = new Menu("CacheListContextMenu");

        cm.addOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switch (((MenuItem) v).getMenuItemId()) {
                    case MenuID.MI_FAVORIT:
                        if (GlobalCore.getSelectedCache() == null) {
                            GL_MsgBox.Show(Translation.Get("NoCacheSelect"), Translation.Get("Error"), MessageBoxIcon.Error);
                            return true;
                        }

                        GlobalCore.getSelectedCache().setFavorite(!GlobalCore.getSelectedCache().isFavorite());
                        CacheDAO dao = new CacheDAO();
                        dao.UpdateDatabase(GlobalCore.getSelectedCache());

                        // Update Query
                        Database.Data.Query.GetCacheById(GlobalCore.getSelectedCache().Id).setFavorite(GlobalCore.getSelectedCache().isFavorite());

                        // Update View
                        if (TabMainView.descriptionView != null)
                            TabMainView.descriptionView.onShow();

                        CacheListChangedEventList.Call();
                        return true;
                    case MenuID.MI_RELOAD_CACHE:
                        ReloadSelectedCache();
                        return true;
                    case MenuID.MI_EDIT_CACHE:
                        if (editCache == null)
                            editCache = new EditCache(ActivityBase.ActivityRec(), "editCache");
                        editCache.update(GlobalCore.getSelectedCache());
                        return true;
                    case MenuID.MI_DELETE_CACHE:
                        DeleteSelectedCache.Execute();
                        GlobalCore.setSelectedWaypoint(null, null, true);
                        return true;
                }
                return false;
            }

        });

        MenuItem mi;

        boolean isSelected = (GlobalCore.isSetSelectedCache());

        mi = cm.addItem(MenuID.MI_FAVORIT, "Favorite", Sprites.getSprite(IconName.favorit.name()));
        mi.setCheckable(true);
        if (isSelected) {
            mi.setChecked(GlobalCore.getSelectedCache().isFavorite());
        } else {
            mi.setEnabled(false);
        }
        cm.addItem(MenuID.MI_EDIT_CACHE, "MI_EDIT_CACHE");
        cm.addItem(MenuID.MI_DELETE_CACHE, "MI_DELETE_CACHE");

        boolean selectedCacheIsNoGC = false;

        if (isSelected)
            selectedCacheIsNoGC = !GlobalCore.getSelectedCache().getGcCode().startsWith("GC");
        mi = cm.addItem(MenuID.MI_RELOAD_CACHE, "ReloadCacheAPI", Sprites.getSprite(IconName.dayGcLiveIcon.name()));
        if (!isSelected)
            mi.setEnabled(false);
        if (selectedCacheIsNoGC)
            mi.setEnabled(false);

        return cm;
    }

    public void ReloadSelectedCache() {
        if (GlobalCore.getSelectedCache() == null) {
            GL_MsgBox.Show(Translation.Get("NoCacheSelect"), Translation.Get("Error"), MessageBoxIcon.Error);
            return;
        }

        wd = CancelWaitDialog.ShowWait(Translation.Get("ReloadCacheAPI"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

            @Override
            public void isCanceled() {
                // TODO handle cancel
            }
        }, new ICancelRunnable() {

            @Override
            public void run() {
                ArrayList<GeoCacheRelated> geoCacheRelateds = updateGeoCache(GlobalCore.getSelectedCache());
                if (geoCacheRelateds.size() > 0) {
                    try {
                        WriteIntoDB.CachesAndLogsAndImagesIntoDB(geoCacheRelateds);
                    } catch (InterruptedException e) {
                        Log.err(log, "WriteIntoDB.CachesAndLogsAndImagesIntoDB", e);
                    }

                    // Reload result from DB
                    synchronized (Database.Data.Query) {
                        String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());
                        CacheListDAO cacheListDAO = new CacheListDAO();
                        cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
                    }
                    CacheListChangedEventList.Call();
                    //
                    Cache selCache = Database.Data.Query.GetCacheByGcCode(GlobalCore.getSelectedCache().getGcCode());
                    GlobalCore.setSelectedCache(selCache);
                    GL.that.RunOnGL(() -> {
                        if (TabMainView.descriptionView != null) {
                            TabMainView.descriptionView.forceReload();
                            TabMainView.descriptionView.onShow();
                        }
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
    }

}
