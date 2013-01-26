package CB_Core.Settings;

import java.util.ArrayList;

public class SettingBase implements Comparable<SettingBase>
{
	public interface iChanged
	{
		public void isChanged();
	}

	protected ArrayList<iChanged> ChangedEventList = new ArrayList<SettingBase.iChanged>();
	protected SettingCategory category;
	protected String name;
	protected SettingModus modus;
	protected SettingStoreType storeType;

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

	public String toDBString()
	{
		return "";
	}

	public boolean fromDBString(String dbString)
	{
		return false;
	}

	public void loadDefault()
	{

	}

	public void saveToLastValue()
	{

	}

	public void loadFromLastValue()
	{

	}

	@Override
	public int compareTo(SettingBase o)
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
}
