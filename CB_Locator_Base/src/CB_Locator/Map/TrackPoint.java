package CB_Locator.Map;

import java.util.Date;

import CB_Utils.Math.PointD;

public class TrackPoint extends PointD
{
	public double Elevation;
	public double Direction;
	public Date TimeStamp;

	public TrackPoint(double x, double y, double elevation, double direction, Date time)
	{
		super(x, y);
		this.Elevation = elevation;
		this.Direction = direction;
		this.TimeStamp = time;
	}

	@Override
	public String toString()
	{

		StringBuilder SB = new StringBuilder();
		SB.append("Trackpoint: " + Double.toString(X) + "/" + Double.toString(Y));
		return SB.toString();
	}
}
