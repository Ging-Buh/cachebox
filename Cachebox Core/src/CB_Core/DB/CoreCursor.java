package CB_Core.DB;

public class CoreCursor
{
	public CoreCursor()
	{
	}

	public boolean moveToFirst()
	{
		return false;
	}

	public boolean isAfterLast()
	{
		return true;
	}

	public boolean moveToNext()
	{
		return false;
	}

	public void close()
	{
	}
	
	public String getString(int columnIndex) {
		return "";
	}
	
	public long getLong(int columnIndex) {
		return 0;
	}

	public int getInt(int columnIndex) {
		return 0;
	}
	
	public boolean isNull(int columnIndex) {
		return true;
	}

	public double getDouble(int columnIndex) {
		return 0;
	}

	public short getShort(int columnIndex) {
		return 0;
	}

	public int getCount()  {
		return 0;
	}
	
}
