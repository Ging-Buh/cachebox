package CB_Core.Math;

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
	 * Die Referenz Größe welche über res.values.dimen.xml festgelegt wurde.</BR> R.dimen.RefSize
	 */
	public int RefSize;

	/**
	 * Die Text Größe welche über res.values.dimen.xml festgelegt wurde. </BR> R.dimen.TextSize_normal
	 */
	public int TextSize_Normal;

	/**
	 * Die Button Text Größe welche über res.values.dimen.xml festgelegt wurde. </BR> R.dimen.BtnTextSize
	 */
	public int ButtonTextSize;

	/**
	 * Die Icon Größe welche über res.values.dimen.xml festgelegt wurde. R.dimen.IconSize
	 */
	public int IconSize;

	/**
	 * Die Margin Größe welche über res.values.dimen.xml festgelegt wurde. R.dimen.Margin
	 */
	public int Margin;

	/**
	 * Die Arrow Größe für die CacheList, welche über res.values.dimen.xml festgelegt wurde. R.dimen.ArrowSize_List
	 */
	public int ArrowSizeList;

	/**
	 * Die Arrow Größe für die Map, welche über res.values.dimen.xml festgelegt wurde. R.dimen.ArrowSize_Map
	 */
	public int ArrowSizeMap;

	/**
	 * Die TB Icon Größe welche über res.values.dimen.xml festgelegt wurde. R.dimen.TB_icon_Size
	 */
	public int TB_IconSize;

	/**
	 * True wenn die Berechnung für Landscape durchgeführt werden soll.
	 */
	public boolean isLandscape;
}
