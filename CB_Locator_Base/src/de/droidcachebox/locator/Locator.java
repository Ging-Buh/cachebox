/*
 * Copyright (C) 2014 team-cachebox.de
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

import de.droidcachebox.Energy;
import de.droidcachebox.locator.Location.ProviderType;
import de.droidcachebox.utils.UnitFormatter;
import de.droidcachebox.utils.log.Log;

import java.util.Date;

/**
 * @author Longri
 */
public class Locator {
    private static final String log = "Locator";
    private static Locator locator;
    private boolean fix = false;
    private long minGpsUpdateTime = 125;
    private double altCorrection = 0;
    private boolean mUseImperialUnits = false;
    private boolean mUseMagneticCompass = false;
    private int mMagneticCompassLevel = 5;
    private boolean isDisplayOff = false;
    private boolean hasSpeed = false;
    private Location mFineLocation;
    private Location mLastSavedFineLocation;
    private Location mNetworkLocation;
    private Location mSaveLocation;
    private float speed = 0;
    private float mlastMagneticHeading = 0;
    private float mlastGPSHeading = -1;
    private long mTimeStampSpeed = (new Date().getTime());
    private CompassType mLastUsedCompassType = CompassType.any;

    private Locator() {
        Energy.addChangedEventListener(() -> {
            isDisplayOff = Energy.isDisplayOff();
            Log.info(log, "Display off: " + isDisplayOff);
        });
    }

    public static Locator getInstance() {
        if (locator == null) locator = new Locator();
        return locator;
    }

    public boolean isFixed() {
        return fix;
    }

    /**
     * Set Display Off. </br>Only events with priority High will fire!
     */
    public void setDisplayOff() {
        isDisplayOff = true;
    }

    /**
     * Set Display on. </br> All events will fire!
     */
    public void setDisplayOn() {
        isDisplayOff = false;
    }

    /**
     * Returns True if the flag for DisplayOff is True!
     *
     */
    public boolean isDisplayOff() {
        return isDisplayOff;
    }

    /**
     * Returns the minimum update time for firing position changed event
     *
     * @return long
     */
    public long getMinUpdateTime() {
        return minGpsUpdateTime;
    }

    /**
     * Set the minimum update time for fire position changed event
     *
     * @param value as long
     */
    public void setMinUpdateTime(Long value) {
        minGpsUpdateTime = value;
    }

    /**
     * Set the speed level for using Hardware or GPS heading
     *
     */
    public void setHardwareCompassLevel(int value) {
        mMagneticCompassLevel = value;
    }

    /**
     * Set true if the Locator is use heading values from Hardware Compass
     *
     */
    public void setUseHardwareCompass(boolean value) {
        mUseMagneticCompass = value;
    }

    /**
     * Set a new location from GPS,Network or the last saved location!</br> For all given information is using the last best Location!</br>
     * 1. Gps</br> 2. Network</br> 3. Saved</br> </br> If the last set of GPS Location older 2min, the saved FineLocation will be cleaned!
     * (Fall back to Network or saved Location)
     *
     */
    public void setNewLocation(Location location) {
        if (location == null)
            location = Location.NULL_LOCATION;

        synchronized (locator) {
            switch (location.getProviderType()) {
                case Saved:
                    locator.mSaveLocation = location;
                    locator.hasSpeed = false;
                    locator.speed = 0;
                    break;
                case Network:
                    locator.mNetworkLocation = location;
                    // reset Speed only if last Speed value old
                    long time = new Date().getTime();
                    if (locator.mTimeStampSpeed + (minGpsUpdateTime * 3) < time) {
                        locator.hasSpeed = false;
                        locator.speed = 0;
                    }

                    break;
                case GPS:

                    locator.mFineLocation = location;
                    locator.mLastSavedFineLocation = location;
                    locator.hasSpeed = location.getHasSpeed();
                    locator.speed = location.getSpeed();
                    locator.mTimeStampSpeed = (new Date()).getTime();
                    if (location.getHasBearing()) {
                        setHeading(location.getBearing(), CompassType.GPS);
                    }

                    if (!fix) {
                        fix = true;
                        GPS_FallBackEventList.CallFix();
                    }

                    break;
                default:
                    Log.debug(log, "invalid Location provider");
                    break;
            }

            if (location.getHasSpeed()) PositionChangedListeners.speedChanged();
            PositionChangedListeners.positionChanged();
            if (location.getHasBearing()) PositionChangedListeners.orientationChanged();
        }
    }

