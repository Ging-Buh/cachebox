/* 
 * Copyright (C) 2013 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package CB_Locator;

import java.util.Date;

/**
 * Represented a GPS Signal with all given Informations like Position, Speed, accuracy, altitude, bearing, Sats
 * 
 * @author Longri
 */
public class Location
{

	/**
	 * @author Longri
	 */
	public enum ProviderType
	{
		/**
		 * @uml.property name="gPS"
		 * @uml.associationEnd
		 */
		GPS, /**
		 * @uml.property name="network"
		 * @uml.associationEnd
		 */
		Network, /**
		 * @uml.property name="saved"
		 * @uml.associationEnd
		 */
		Saved, /**
		 * @uml.property name="nULL"
		 * @uml.associationEnd
		 */
		NULL,

		any
	}

	/**
	 * @uml.property name="hasSpeed"
	 */
	private boolean hasSpeed = false;
	/**
	 * @uml.property name="speed"
	 */
	private float speed = 0;
	/**
	 * @uml.property name="hasBearing"
	 */
	private boolean hasBearing = false;
	/**
	 * @uml.property name="bearing"
	 */
	private float bearing = 0;
	/**
	 * @uml.property name="altitude"
	 */
	private float altitude = 0;
	/**
	 * @uml.property name="position"
	 * @uml.associationEnd
	 */
	private Coordinate Position = new Coordinate();
	/**
	 * @uml.property name="provider"
	 * @uml.associationEnd
	 */
	private ProviderType provider = ProviderType.NULL;
	/**
	 * @uml.property name="timeStamp"
	 */
	private Date TimeStamp;

	public Location()
	{

	}

	/**
	 * Constructor </br> You can set the values only over a constructor! </br> No manipulation of any value!
	 * 
	 * @param latitude
	 * @param longitude
	 * @param accuracy
	 * @param hasSpeed
	 * @param speed
	 * @param hasBearing
	 * @param bearing
	 * @param altitude
	 * @param provider
	 */
	public Location(double latitude, double longitude, float accuracy, boolean hasSpeed, float speed, boolean hasBearing, float bearing,
			double altitude, ProviderType provider)
	{
		synchronized (this)
		{
			Position = new Coordinate(latitude, longitude, (int) accuracy);
			this.hasSpeed = hasSpeed;
			this.speed = speed;
			this.hasBearing = hasBearing;
			this.bearing = bearing;
			this.altitude = (float) altitude;
			this.provider = provider;
		}
	}

	/**
	 * Constant Location with all values are 0 ore false!</br> ProviderType is ProviderType.NULL
	 * 
	 * @uml.property name="nULL_LOCATION"
	 * @uml.associationEnd
	 */
	public static final Location NULL_LOCATION = new Location();

	/**
	 * Returns the Provider Type of this location
	 * 
	 * @return ProviderType as ProviderType
	 */
	public ProviderType getProviderType()
	{
		return provider;
	}

	/**
	 * Returns the Timestamp of this location
	 * 
	 * @return Timestamp as date
	 * @uml.property name="timeStamp"
	 */
	public Date getTimeStamp()
	{
		return TimeStamp;
	}

	/**
	 * Returns the Latitude of this location!
	 * 
	 * @return Latitude as double
	 */
	public double getLatitude()
	{
		return Position.getLatitude();
	}

	/**
	 * Returns the Longitude of this location
	 * 
	 * @return Longitude as double
	 */
	public double getLongitude()
	{
		return Position.getLongitude();
	}

	/**
	 * Returns True if this Location have a bearing Value
	 * 
	 * @return boolean
	 * @uml.property name="hasBearing"
	 */
	public boolean getHasBearing()
	{
		return hasBearing;
	}

	/**
	 * Returns the bearing of this location as float
	 * 
	 * @return float
	 * @uml.property name="bearing"
	 */
	public float getBearing()
	{
		return bearing;
	}

	/**
	 * Returns True if this Location have a Speed Value
	 * 
	 * @return boolean
	 * @uml.property name="hasSpeed"
	 */
	public boolean getHasSpeed()
	{
		return hasSpeed;
	}

	/**
	 * Returns the speed of this location as float (kmh)
	 * 
	 * @return float
	 * @uml.property name="speed"
	 */
	public float getSpeed()
	{
		return speed;
	}

	/**
	 * Returns the Altitude of this location as float (m)
	 * 
	 * @return
	 * @uml.property name="altitude"
	 */
	public float getAltitude()
	{
		return altitude;
	}

	public Coordinate toCordinate()
	{
		return this.Position;
	}

	public void setlatitude(double latitude)
	{
		this.Position.setLatitude(latitude);
	}

	public void setLongitude(double longitude)
	{
		this.Position.setLongitude(longitude);
	}

	public void setAccuracy(float accuracy)
	{
		this.Position.setAccuracy(accuracy);
	}

	public void setHasSpeed(boolean hasSpeed)
	{
		this.hasSpeed = hasSpeed;
	}

	public void setSpeed(float speed)
	{
		this.speed = speed;
	}

	public void setHasBearing(boolean hasBearing)
	{
		this.hasBearing = hasBearing;
	}

	public void setBearing(float bearing)
	{
		this.bearing = bearing;
	}

	public void setAltitude(double altitude)
	{
		this.altitude = (float) altitude;
	}

	public void setProvider(ProviderType provider)
	{
		this.provider = provider;
	}

	public Location cpy()
	{
		Location ret = new Location();

		ret.Position = Position.copy();
		ret.hasSpeed = this.hasSpeed;
		ret.speed = this.speed;
		ret.hasBearing = this.hasBearing;
		ret.bearing = this.bearing;
		ret.altitude = this.altitude;
		ret.provider = this.provider;
		return ret;
	}

}
