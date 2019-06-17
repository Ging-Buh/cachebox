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

import java.util.ArrayList;

public class DeleteSelectedCache {
    private static final String log = "DeleteSelectedCache";

    public static void Execute() {
        ArrayList<String> GcCodeList = new ArrayList<>();
        GcCodeList.add(GlobalCore.getSelectedCache().getGcCode());
        CacheListDAO dao = new CacheListDAO();
        dao.delCacheImages(GcCodeList, CB_Core_Settings.SpoilerFolder.getValue(), CB_Core_Settings.SpoilerFolderLocal.getValue(), CB_Core_Settings.DescriptionImageFolder.getValue(), CB_Core_Settings.DescriptionImageFolderLocal.getValue());

        for (int i = 0, n = GlobalCore.getSelectedCache().waypoints.size(); i < n; i++) {
            Waypoint wp = GlobalCore.getSelectedCache().waypoints.get(i);
            Database.DeleteFromDatabase(wp);
        }

        Database.Data.sql.delete("Caches", "GcCode='" + GlobalCore.getSelectedCache().getGcCode() + "'", null);

        LogDAO logdao = new LogDAO();
        //logdao.ClearOrphanedLogs(); // doit when you have more time
        logdao.deleteLogs(GlobalCore.getSelectedCache().Id);
        EditFilterSettings.ApplyFilter(FilterInstances.getLastFilter());

        GlobalCore.setSelectedCache(null);

        CacheListChangedEventList.Call();
    }
}
