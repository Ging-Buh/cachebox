/*
 * Copyright (C) 2011-2022 team-cachebox.de
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

public class SettingBool extends SettingBase<Boolean> {

    public SettingBool(String name, SettingCategory category, SettingModus modus, boolean defaultValue, SettingStoreType StoreType) {
        super(name, category, modus, StoreType);
        this.defaultValue = defaultValue;
        value = defaultValue;
    }

    public SettingBool(String name, SettingCategory category, SettingModus modus, boolean defaultValue, SettingStoreType StoreType, boolean needRestart) {
        super(name, category, modus, StoreType);
        this.defaultValue = defaultValue;
        value = defaultValue;
        if (needRestart) setNeedRestart();
    }

    @Override
    public String toDBString() {
        return String.valueOf(value);
    }

    @Override
    public boolean fromDBString(String dbString) {
        try {
            value = Boolean.valueOf(dbString);
            return true;
        } catch (Exception ex) {
            value = defaultValue;
            return false;
        }
    }

    @Override
    public SettingBase<Boolean> copy() {
        SettingBase<Boolean> ret = new SettingBool(name, category, modus, defaultValue, storeType);

        ret.value = value;
        ret.lastValue = lastValue;

        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SettingBool))
            return false;

        SettingBool inst = (SettingBool) obj;
        if (!(inst.name.equals(name)))
            return false;
        return inst.value == value;
    }
}
