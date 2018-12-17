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

import CB_Core.CB_Core_Settings;
import CB_Core.CacheListChangedEventList;
import CB_Core.DAO.LogDAO;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.Types.CacheListDAO;
import CB_Core.Types.Waypoint;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GlobalCore;
import CB_Utils.Log.Log;

import java.util.ArrayList;

public class DeleteSelectedCache {
    private static final String log = "DeleteSelectedCache";

    public static void Execute() {
        // Images
        Log.debug(log, "Delete Images");
        ArrayList<String> GcCodeList = new ArrayList<String>();
        GcCodeList.add(GlobalCore.getSelectedCache().getGcCode());
        CacheListDAO dao = new CacheListDAO();
        dao.delCacheImages(GcCodeList, CB_Core_Settings.SpoilerFolder.getValue(), CB_Core_Settings.SpoilerFolderLocal.getValue(), CB_Core_Settings.DescriptionImageFolder.getValue(), CB_Core_Settings.DescriptionImageFolderLocal.getValue());
        GcCodeList = null;
        dao = null;
        // Waypoints
        Log.debug(log, "Delete Waypoints");
        for (int i = 0, n = GlobalCore.getSelectedCache().waypoints.size(); i < n; i++) {
            Waypoint wp = GlobalCore.getSelectedCache().waypoints.get(i);
            Database.DeleteFromDatabase(wp);
        }
        // Cache
        Log.debug(log, "Delete Cache " + GlobalCore.getSelectedCache().getGcCode());
        Database.Data.delete("Caches", "GcCode='" + GlobalCore.getSelectedCache().getGcCode() + "'", null);
        // Logs
        Log.debug(log, "Delete Logs");
        LogDAO logdao = new LogDAO();
        //logdao.ClearOrphanedLogs(); // doit when you have more time
        logdao.deleteLogs(GlobalCore.getSelectedCache().Id);
        logdao = null;
        // compact DB hangs : commented out
        // Log.debug(log, "Delete compact DB");
        // Database.Data.execSQL("vacuum");
        // Filter Liste neu aufbauen oder gibt es eine schnellere Möglichkeit?
        Log.debug(log, "Execute LastFilter");
        EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());
        Log.debug(log, "unselect Cache");
        GlobalCore.setSelectedCache(null);
        Log.debug(log, "Rebuild View");
        CacheListChangedEventList.Call();
    }
}
