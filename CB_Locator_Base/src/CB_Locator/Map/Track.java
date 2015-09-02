package CB_Locator.Map;

import java.util.ArrayList;

import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;

import com.badlogic.gdx.graphics.Color;

public class Track
{
	private final ArrayList<TrackPoint> Points;
	public String Name;
	public String FileName;
	public boolean ShowRoute = false;
	public boolean IsActualTrack = false;
	public Color mColor;
	public double TrackLength;
	public double AltitudeDifference;
	private boolean mTrackLengthDirty = true;

	public Track(String name, Color color)
	{
		Points = new ArrayList<TrackPoint>();
		Name = name;
		mColor = color;
	}

	public Track(String name, Color color, ArrayList<TrackPoint> points)
	{
		Points = points;
		Name = name;
		mColor = color;
	}

	public Color getColor()
	{
		return mColor;
	}

	public void setColor(Color color)
	{
		mColor = color;
	}

	public void add(TrackPoint newPoint)
	{
		Points.add(newPoint);
		mTrackLengthDirty = true;
	}

	private void calculateTrackLength()
	{

		double lastLat = Double.NEGATIVE_INFINITY;
		double lastLon = Double.NEGATIVE_INFINITY;
		double Distance = 0;
		float[] dist = new float[4];

		if (Points.size() > 1)
		{

			for (int i = 0; i < Points.size(); i++)
			{
				if (lastLat == Double.NEGATIVE_INFINITY)
				{
					TrackPoint po = Points.get(i);

					lastLat = po.X;
					lastLon = po.Y;

				}
				else
				{

					TrackPoint po = Points.get(i);

					MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, po.X, po.Y, lastLat, lastLon, dist);
					Distance += dist[0];

					lastLat = po.X;
					lastLon = po.Y;
				}
			}

		}
		TrackLength = Distance;
		mTrackLengthDirty = false;
	}

	@Override
	public String toString()
	{

		StringBuilder SB = new StringBuilder();
		if (Points != null && Points.size() >= 2)
		{
			SB.append("Track: start=" + Points.get(0).toString() + "  end=" + Points.get(Points.size() - 1));
			if (mTrackLengthDirty) calculateTrackLength();

			SB.append("  Tracklength=" + TrackLength);
		}
		else
		{
			SB.append("Empty Track");
		}
		return SB.toString();
	}

	public int PointSize()
	{
		return Points.size();
	}

	public TrackPoint get(int i)
	{
		return Points.get(i);
	}

	public ArrayList<TrackPoint> getPoints()
	{
		return Points;
	}

}