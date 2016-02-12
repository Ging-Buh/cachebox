package CB_UI_Base.GL_UI.utils;

public class KineticZoom {

	private final float startZoom;
	private final float endZoom;
	private final long startTime;
	private final long endTime;
	private boolean fertig;

	public KineticZoom(float startZoom, float endZoom, long startTime, long endTime) {
		// KineticZoom = mapViewBase;
		this.startTime = startTime;
		this.endTime = endTime;
		this.startZoom = startZoom;
		this.endZoom = endZoom;
		fertig = false;
	}

	public float getAktZoom() {
		long aktTime = System.currentTimeMillis();
		float faktor = (float) (aktTime - startTime) / (float) (endTime - startTime);
		faktor = com.badlogic.gdx.math.Interpolation.fade.apply(faktor);
		if (faktor >= 1) {
			fertig = true;
			faktor = 1;
		}
		return startZoom + (endZoom - startZoom) * faktor;
	}

	public boolean getFertig() {
		return fertig;
	}
}