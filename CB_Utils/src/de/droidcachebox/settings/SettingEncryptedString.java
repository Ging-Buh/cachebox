/*
 * Copyright (C) 2011-2020 team-cachebox.de
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
package de.droidcachebox.settings;

/**
 * im value intern ist die Einstellung verschlüsselt abgespeichert
 * so wie sie dann in die DB geschrieben wird.
 */
public class SettingEncryptedString extends SettingLongString {

    public SettingEncryptedString(String name, SettingCategory category, SettingModus modus, String defaultValue, SettingStoreType StoreType, SettingUsage usage) {
        super(name, category, modus, defaultValue, StoreType, usage);
    }

    // liefert die Einstellung im Klartext
    @Override
    public String getValue() {
        if (value == null)
            return value;
        else
            return Config_Core.decrypt(this.value);
    }

    // hiermit kann die Einstellung im Klartext übergeben werden und wird sofort
    // verschlüsselt
    @Override
    public void setValue(String value) {
        String encrypted = "";
        if (value.length() > 0)
            encrypted = Config_Core.encrypt(value);
        if ((this.value != null) && (this.value.equals(encrypted)))
            return;
        this.value = encrypted;
        setDirty();
    }

    // Liefert die verschlüsselte Einstellung zurück
    public String getEncryptedValue() {
        return this.value;
    }

    // hier kann die schon verschlüsselte Einstellung übergeben werden.
    public void setEncryptedValue(String value) {
        if ((this.value != null) && (this.value.equals(value)))
            return;
        this.value = value;
        setDirty();
    }

    // liefert den Standardwert im Klartext
    @Override
    public String getDefaultValue() {
        return Config_Core.decrypt(this.defaultValue);
    }

    // liefert den verschlüsselten Standadwert
    public String getEncryptedDefaultValue() {
        return this.defaultValue;
    }

}
