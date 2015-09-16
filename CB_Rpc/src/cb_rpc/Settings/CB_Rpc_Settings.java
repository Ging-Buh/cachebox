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

import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingString;
import CB_Utils.Settings.SettingUsage;

public interface CB_Rpc_Settings {
	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus INVISIBLE = SettingModus.Invisible;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;
	public static final SettingModus DEVELOP = SettingModus.develop;

	public static final SettingString CBS_IP = new SettingString("CBS_IP",
			SettingCategory.CBS, DEVELOP, "", SettingStoreType.Global,
			SettingUsage.ACB);
	public static final SettingInt CBS_BLOCK_SIZE = new SettingInt(
			"CBS_BLOCKSIZE", SettingCategory.CBS, DEVELOP, 100,
			SettingStoreType.Global, SettingUsage.ACB);

}
