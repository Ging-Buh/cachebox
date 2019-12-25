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

public class SettingString extends SettingBase<String> {

    public SettingString(String name, SettingCategory category, SettingModus modus, String defaultValue, SettingStoreType StoreType, SettingUsage usage) {
        super(name, category, modus, StoreType, usage);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public String toDBString() {
        return String.valueOf(value);
    }

    @Override
    public boolean fromDBString(String dbString) {
        try {
            value = dbString;
            return true;
        } catch (Exception ex) {
            value = defaultValue;
            return false;
        }
    }

    @Override
    public SettingBase<String> copy() {
        SettingBase<String> ret = new SettingString(this.name, this.category, this.modus, this.defaultValue, this.storeType, usage);
        ret.value = this.value;
        ret.lastValue = this.lastValue;
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SettingString))
            return false;

        SettingString inst = (SettingString) obj;
        if (!(inst.name.equals(this.name)))
            return false;
        if (!inst.value.equals(this.value))
            return false;

        return true;
    }

}
