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
package de.droidcachebox;

import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.locator.LocatorSettings;
import de.droidcachebox.rpc.CB_Rpc_Settings;
import de.droidcachebox.settings.SettingsClass;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.Config_Core;

public class Config extends Config_Core implements CB_Core_Settings, CB_UI_Settings, CB_UI_Base_Settings, CB_Rpc_Settings, LocatorSettings {
    public static SettingsClass settings;
    private static Config that;

    public Config(String workPath) {
        super(workPath);
        settings = new SettingsClass();
        that = this;
    }

    public static void changeDayNight() {
        boolean value = Config.nightMode.getValue();
        value = !value;
        Config.nightMode.setValue(value);
        Config.AcceptChanges();
    }

    public static void AcceptChanges() {
        that.acceptChanges();
    }

    protected void acceptChanges() {
        if (settings.WriteToDB()) {
            MessageBox.show(Translation.get("Desc_SettingChangesNeedRestart"), Translation.get("SettingChangesNeedRestart"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
        }
    }

}
