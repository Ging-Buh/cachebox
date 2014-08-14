package CB_Core.Enums;

public enum LiveCacheTime
{
	min_10, min_30, min_60, h_6, h_12, h_24;

	public int getMinuten()
	{
		switch (this)
		{
		case h_24:
			return 1440;
		case h_12:
			return 720;
		case h_6:
			return 360;
		case min_10:
			return 10;
		case min_30:
			return 30;
		case min_60:
			return 60;
		default:
			return 1440;

		}
	}
}
