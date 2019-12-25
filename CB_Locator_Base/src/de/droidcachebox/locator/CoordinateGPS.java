/*
 * Copyright (C) 2011-2020 team-cachebox.de
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

import java.io.Serializable;

/**
 * @author Longri
 */
public class CoordinateGPS extends Coordinate implements Serializable {

    private static final long serialVersionUID = 1235642315487L;
    /**
     * Die Genauigkeit dieser Coordinate! Wird beim Messen benutzt
     *
     * @uml.property name="accuracy"
     */
    protected int Accuracy = -1;
    /**
     * @uml.property name="elevation"
     */
    private double Elevation = 0;

    public CoordinateGPS(double latitude, double longitude) {
        super(latitude, longitude);
        this.setElevation(0);
        if (latitude == 0 && longitude == 0)
            return;
        valid = true;
    }

    public CoordinateGPS(double latitude, double longitude, int accuracy) {
        super(latitude, longitude);
        this.setElevation(0);
        this.Accuracy = accuracy;
        if (latitude == 0 && longitude == 0)
            return;
        valid = true;
    }

    public CoordinateGPS(int latitude, int longitude, int accuracy) {
        super(latitude, longitude);
        this.setElevation(0);
        this.Accuracy = accuracy;
        if (latitude == 0 && longitude == 0)
            return;
        valid = true;
    }

    public CoordinateGPS(CoordinateGPS parent) {
        super(parent.latitude, parent.longitude);
        this.setElevation(parent.getElevation());
        this.Accuracy = parent.getAccuracy();
        this.valid = parent.valid;
    }

    // Parse Coordinates from String
    public CoordinateGPS(String text) {
        super(text);
    }

    public boolean hasAccuracy() {
        if (Accuracy == -1)
            return false;
        return true;
    }

    /**
     * @return
     * @uml.property name="accuracy"
     */
    public int getAccuracy() {
        return Accuracy;
    }

    public void setAccuracy(float accuracy) {
        Accuracy = (int) accuracy;
    }

    /**
     * @return
     * @uml.property name="elevation"
     */
    public double getElevation() {
        return Elevation;
    }

    /**
     * @param elevation
     * @uml.property name="elevation"
     */
    public void setElevation(double elevation) {
        Elevation = elevation;
    }

}