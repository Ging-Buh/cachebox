package CB_Utils.Math;

public class PointL {
	final static String POINT = "Point x/y ";
	final static String SLASH = "/";

	public long x;
	public long y;

	public PointL(long x, long y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return POINT + this.x + SLASH + this.y;
	}
}
