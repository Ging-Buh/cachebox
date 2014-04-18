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
	private int Accuracy = -1;

	// Cache the inputs and outputs of computeDistanceAndBearing
	// so calls to distanceTo() and bearingTo() can share work
	// private final double mLat1 = 0.0;
	// private final double mLon1 = 0.0;
	// private final double mLat2 = 0.0;
	// private final double mLon2 = 0.0;
	// private final float mInitialBearing = 0.0f;
	// private float mDistance = 0.0f;

	public CoordinateGPS()
	{
		Valid = false;
	}

	public CoordinateGPS(double latitude, double longitude)
	{
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setElevation(0);
		Valid = true;
	}

	public CoordinateGPS(double latitude, double longitude, int accuracy)
	{
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setElevation(0);
		this.Accuracy = accuracy;
		Valid = true;
	}

	public CoordinateGPS(CoordinateGPS parent)
	{
		this.setLatitude(parent.getLatitude());
		this.setLongitude(parent.getLongitude());
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