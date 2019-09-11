package CB_Utils.Math;

public class PointD {
    public double X;
    public double Y;

    public PointD(double x, double y) {
        this.X = x;
        this.Y = y;
    }

    @Override
    public String toString() {
        return this.X + " / " + this.Y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof PointD)) {
            return false;
        }
        PointD other = (PointD) obj;
        if (X == other.X && Y == other.Y)
            return true;
        else
            return false;
    }
}
