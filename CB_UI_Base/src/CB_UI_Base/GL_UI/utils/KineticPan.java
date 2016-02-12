package CB_UI_Base.GL_UI.utils;

import CB_Utils.Math.Point;

public class KineticPan {

	private boolean started;
	private boolean fertig;
	// benutze den Abstand der letzten 5 Positions�nderungen
	final int anzPoints = 3;
	private final int[] x = new int[anzPoints];
	private final int[] y = new int[anzPoints];
	private int diffX;
	private int diffY;
	private long startTs;
	private long endTs;

	public KineticPan() {
		fertig = false;
		started = false;
		diffX = 0;
		diffY = 0;
		for (int i = 0; i < anzPoints; i++) {
			x[i] = 0;
			y[i] = 0;
		}
	}

	public void setLast(long aktTs, int aktX, int aktY) {
		for (int i = anzPoints - 2; i >= 0; i--) {
			x[i + 1] = x[i];
			y[i + 1] = y[i];
		}
		x[0] = aktX;
		y[0] = aktY;

		for (int i = 1; i < anzPoints; i++) {
			if (x[i] == 0)
				x[i] = x[i - 1];
			if (y[i] == 0)
				y[i] = y[i - 1];
		}
		diffX = x[anzPoints - 1] - aktX;
		diffY = aktY - y[anzPoints - 1];

		// debugString = x[2] + " - " + x[1] + " - " + x[0];
	}

	public boolean getFertig() {
		return fertig;
	}

	public boolean getStarted() {
		return started;
	}

	public void start() {
		startTs = System.currentTimeMillis();
		int abstand = (int) Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));
		endTs = startTs + 2000 + abstand * 50 / anzPoints;
		started = true;
		// mapTileLoader.doubleCache();
	}

	public Point getAktPan() {
		Point result = new Point(0, 0);

		long aktTs = System.currentTimeMillis();
		float faktor = (float) (aktTs - startTs) / (float) (endTs - startTs);
		faktor = com.badlogic.gdx.math.Interpolation.exp10Out.apply(faktor);
		if (faktor >= 1) {
			fertig = true;
			faktor = 1;
		}

		result.x = (int) (diffX / anzPoints * (1 - faktor));
		result.y = (int) (diffY / anzPoints * (1 - faktor));
		return result;
	}
}