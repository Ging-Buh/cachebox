package de.droidcachebox.utils;

public class PointD {
    public double x;
    public double y;

    public PointD(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return this.x + " / " + this.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PointD)) {
            return false;
        }
        PointD other = (PointD) obj;
        return x == other.x && y == other.y;
    }
}
