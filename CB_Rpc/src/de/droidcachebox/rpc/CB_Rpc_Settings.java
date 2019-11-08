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
package de.droidcachebox.rpc;

import de.droidcachebox.settings.SettingInt;
import de.droidcachebox.settings.SettingString;

import static de.droidcachebox.settings.SettingCategory.CBS;
import static de.droidcachebox.settings.SettingModus.DEVELOPER;
import static de.droidcachebox.settings.SettingModus.EXPERT;
import static de.droidcachebox.settings.SettingStoreType.Global;
import static de.droidcachebox.settings.SettingUsage.ACB;

public interface CB_Rpc_Settings {
    SettingString CBS_IP = new SettingString("CBS_IP", CBS, EXPERT, "", Global, ACB);
    SettingInt CBS_BLOCK_SIZE = new SettingInt("CBS_BLOCKSIZE", CBS, DEVELOPER, 100, Global, ACB);
}