    /**
     * Returns the last saved fine location (from GPS) or null !
     *
     */
    public Location getLastSavedFineLocation() {
        synchronized (locator) {
            return locator.mLastSavedFineLocation;
        }
    }

    /**
     * Returns the last Latitude from the last valid position!</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
     *
     * @return Latitude as double
     */
    public double getLatitude() {
        return getLatitude(ProviderType.any);
    }

    /**
     * Returns the last Latitude from the last position of the given ProviderType
     *
     */
    public double getLatitude(ProviderType type) {
        return getLocation(type).getLatitude();
    }

    /**
     * Returns the last Longitude from the last valid position!</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
     *
     * @return Longitude as double
     */
    public double getLongitude() {
        return getLongitude(ProviderType.any);
    }

    /**
     * Returns the last Longitude from the last position of the given ProviderType
     *
     */
    public double getLongitude(ProviderType type) {
        return getLocation(type).getLongitude();
    }

    /**
     * Returns the last valid position.</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
     *
     */
    public Location getLocation() {
        return getLocation(ProviderType.any);
    }

    /**
     * Returns the last valid position of the given ProviderType
     */
    public Location getLocation(ProviderType type) {
        synchronized (locator) {

            if (type == ProviderType.any) {
                if (locator.mFineLocation != null)
                    return locator.mFineLocation;
                if (locator.mNetworkLocation != null)
                    return locator.mNetworkLocation;
                if (locator.mSaveLocation != null)
                    return locator.mSaveLocation;
                return Location.NULL_LOCATION;
            } else if (type == ProviderType.GPS) {
                return locator.mLastSavedFineLocation;
            } else if (type == ProviderType.Network) {
                return locator.mNetworkLocation;
            } else if (type == ProviderType.Saved) {
                return locator.mSaveLocation;
            }
            return Location.NULL_LOCATION;
        }
    }

    /**
     * Returns the last valid position.</br> 1. Gps</br> 2. Network</br> 3. Saved</br>
     *
     */
    public CoordinateGPS getMyPosition() {
        return getLocation(ProviderType.any).toCordinate();
    }

    /**
     * Returns True if the saved Location != ProviderType.NULL
     *
     */
    public boolean isValid() {
        return getLocation().getProviderType() == ProviderType.GPS || getLocation().getProviderType() == ProviderType.Network;
    }

    public Coordinate getValidPosition(Coordinate defaultValue) {
        if (isValid()) {
            CoordinateGPS retValue = getLocation(ProviderType.any).toCordinate();
            if (retValue == Location.NULL_LOCATION) {
                return defaultValue;
            }
            else return retValue;
        }
        else
            return defaultValue;
    }

    /**
     * Returns the last valid position of the given ProviderType
     *
     */
    public Coordinate getMyPosition(ProviderType type) {
        Location loc = getLocation(type);
        if (loc == null)
            return null;
        return loc.toCordinate();
    }

    /**
     * Set a flag, that all Units are formated as Imperial Units or not! </br> default are false
     *
     * @param value as boolean
     */
    public void setUseImperialUnits(boolean value) {
        mUseImperialUnits = value;
    }

    /**
     * Returns the formated speed String!
     *
     */
    public String SpeedString() {
        synchronized (locator) {
            if (locator.hasSpeed)
                return Formatter.SpeedString(speedOverGround(), mUseImperialUnits);
            else
                return "-----";
        }
    }

