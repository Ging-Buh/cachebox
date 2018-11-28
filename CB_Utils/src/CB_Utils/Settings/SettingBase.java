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

package CB_Utils.Settings;

import CB_Utils.Lists.CB_List;
import CB_Utils.Util.IChanged;

/**
 * @author ging-buh
 * @author Longri
 */
public abstract class SettingBase<T> implements Comparable<SettingBase<T>> {

    private static int indexCount = 0;
    protected CB_List<IChanged> SettingChangedListeners = new CB_List<>();
    protected SettingCategory category;
    protected String name;
    protected SettingModus modus;
    protected SettingStoreType storeType;
    protected SettingUsage usage;
    protected T value;
    protected T defaultValue;
    protected T lastValue;
    protected boolean needRestart = false;
    /**
     * saves whether this setting is changed and needs to be saved
     */
    protected boolean dirty;
    private int index = -1;

    public SettingBase(String name, SettingCategory category, SettingModus modus, SettingStoreType StoreType, SettingUsage usage) {
        this.name = name;
        this.category = category;
        this.modus = modus;
        this.storeType = StoreType;
        this.usage = usage;
        this.dirty = false;

        this.index = indexCount++;
    }

    public void addSettingChangedListener(IChanged listener) {
        synchronized (SettingChangedListeners) {
            if (!SettingChangedListeners.contains(listener))
                SettingChangedListeners.add(listener);
        }
    }

    public void removeSettingChangedListener(IChanged listener) {
        synchronized (SettingChangedListeners) {
            SettingChangedListeners.remove(listener);
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty() {
        dirty = true;
        fireChangedEvent();
    }

    public void clearDirty() {
        dirty = false;
    }

    public String getName() {
        return name;
    }

    public SettingCategory getCategory() {
        return category;
    }

    public SettingStoreType getStoreType() {
        return storeType;
    }

    public SettingModus getModus() {
        return modus;
    }

    public void changeSettingsModus(SettingModus Modus) {
        this.modus = Modus;
    }

    public abstract String toDBString();

    public abstract boolean fromDBString(String dbString);

    @Override
    public int compareTo(SettingBase<T> o) {
        if (this.index > o.index) return 1;
        if (this.index == o.index) return 0;
        else return -1;
    }

    private void fireChangedEvent() {
        synchronized (SettingChangedListeners) {
            // do this at new Thread, dont't block Ui-Thread
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0, n = SettingChangedListeners.size(); i < n; i++) {
                        IChanged listener = SettingChangedListeners.get(i);
                        listener.handleChange();
                    }
                }
            });
            th.start();
        }
    }

    public T getValue() {
        return value;
    }

    public void setValue(T newValue) {
        if (ifValueEquals(newValue))
            return;
        this.value = newValue;
        setDirty();
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    protected boolean ifValueEquals(T newValue) {
        return this.value.equals(newValue);
    }

    public void ForceDefaultChange(T defaultValue) {
        if (this.defaultValue.equals(defaultValue))
            return;
        this.defaultValue = defaultValue;
    }

    public void loadDefault() {
        value = defaultValue;
    }

    public void saveToLastValue() {
        lastValue = value;
    }

    public void loadFromLastValue() {
        if (lastValue == null)
            throw new IllegalArgumentException("You have never saved the last value! Call SaveToLastValue()");
        value = lastValue;
    }

    public abstract SettingBase<T> copy();

    @SuppressWarnings("unchecked")
    public void setValueFrom(SettingBase<?> cpy) {
        try {
            this.value = (T) cpy.value;
        } catch (Exception e) {

        }
    }

    @Override
    public abstract boolean equals(Object obj);

    public SettingUsage getUsage() {
        return this.usage;
    }

    public boolean isDefault() {
        return value.equals(defaultValue);
    }

    public SettingBase<T> setNeedRestart() {
        needRestart = true;
        return this;
    }
}
