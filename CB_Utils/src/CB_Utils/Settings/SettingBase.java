package CB_Utils.Settings;

import java.util.ArrayList;

import CB_Utils.Util.iChanged;

public abstract class SettingBase<T> implements Comparable<SettingBase<T>>
{

	protected ArrayList<iChanged> ChangedEventList = new ArrayList<iChanged>();
	protected SettingCategory category;
	protected String name;
	protected SettingModus modus;
	protected SettingStoreType storeType;

	protected T value;
	protected T defaultValue;
	protected T lastValue;

	/**
	 * saves whether this setting is changed and needs to be saved
	 */
	protected boolean dirty;

	private static int indexCount = 0;
	private int index = -1;

	public SettingBase(String name, SettingCategory category, SettingModus modus, SettingStoreType StoreType)
	{
		this.name = name;
		this.category = category;
		this.modus = modus;
		this.storeType = StoreType;
		dirty = false;

		index = indexCount++;
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
		return (this.index < o.index ? -1 : (this.index == o.index ? 0 : 1));
	}

	private void fireChangedEvent()
	{
		synchronized (ChangedEventList)
		{
			for (iChanged event : ChangedEventList)
			{
				event.isChanged();
			}
		}

	}

	public T getValue()
	{
		return value;
	}

	public T getDefaultValue()
	{
		return defaultValue;
	}

	public void setValue(T value)
	{
		if (this.value.equals(value)) return;
		this.value = value;
		setDirty();
	}

	public void loadDefault()
	{
		value = defaultValue;
	}

	public void saveToLastValue()
	{
		lastValue = value;
	}

	public void loadFromLastValue()
	{
		if (lastValue == null) throw new IllegalArgumentException("You have never saved the last value! Call SaveToLastValue()");
		value = lastValue;
	}

}