    /**
     * Returns the Speed as float
     *
     */
    public float speedOverGround() {
        synchronized (locator) {
            if (locator.hasSpeed) {
                return locator.speed * 3600 / 1000;
            } else
                return 0;
        }
    }

    /**
     * Return True if the last valid Location from Type GPS
     *
     */
    public boolean isGPSprovided() {
        return getLocation().getProviderType() == ProviderType.GPS;
    }

    /**
     * Set the alt correction value
     *
     */
    public void setAltCorrection(double value) {
        Log.debug(log, "set alt corection to: " + value);
        altCorrection = value;
    }

    /**
     * Call this if GPS state changed to no sat have a fix
     */
    public void FallBack2Network() {
        synchronized (locator) {
            // check if last GPS position older then 20 sec

            if (locator.mTimeStampSpeed + 20000 >= (new Date()).getTime()) {
                return;
            }

            fix = false;
            locator.mFineLocation = null;
        }
        GPS_FallBackEventList.CallFallBack();
    }

    /**
     * Returns True if the last valid Location have a speed value
     *
     */
    public boolean hasSpeed() {
        synchronized (locator) {
            return locator.hasSpeed;
        }
    }

    /**
     * Returns the speed value of the last valid Location
     *
     */
    public double getSpeed() {
        synchronized (locator) {
            return locator.speed;
        }
    }

    /**
     * Returns the altitude with correction from last valid Location
     *
     */
    public double getAlt() {
        return getLocation().getAltitude() - altCorrection;
    }

    /**
     * Returns the formated string of last valid altitude with correction value
     *
     */
    public String getAltStringWithCorection() {
        String result = getAltString();
        if (altCorrection > 0)
            result += " (+" + UnitFormatter.AltString((float) altCorrection);
        else if (altCorrection < 0)
            result += " (" + UnitFormatter.AltString((float) altCorrection);
        return result;
    }

    /**
     * Returns the formated string of last valid altitude
     *
     */
    public String getAltString() {
        return UnitFormatter.AltString((float) getAlt());
    }

    /**
     * Returns the ProviderType of the last isValid Location
     *
     */
    public ProviderType getProvider() {
        return getLocation().getProviderType();
    }

    /**
     * Returns True if the used bearing value from magnetic compass. </br> Returns False if the bearing from GPS.
     *
     */
    public boolean UseMagneticCompass() {
        if (locator == null)
            return false;
        synchronized (locator) {
            return locator.mLastUsedCompassType == CompassType.Magnetic;
        }
    }

    /**
     * Returns the last saved heading
     *
     */
    public float getHeading() {
        return getHeading(CompassType.any);
    }

    /**
     * Returns the last saved heading of the given ProviderType
     *
     */
    public float getHeading(CompassType type) {
        synchronized (locator) {

            if (type == CompassType.GPS || !mUseMagneticCompass)
                return locator.mlastGPSHeading;
            if (type == CompassType.Magnetic)
                return locator.mlastGPSHeading;

            if (UseMagneticCompass()) {
                return locator.mlastMagneticHeading;
            } else {
                return locator.mlastGPSHeading;
            }
        }
    }

    /**
     * Set the heading from GPS or magnetic sensor
     *
     */
    public void setHeading(float heading, CompassType type) {

        if (type == CompassType.GPS) {
            locator.mlastGPSHeading = heading;
        } else {
            locator.mlastMagneticHeading = heading;
        }

        // set last used compass Type

        if ((locator.mlastGPSHeading > -1 && speedOverGround() > mMagneticCompassLevel) || !mUseMagneticCompass) {
            locator.mLastUsedCompassType = CompassType.GPS;
        } else {
            locator.mLastUsedCompassType = CompassType.Magnetic;
        }

        PositionChangedListeners.orientationChanged();
    }

    public enum CompassType {
        GPS,
        Magnetic,
        any
    }

}
