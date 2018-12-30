/*
 * Copyright (C) 2011-2014 team-cachebox.de
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
package cb_rpc.Settings;

import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingString;

public interface CB_Rpc_Settings {
    // Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
    SettingModus DEVELOPER = CB_Utils.Settings.SettingModus.DEVELOPER;
    SettingModus NORMAL = CB_Utils.Settings.SettingModus.NORMAL;
    //SettingModus EXPERT = CB_Utils.Settings.SettingModus.EXPERT;
    //SettingModus NEVER = CB_Utils.Settings.SettingModus.NEVER;

    SettingString CBS_IP = new SettingString("CBS_IP", CB_Utils.Settings.SettingCategory.CBS, NORMAL, "", CB_Utils.Settings.SettingStoreType.Global, CB_Utils.Settings.SettingUsage.ACB);
    SettingInt CBS_BLOCK_SIZE = new SettingInt("CBS_BLOCKSIZE", CB_Utils.Settings.SettingCategory.CBS, DEVELOPER, 100, CB_Utils.Settings.SettingStoreType.Global, CB_Utils.Settings.SettingUsage.ACB);

}
