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

import org.slf4j.LoggerFactory;

import CB_Core.CoreSettingsForward;
import CB_Core.FilterProperties;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.Categories;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Activitys.SelectDB;
import CB_UI.GL_UI.Activitys.SelectDB.ReturnListner;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Dialogs.WaitDialog;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.CB_ActionCommand;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Show_SelectDB_Dialog extends CB_ActionCommand {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(CB_Action_Show_SelectDB_Dialog.class);

    public CB_Action_Show_SelectDB_Dialog() {
	super("manageDB", MenuID.AID_SHOW_SELECT_DB_DIALOG);
    }

    @Override
    public boolean getEnabled() {
	return true;
    }

    @Override
    public Sprite getIcon() {
	return SpriteCacheBase.Icons.get(IconName.manageDB_41.ordinal());
    }

    @Override
    public void Execute() {

	if (GlobalCore.ifCacheSelected()) {
	    // speichere selektierten Cache, da nicht alles �ber die SelectedCacheEventList l�uft
	    Config.LastSelectedCache.setValue(GlobalCore.getSelectedCache().getGcCode());
	    Config.AcceptChanges();
	    log.debug("LastSelectedCache = " + GlobalCore.getSelectedCache().getGcCode());
	}

	SelectDB selectDBDialog = new SelectDB(new CB_RectF(0, 0, GL.that.getWidth(), GL.that.getHeight()), "SelectDbDialog", false);
	selectDBDialog.setReturnListner(new ReturnListner() {
	    @Override
	    public void back() {
		returnFromSelectDB();
	    }
	});
	selectDBDialog.show();
	selectDBDialog = null;
    }

    WaitDialog wd;

    private void returnFromSelectDB() {
	wd = WaitDialog.ShowWait("Load DB ...");

	log.debug("\r\nSwitch DB");
	Thread thread = new Thread(new Runnable() {

	    @Override
	    public void run() {
		Database.Data.Query.clear();
		Database.Data.Close();
		Database.Data.StartUp(Config.DatabasePath.getValue());

		Config.settings.ReadFromDB();

		CoreSettingsForward.Categories = new Categories();

		String FilterString = Config.FilterNew.getValue();
		FilterProperties.LastFilter = (FilterString.length() == 0) ? new FilterProperties(FilterProperties.presets[0].toString()) : new FilterProperties(FilterString);

		String sqlWhere = FilterProperties.LastFilter.getSqlWhere(Config.GcLogin.getValue());
		Database.Data.GPXFilenameUpdateCacheCount();

		synchronized (Database.Data.Query) {
		    CacheListDAO cacheListDAO = new CacheListDAO();
		    cacheListDAO.ReadCacheList(Database.Data.Query, sqlWhere, false, Config.ShowAllWaypoints.getValue());
		}

		// set selectedCache from lastselected Cache
		GlobalCore.setSelectedCache(null);
		String sGc = Config.LastSelectedCache.getValue();
		if (sGc != null && !sGc.equals("")) {
		    for (int i = 0, n = Database.Data.Query.size(); i < n; i++) {
			Cache c = Database.Data.Query.get(i);
			if (c.getGcCode().equalsIgnoreCase(sGc)) {
			    log.debug("returnFromSelectDB:Set selectedCache to " + c.getGcCode() + " from lastSaved.");
			    GlobalCore.setSelectedCache(c);
			    break;
			}
		    }
		}
		// Wenn noch kein Cache Selected ist dann einfach den ersten der Liste aktivieren
		if ((GlobalCore.getSelectedCache() == null) && (Database.Data.Query.size() > 0)) {
		    log.debug("Set selectedCache to " + Database.Data.Query.get(0).getGcCode() + " from firstInDB");
		    GlobalCore.setSelectedCache(Database.Data.Query.get(0));
		}

		GlobalCore.setAutoResort(Config.StartWithAutoSelect.getValue());

		CachListChangedEventList.Call();

		TabMainView.that.filterSetChanged();

		wd.dismis();
	    }
	});

	thread.start();

    }
}
