package de.droidcachebox.locator.map;

import de.droidcachebox.utils.PointD;

import java.util.Date;

public class TrackPoint extends PointD {
    public double elevation;
    public double direction;
    public Date date;

    public TrackPoint(double lon, double lat, double elevation, double direction, Date date) {
        super(lon, lat);
        this.elevation = elevation;
        this.direction = direction;
        this.date = date;
    }
}
