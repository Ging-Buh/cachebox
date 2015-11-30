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
package CB_UI.GL_UI.Activitys;

import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import CB_Core.FilterProperties;
import CB_Core.DAO.CacheListDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DB.Database;
import CB_Core.Events.CacheListChangedEventList;
import CB_Core.Settings.CB_Core_Settings;
import CB_Core.Types.Waypoint;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;

public class DeleteSelectedCache {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(DeleteSelectedCache.class);

    public static void Execute() {
	// Images
	log.debug("Delete Images");
	ArrayList<String> GcCodeList = new ArrayList<String>();
	GcCodeList.add(GlobalCore.getSelectedCache().getGcCode());
	CacheListDAO dao = new CacheListDAO();
	dao.delCacheImages(GcCodeList, CB_Core_Settings.SpoilerFolder.getValue(), CB_Core_Settings.SpoilerFolderLocal.getValue(), CB_Core_Settings.DescriptionImageFolder.getValue(), CB_Core_Settings.DescriptionImageFolderLocal.getValue());
	GcCodeList = null;
	dao = null;
	// Waypoints
	log.debug("Delete Waypoints");
	for (int i = 0, n = GlobalCore.getSelectedCache().waypoints.size(); i < n; i++) {
	    Waypoint wp = GlobalCore.getSelectedCache().waypoints.get(i);
	    Database.DeleteFromDatabase(wp);
	}
	// Cache
	log.debug("Delete Cache " + GlobalCore.getSelectedCache().getGcCode());
	Database.Data.delete("Caches", "GcCode='" + GlobalCore.getSelectedCache().getGcCode() + "'", null);
	// Logs
	log.debug("Delete Logs");
	LogDAO logdao = new LogDAO();
	logdao.ClearOrphanedLogs();
	logdao = null;
	// compact DB hangs : commented out
	// log.debug("Delete compact DB");
	// Database.Data.execSQL("vacuum");
	// Filter Liste neu aufbauen oder gibt es eine schnellere Möglichkeit?
	log.debug("Execute LastFilter");
	EditFilterSettings.ApplyFilter(FilterProperties.LastFilter);
	log.debug("unselect Cache");
	GlobalCore.setSelectedCache(null);
	log.debug("Rebuild View");
	CacheListChangedEventList.Call();
    }
}
