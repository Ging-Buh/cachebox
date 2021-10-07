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

import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.Config_Core;

public class Config extends Config_Core {
    public Settings settings;
    public static Config that;

    public Config(String workPath) {
        super(workPath);
        settings = new Settings();
        that = this;
    }

    public void changeDayNight() {
        boolean value = settings.nightMode.getValue();
        value = !value;
        settings.nightMode.setValue(value);
        acceptChanges();
    }

    public void acceptChanges() {
        if (settings.writeToDatabases()) {
            MsgBox.show(Translation.get("Desc_SettingChangesNeedRestart"), Translation.get("SettingChangesNeedRestart"), MsgBoxButton.OK, MsgBoxIcon.Information, null);
        }
    }

}
