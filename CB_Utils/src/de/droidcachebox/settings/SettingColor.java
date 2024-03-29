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

import com.badlogic.gdx.graphics.Color;

import de.droidcachebox.gdx.graphics.HSV_Color;

/**
 * @author Longri
 */
public class SettingColor extends SettingBase<Color> {

    public SettingColor(String name, SettingCategory category, SettingModus modus, Color defaultValue, SettingStoreType StoreType) {
        super(name, category, modus, StoreType);
        this.defaultValue = defaultValue;
    }

    @Override
    public String toDBString() {
        return value.toString();
    }

    @Override
    public boolean fromDBString(String dbString) {
        try {
            value = new HSV_Color(dbString);
            return true;
        } catch (Exception e) {
            value = defaultValue;
            return false;
        }
    }

    @Override
    public SettingBase<Color> copy() {
        SettingBase<Color> ret = new SettingColor(name, category, modus, defaultValue, storeType);
        ret.value = value;
        ret.lastValue = lastValue;
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof SettingColor))
            return false;
        SettingColor inst = (SettingColor) obj;
        if (inst.name == null || name == null)
            return false;
        if (!(inst.name.equals(name)))
            return false;
        if (inst.value == null || value == null)
            return false;
        if (!inst.value.equals(value))
            return false;

        return true;
    }

}
