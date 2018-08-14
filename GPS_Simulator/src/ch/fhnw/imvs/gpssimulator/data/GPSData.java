/*
 * Copyright (c) 2007 by the University of Applied Sciences Northwestern Switzerland (FHNW)
 *
 * This program can be redistributed or modified under the terms of the
 * GNU General Public License as published by the Free Software Foundation.
 * This program is distributed without any warranty or implied warranty
 * of merchantability or fitness for a particular purpose.
 *
 * See the GNU General Public License for more details.
 */

package ch.fhnw.imvs.gpssimulator.data;

import java.util.Vector;

public final class GPSData {

    private static Status status;

    ;
    private static double latitude;

    ;
    private static double longitude;

    ;
    private static Orientation ns;
    private static Orientation ew;
    private static double speed;
    private static double altitude;
    private static int course;
    private static int satellites;
    private static int quality;
    private static FixType fixType; // 3: 3D-fix, 2: 2D-fix, 1: no-fix
    private static double pdop;
    private static double hdop;
    private static double vdop;
    private static Mode mode;
    private static Vector<GPSDataListener> listeners = new Vector<GPSDataListener>();
    private static boolean running = false;

    static {
        status = GPSData.Status.A;
        latitude = 47.48135;
        longitude = 8.20797;
        ns = Orientation.NORTH;
        ew = Orientation.EAST;
        speed = 3;
        altitude = 315;
        course = 314;
        satellites = 5;
        quality = 1;
        hdop = 2;
        vdop = 2;
        pdop = 2.8;
        mode = Mode.SIMULATOR;
        fixType = FixType.FIX_3D;
    }

    private GPSData() {
    }

    // set default values

    public static void addChangeListener(GPSDataListener listener) {
        listeners.add(listener);
    }

    public static void start() {
        if (!running) {
            running = true;
            notifyChange();
        }
    }

    private static void notifyChange() {
        if (running)
            for (GPSDataListener l : listeners) {
                l.valueChanged();
            }
    }

    public static Status getStatus() {
        return status;
    }

    public static void setStatus(Status status) {
        if (GPSData.status != status) {
            GPSData.status = status;
            notifyChange();
        }
    }

    public synchronized static double getLatitude() {
        return latitude;
    }

    public synchronized static void setLatitude(double latitude) {
        if (GPSData.latitude != latitude) {
            GPSData.latitude = latitude;
            notifyChange();
        }
    }

    public synchronized static double getLongitude() {
        return longitude;
    }

    public synchronized static void setLongitude(double longitude) {
        if (GPSData.longitude != longitude) {
            GPSData.longitude = longitude;
            notifyChange();
        }
    }

    public static Orientation getEW() {
        return ew;
    }

    public static void setEW(Orientation ew) {
        if (ew == Orientation.NORTH || ew == Orientation.SOUTH)
            throw new IllegalArgumentException();
        if (GPSData.ew != ew) {
            GPSData.ew = ew;
            notifyChange();
        }
    }

    public static Orientation getNS() {
        return ns;
    }

    public static void setNS(Orientation ns) {
        if (ns == Orientation.EAST || ns == Orientation.WEST)
            throw new IllegalArgumentException();
        if (GPSData.ns != ns) {
            GPSData.ns = ns;
            notifyChange();
        }
    }

    public synchronized static double getSpeed() {
        return speed;
    }

    public synchronized static void setSpeed(double speed) {
        if (GPSData.speed != speed) {
            GPSData.speed = speed;
            notifyChange();
        }
    }

    public synchronized static double getAltitude() {
        return altitude;
    }

    public synchronized static void setAltitude(double altitude) {
        if (GPSData.altitude != altitude) {
            GPSData.altitude = altitude;
            notifyChange();
        }
    }

    public static int getCourse() {
        return course;
    }

    public static void setCourse(int course) {
        if (GPSData.course != course) {
            GPSData.course = course;
            notifyChange();
        }
    }

    public static int getSatellites() {
        return satellites;
    }

    public static void setSatellites(int satellites) {
        if (GPSData.satellites != satellites) {
            GPSData.satellites = satellites;
            notifyChange();
        }
    }

    public static int getQuality() {
        return quality;
    }

    public static void setQuality(int quality) {
        if (GPSData.quality != quality) {
            GPSData.quality = quality;
            notifyChange();
        }
    }

    public synchronized static double getPDOP() {
        return pdop;
    }

    public synchronized static void setPDOP(double pdop) {
        if (GPSData.pdop != pdop) {
            GPSData.pdop = pdop;
            notifyChange();
        }
    }

    public synchronized static double getHDOP() {
        return hdop;
    }

    public synchronized static void setHDOP(double hdop) {
        if (GPSData.hdop != hdop) {
            GPSData.hdop = hdop;
            notifyChange();
        }
    }

    public synchronized static double getVDOP() {
        return vdop;
    }

    public synchronized static void setVDOP(double vdop) {
        if (GPSData.vdop != vdop) {
            GPSData.vdop = vdop;
            notifyChange();
        }
    }

    public synchronized static Mode getMode() {
        return mode;
    }

    public synchronized static void setMode(Mode mode) {
        if (GPSData.mode != mode) {
            GPSData.mode = mode;
            notifyChange();
        }
    }

    public synchronized static FixType getFixType() {
        return fixType;
    }

    public synchronized static void setFixType(FixType fixType) {
        if (GPSData.fixType != fixType) {
            GPSData.fixType = fixType;
            notifyChange();
        }
    }

    public enum Status {
        A, V
    }

    public enum Orientation {
        EAST, WEST, NORTH, SOUTH;

        @Override
        public String toString() {
            return this.name().substring(0, 1) + this.name().substring(1).toLowerCase();
        }
    }

    public enum FixType {
        FIX_NONE, FIX_2D, FIX_3D
    }

    public enum Mode {
        AUTONOMOUS, DIFFERENTIAL, ESTIMATED, NOT_VALID, SIMULATOR
    }
}
