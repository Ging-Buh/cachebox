package CB_Core.Math;

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
	 * Die Referenz Gr��e welche �ber res.values.dimen.xml festgelegt wurde.</BR> R.dimen.RefSize
	 */
	public int RefSize;

	/**
	 * Die Text Gr��e welche �ber res.values.dimen.xml festgelegt wurde. </BR> R.dimen.TextSize_normal
	 */
	public int TextSize_Normal;

	/**
	 * Die Button Text Gr��e welche �ber res.values.dimen.xml festgelegt wurde. </BR> R.dimen.BtnTextSize
	 */
	public int ButtonTextSize;

	/**
	 * Die Icon Gr��e welche �ber res.values.dimen.xml festgelegt wurde. R.dimen.IconSize
	 */
	public int IconSize;

	/**
	 * Die Margin Gr��e welche �ber res.values.dimen.xml festgelegt wurde. R.dimen.Margin
	 */
	public int Margin;

	/**
	 * Die Arrow Gr��e f�r die CacheList, welche �ber res.values.dimen.xml festgelegt wurde. R.dimen.ArrowSize_List
	 */
	public int ArrowSizeList;

	/**
	 * Die Arrow Gr��e f�r die Map, welche �ber res.values.dimen.xml festgelegt wurde. R.dimen.ArrowSize_Map
	 */
	public int ArrowSizeMap;

	/**
	 * Die TB Icon Gr��e welche �ber res.values.dimen.xml festgelegt wurde. R.dimen.TB_icon_Size
	 */
	public int TB_IconSize;

	/**
	 * True wenn die Berechnung f�r Landscape durchgef�hrt werden soll.
	 */
	public boolean isLandscape;
}
