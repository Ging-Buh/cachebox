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

public class SettingTime extends SettingBase<Integer> {

    public SettingTime(String name, SettingCategory category, SettingModus modus, int defaultValue, SettingStoreType StoreType, SettingUsage usage) {
        super(name, category, modus, StoreType, usage);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public int getMin() {

        int sec = value / 1000;

        return sec / 60;
    }

    public void setMin(int min) {
        setValue(((min * 60) + getSec()) * 1000);
    }

    public int getSec() {
        int sec = value / 1000;
        int min = sec / 60;

        return sec - (min * 60);
    }

    public void setSec(int sec) {
        setValue(((getMin() * 60) + sec) * 1000);
    }

    @Override
    public String toDBString() {
        return String.valueOf(value);
    }

    @Override
    public boolean fromDBString(String dbString) {
        try {
            value = Integer.valueOf(dbString);
            return true;
        } catch (Exception ex) {
            value = defaultValue;
            return false;
        }
    }

    @Override
    public SettingBase<Integer> copy() {
        SettingBase<Integer> ret = new SettingTime(this.name, this.category, this.modus, this.defaultValue, this.storeType, this.usage);
        ret.value = this.value;
        ret.lastValue = this.lastValue;
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SettingTime))
            return false;

        SettingTime inst = (SettingTime) obj;
        if (!(inst.name.equals(this.name)))
            return false;
        if (inst.value != this.value)
            return false;

        return true;
    }
}
