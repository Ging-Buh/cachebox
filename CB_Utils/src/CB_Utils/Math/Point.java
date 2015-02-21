package CB_Utils.Math;

public class Point
{
	final static String POINT = "Point x/y ";
	final static String SLASH = "/";

	public int x;
	public int y;

	public Point(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public Point()
	{
		this.x = 0;
		this.y = 0;
	}

	public String toString()
	{
		return POINT + this.x + SLASH + this.y;
	}
}
