package CB_Utils.DB;

public abstract class CoreCursor
{
	public CoreCursor()
	{
	}

	public abstract boolean moveToFirst();

	public abstract boolean isAfterLast();

	public abstract boolean moveToNext();

	public abstract void close();
	
	public abstract String getString(int columnIndex);
	
	public abstract long getLong(int columnIndex);

	public abstract int getInt(int columnIndex);
	
	public abstract boolean isNull(int columnIndex);

	public abstract double getDouble(int columnIndex);

	public abstract short getShort(int columnIndex);

	public abstract int getCount();
	
}
