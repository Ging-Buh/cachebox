package CB_UI_Base.Math;

import java.io.Serializable;

/**
 * UiSize ist eine Struktur die alle Wichtigen Ger�te abh�ngigen Gr��en enth�lt. Sie wird in der Regel der Klasse Sizes im Constructor
 * �bergeben, damit die Gr��en in dieser berechnet werden k�nnen!
 * 
 * @author Longri
 */
public class devicesSizes implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5441749943486589905L;

	/**
	 * Die Gr��e die CB im gesamten zur Verf�gung steht.
	 */
	public Size Window;

	/**
	 * die Aufl�sung die das Display hat
	 */
	public float Density;

	/**
	 * True wenn die Berechnung f�r Landscape durchgef�hrt werden soll.
	 */
	public boolean isLandscape;
}
