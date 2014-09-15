package CB_Locator.Map;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;

public class Track
{
	public ArrayList<TrackPoint> Points;
	public String Name;
	public String FileName;
	public boolean ShowRoute = false;
	public boolean IsActualTrack = false;
	public Color mColor;
	public double TrackLength;
	public double AltitudeDifference;

	public Track(String name, Color color)
	{
		Points = new ArrayList<TrackPoint>();
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

}