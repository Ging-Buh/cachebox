package CB_Locator;

import java.io.Serializable;

/**
 * @author Longri
 */
public class CoordinateGPS extends Coordinate implements Serializable
{

	private static final long serialVersionUID = 1235642315487L;

	/**
	 * @uml.property name="elevation"
	 */
	private double Elevation = 0;

	/**
	 * Die Genauigkeit dieser Coordinate! Wird beim Messen benutzt
	 * 
	 * @uml.property name="accuracy"
	 */
	protected int Accuracy = -1;

	public CoordinateGPS(double latitude, double longitude)
	{
		super(latitude, longitude);
		this.setElevation(0);
		Valid = true;
	}

	public CoordinateGPS(double latitude, double longitude, int accuracy)
	{
		super(latitude, longitude);
		this.setElevation(0);
		this.Accuracy = accuracy;
		Valid = true;
	}

	public CoordinateGPS(int latitude, int longitude, int accuracy)
	{
		super(latitude, longitude);
		this.setElevation(0);
		this.Accuracy = accuracy;
		Valid = true;
	}

	public CoordinateGPS(CoordinateGPS parent)
	{
		super(parent.latitude, parent.longitude);
		this.setElevation(parent.getElevation());
		this.Accuracy = parent.getAccuracy();
		this.Valid = parent.Valid;
	}

	public boolean hasAccuracy()
	{
		if (Accuracy == -1) return false;
		return true;
	}

	/**
	 * @return
	 * @uml.property name="accuracy"
	 */
	public int getAccuracy()
	{
		return Accuracy;
	}

	// Parse Coordinates from String
	public CoordinateGPS(String text)
	{
		super(text);
	}

	/**
	 * @return
	 * @uml.property name="elevation"
	 */
	public double getElevation()
	{
		return Elevation;
	}

	/**
	 * @param elevation
	 * @uml.property name="elevation"
	 */
	public void setElevation(double elevation)
	{
		Elevation = elevation;
	}

	public void setAccuracy(float accuracy)
	{
		Accuracy = (int) accuracy;
	}

}