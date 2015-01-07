package CB_UI_Base.Math;

import java.io.Serializable;

/**
 * UiSize ist eine Struktur die alle Wichtigen Geräte abhängigen Größen enthält. Sie wird in der Regel der Klasse Sizes im Constructor
 * übergeben, damit die Größen in dieser berechnet werden können!
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
	 * Die Größe die CB im gesamten zur Verfügung steht.
	 */
	public Size Window;

	/**
	 * die Auflösung die das Display hat
	 */
	public float Density;

	/**
	 * True wenn die Berechnung für Landscape durchgeführt werden soll.
	 */
	public boolean isLandscape;
}
