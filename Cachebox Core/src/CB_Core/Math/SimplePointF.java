package CB_Core.Math;

public class SimplePointF
{
	public float x;
	public float y;

	public SimplePointF(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString()
	{
		return String.valueOf(x) + "," + String.valueOf(y);
	}
}
