package CB_UI_Base.Math;

import java.io.Serializable;

/**
 * DevicesSizes ist eine Struktur die alle wichtigen geräteabhängigen Größen enthält. Sie wird in der Regel der Klasse UI_Size_Base im Constructor
 * übergeben, damit die Größen in dieser berechnet werden können!
 * 
 * @author Longri
 */
public class DevicesSizes implements Serializable {
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
