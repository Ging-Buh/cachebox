package CB_Utils.Math;

public class PointL {
    final static String POINT = "Point x/y ";
    final static String SLASH = "/";

    private long x;
    private long y;

    public PointL(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public void set(PointL from) {
        x = from.x;
        y = from.y;
    }

    public void set(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public long getY() {
        return y;
    }

    public void setY(long y) {
        this.y = y;
    }

    public boolean equals(Object anObject) {
        if (this == anObject) return true;
        if (anObject instanceof PointL) {
            return (x == ((PointL) anObject).x && y == ((PointL) anObject).y);
        }
        return false;
    }


    public String toString() {
        return POINT + this.x + SLASH + this.y;
    }
}
