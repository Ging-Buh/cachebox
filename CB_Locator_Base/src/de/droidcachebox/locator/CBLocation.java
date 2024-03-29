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

package de.droidcachebox.locator;

import java.util.Date;

/**
 * Represented a GPS Signal with all given Informations like Position, Speed, accuracy, altitude, bearing, Sats
 *
 * @author Longri
 */
public class CBLocation extends CoordinateGPS {
    /**
     * Constant Location with all values are 0 ore false!</br> ProviderType is ProviderType.NULL
     * <p>
     * name="nULL_LOCATION"
     */
    public static final CBLocation NULL_LOCATION = new CBLocation(0, 0, 0);
    private static final long serialVersionUID = 3339644242602640422L;
    /**
     * name="hasSpeed"
     */
    private boolean hasSpeed = false;
    /**
     * name="speed"
     */
    private float speed = 0;
    /**
     * name="hasBearing"
     */
    private boolean hasBearing = false;
    /**
     * name="bearing"
     */
    private float bearing = 0;

    private float altitude = 0;

    /**
     * name="provider"
     */
    private ProviderType provider = ProviderType.NULL;
    /**
     * name="timeStamp"
     */
    private Date TimeStamp;

    /**
     * Constructor </br> You can set the values only over a constructor! </br> No manipulation of any value!
     *
     * @param latitude   ?
     * @param longitude  ?
     * @param accuracy   ?
     * @param hasSpeed   ?
     * @param speed      ?
     * @param hasBearing ?
     * @param bearing    ?
     * @param altitude   ?
     * @param provider   ?
     */
    public CBLocation(double latitude, double longitude, float accuracy, boolean hasSpeed, float speed, boolean hasBearing, float bearing, double altitude, ProviderType provider) {
        super(latitude, longitude, (int) accuracy);
        this.hasSpeed = hasSpeed;
        this.speed = speed;
        this.hasBearing = hasBearing;
        this.bearing = bearing;
        this.altitude = (float) altitude;
        this.provider = provider;
    }

    public CBLocation(int latitude, int longitude, int accuracy) {
        super(latitude, longitude, accuracy);
    }

    public CBLocation(double latitude, double longitude, float accuracy) {
        super(latitude, longitude);
        this.Accuracy = (int) accuracy;
    }

    /**
     * Returns the Provider Type of this location
     *
     * @return ProviderType as ProviderType
     */
    public ProviderType getProviderType() {
        return provider;
    }

    /**
     * Returns the Timestamp of this location
     *
     * @return Timestamp as date
     * name="timeStamp"
     */
    public Date getTimeStamp() {
        return TimeStamp;
    }

    /**
     * Returns the Latitude of this location!
     *
     * @return Latitude as double
     */
    @Override
    public double getLatitude() {
        return super.getLatitude();
    }

    /**
     * Returns the Longitude of this location
     *
     * @return Longitude as double
     */
    @Override
    public double getLongitude() {
        return super.getLongitude();
    }

    /**
     * Returns True if this Location have a bearing Value
     *
     * @return boolean
     * name="hasBearing"
     */
    public boolean getHasBearing() {
        return hasBearing;
    }

    public void setHasBearing(boolean hasBearing) {
        this.hasBearing = hasBearing;
    }

    /**
     * Returns the bearing of this location as float
     *
     * @return float
     * name="bearing"
     */
    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    /**
     * Returns True if this Location have a Speed Value
     *
     * @return boolean
     * name="hasSpeed"
     */
    public boolean getHasSpeed() {
        return hasSpeed;
    }

    public void setHasSpeed(boolean hasSpeed) {
        this.hasSpeed = hasSpeed;
    }

    /**
     * Returns the speed of this location as float (kmh)
     *
     * @return float
     * name="speed"
     */
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Returns the Altitude of this location as float (m)
     *
     * @return float
     * name="altitude"
     */
    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = (float) altitude;
    }

    public CoordinateGPS toCordinate() {
        return new CoordinateGPS(this);
    }

    @Override
    public void setAccuracy(float accuracy) {
        super.setAccuracy(accuracy);
    }

    public void setProvider(ProviderType provider) {
        this.provider = provider;
    }

    public CBLocation cpy() {
        CBLocation ret = new CBLocation(this.latitude, this.longitude, this.Accuracy);

        ret.hasSpeed = this.hasSpeed;
        ret.speed = this.speed;
        ret.hasBearing = this.hasBearing;
        ret.bearing = this.bearing;
        ret.altitude = this.altitude;
        ret.provider = this.provider;
        return ret;
    }

    @Override
    public String toString() {
        return "Location Typ:" + provider + " [latitude=" + latitude + ", longitude=" + longitude + "]";
    }

    public enum ProviderType {
        GPS,
        Network,
        Saved,
        NULL,
        any
    }

}
