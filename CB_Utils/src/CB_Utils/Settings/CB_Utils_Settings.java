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
package CB_Utils.Settings;

import CB_Utils.Log.LogLevel;

public interface CB_Utils_Settings {
    SettingModus DEVELOPER = SettingModus.DEVELOPER;
    SettingModus NORMAL = SettingModus.Normal;
    SettingModus EXPERT = SettingModus.Expert;
    SettingModus NEVER = SettingModus.Never;

    SettingEnum<Enum<LogLevel>> AktLogLevel = new SettingEnum<Enum<LogLevel>>("AktLogLevel", SettingCategory.Debug, NORMAL, LogLevel.OFF, SettingStoreType.Platform, SettingUsage.ALL, LogLevel.OFF);

}
