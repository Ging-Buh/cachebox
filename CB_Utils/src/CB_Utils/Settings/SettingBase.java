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
import CB_Utils.Util.iChanged;

/**
 * @author ging-buh
 * @author Longri
 */
public abstract class SettingBase<T> implements Comparable<SettingBase<T>>
{

	protected CB_List<iChanged> ChangedEventList = new CB_List<iChanged>();
	protected SettingCategory category;
	protected String name;
	protected SettingModus modus;
	protected SettingStoreType storeType;
	protected SettingUsage usage;

	protected T value;
	protected T defaultValue;
	protected T lastValue;

	/**
	 * saves whether this setting is changed and needs to be saved
	 */
	protected boolean dirty;

	private static int indexCount = 0;
	private int index = -1;

	public SettingBase(String name, SettingCategory category, SettingModus modus, SettingStoreType StoreType, SettingUsage usage)
	{
		this.name = name;
		this.category = category;
		this.modus = modus;
		this.storeType = StoreType;
		this.usage = usage;
		this.dirty = false;

		this.index = indexCount++;
	}

	public void addChangedEventListner(iChanged listner)
	{
		synchronized (ChangedEventList)
		{
			if (!ChangedEventList.contains(listner)) ChangedEventList.add(listner);
		}
	}

	public void removeChangedEventListner(iChanged listner)
	{
		synchronized (ChangedEventList)
		{
			ChangedEventList.remove(listner);
		}
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public void setDirty()
	{
		dirty = true;
		fireChangedEvent();
	}

	public void clearDirty()
	{
		dirty = false;
	}

	public String getName()
	{
		return name;
	}

	public SettingCategory getCategory()
	{
		return category;
	}

	public SettingStoreType getStoreType()
	{
		return storeType;
	}

	public SettingModus getModus()
	{
		return modus;
	}

	public void changeSettingsModus(SettingModus Modus)
	{
		this.modus = Modus;
	}

	public abstract String toDBString();

	public abstract boolean fromDBString(String dbString);

	@Override
	public int compareTo(SettingBase<T> o)
	{
		return Double.compare(o.index, this.index);
	}

	private void fireChangedEvent()
	{
		synchronized (ChangedEventList)
		{
			// do this at new Thread, dont't block Ui-Thread

			Thread th = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					for (int i = 0, n = ChangedEventList.size(); i < n; i++)
					{
						iChanged event = ChangedEventList.get(i);
						event.isChanged();
					}
				}
			});
			th.start();
		}
	}

	public T getValue()
	{
		return (T) value;
	}

	public T getDefaultValue()
	{
		return defaultValue;
	}

	public void setValue(T newValue)
	{
		if (this.value.equals(newValue)) return;
		this.value = newValue;
		setDirty();
	}

	public void ForceDefaultChange(T defaultValue)
	{
		if (this.defaultValue.equals(defaultValue)) return;
		this.defaultValue = defaultValue;
	}

	public void loadDefault()
	{
		value = defaultValue;
	}

	public void saveToLastValue()
	{
		lastValue = (T) value;
	}

	public void loadFromLastValue()
	{
		if (lastValue == null) throw new IllegalArgumentException("You have never saved the last value! Call SaveToLastValue()");
		value = lastValue;
	}

	public abstract SettingBase<T> copy();

	@SuppressWarnings("unchecked")
	public void setValueFrom(SettingBase<?> cpy)
	{
		try
		{
			this.value = (T) cpy.value;
		}
		catch (Exception e)
		{

		}
	}

	public abstract boolean equals(Object obj);

	public SettingUsage getUsage()
	{
		return this.usage;
	}

	public boolean isDefault()
	{
		return value.equals(defaultValue);
	}
}
