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
package de.droidcachebox.settings;

import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.log.Log;

public class SettingEnum<EnumTyp extends Enum<?>> extends SettingString {
    private static final String log = "SettingEnum";
    private final CB_List<String> values;

    private EnumTyp myEnum;

    public SettingEnum(String name, SettingCategory category, SettingModus modus, EnumTyp defaultValue, SettingStoreType StoreType, SettingUsage usage, EnumTyp enu) {
        super(name, category, modus, defaultValue.name(), StoreType, usage);
        myEnum = enu;

        values = new CB_List<>();

        // hier bekommst du die Klasse TestEnum
        Class c = enu.getDeclaringClass();
        // hier kannst du alle Zustände abfragen
        Object[] oo = c.getEnumConstants();
        // hier kannst du dann über alle Zustände iterieren
        for (Object o : oo) {
            // und von jedem den Namen abfragen (in unserem Beispiel "wert1", "wert2", "wert3"
            values.add(((Enum) o).name());
        }

    }

    public CB_List<String> getValues() {
        return values;
    }

    @Override
    public void setValue(String value) {
        if (this.value.equals(value))
            return;

        if (value == null || value.isEmpty()) {
            myEnum = getEnumFromString(defaultValue);
            setDirty();
            return;
        }

        this.value = value;
        myEnum = getEnumFromString(value);
        setDirty();
    }

    public EnumTyp getEnumValue() {
        return getEnumFromString(value);
    }

    public void setEnumValue(EnumTyp value) {
        if (this.myEnum == value)
            return;
        this.value = value.name();
        myEnum = value;
        setDirty();
    }

    public EnumTyp getEnumDefaultValue() {
        return getEnumFromString(defaultValue);
    }

    @SuppressWarnings("unchecked")
    private EnumTyp getEnumFromString(String stringValue) {
        EnumTyp ret = null;
        try {
            ret = (EnumTyp) Enum.valueOf(myEnum.getDeclaringClass(), stringValue);
        } catch (Exception e) {
            Log.err(log, "Wrong ENUM value:" + stringValue, e);
            ret = getEnumFromString(defaultValue);
        }

        return ret;
    }

}
