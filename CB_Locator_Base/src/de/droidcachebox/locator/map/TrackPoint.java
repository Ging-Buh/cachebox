package de.droidcachebox.locator.map;

import de.droidcachebox.utils.PointD;

import java.util.Date;

public class TrackPoint extends PointD {
    public double Elevation;
    public double Direction;
    public Date TimeStamp;

    public TrackPoint(double x, double y, double elevation, double direction, Date time) {
        super(x, y);
        this.Elevation = elevation;
        this.Direction = direction;
        this.TimeStamp = time;
    }
}
