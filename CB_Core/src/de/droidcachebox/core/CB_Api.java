/*
 * Copyright (C) 2011 team-cachebox.de
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

package de.droidcachebox.core;

import static de.droidcachebox.settings.AllSettings.UseTestUrl;

/**
 * Diese Klasse stellt eine verbindung zu Team-Cachebox.de her und gibt dort hinterlegte Informationen zurück. (GCAuth url ; Versionsnummer)
 *
 * @author Longri
 */
public class CB_Api {

    /**
     * Gibt die bei Team-Cachebox.de hinterlegte GC Auth url zurueck
     *
     * @return String
     */
    public static String getGcAuthUrl() {
        if (UseTestUrl.getValue()) {
            return "https://gc-oauth.longri.de/index.php?Version=s-ACB"; // not tested
        } else {
            return "https://gc-oauth.longri.de/index.php?Version=ACB";
        }
        /*
        try {
            String url, resultKey;
            if (UseTestUrl.getValue()) {
                url = "https://longri.de/CB_API/index.php?get=url_ACB_Staging";
                resultKey = "GcAuth_ACB_Staging";
            } else {
                url = "https://longri.de/CB_API/index.php?get=url_ACB";
                resultKey = "GcAuth_ACB";
            }
            Webb httpClient = Webb.create();
            JSONObject response = httpClient
                    .post(url)
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            return response.getString(resultKey).trim();
        } catch (Exception ex) {
            Log.err("CB_Api", "getGcAuthUrl", ex);
            return "";
        }
        */
    }
}
