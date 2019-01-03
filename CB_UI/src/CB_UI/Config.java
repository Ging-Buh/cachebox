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
package CB_UI;

import CB_Core.CB_Core_Settings;
import CB_Locator.LocatorSettings;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Config_Core;
import cb_rpc.Settings.CB_Rpc_Settings;

public class Config extends Config_Core implements CB_Core_Settings, CB_UI_Settings, CB_UI_Base_Settings, CB_Rpc_Settings, LocatorSettings {
    public static SettingsClass settings;
    private static Config that;

    public Config(String workPath) {
        super(workPath);
        settings = new SettingsClass();
        that = this;
    }

    public static void changeDayNight() {
        Boolean value = Config.nightMode.getValue();
        value = !value;
        Config.nightMode.setValue(value);
        Config.AcceptChanges();
    }

    public static void AcceptChanges() {
        that.acceptChanges();
    }

    protected void acceptChanges() {
        if (settings.WriteToDB()) {
            //TODO change to Dialog for restart now
            GL.that.Toast(Translation.Get("SettingChangesNeedRestart"));
        }
    }

}
