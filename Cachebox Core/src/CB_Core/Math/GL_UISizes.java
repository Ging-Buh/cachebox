package CB_Core.Math;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Vector2;

/**
 * Diese Klasse Kapselt die Werte, welche in der OpenGL Map benötigt werden. Auch die Benutzen Fonts werden hier gespeichert, da die Grösse
 * hier berechnet wird.
 * 
 * @author Longri
 */
public class GL_UISizes implements SizeChangedEvent
{

	// /**
	// * Initialisiert die Größen und Positionen der UI-Elemente der OpenGL Map, anhand der zur Verfügung stehenden Größe und des
	// * Eingestellten DPI Faktors. Für die Berechnung wird die Größe von Gdx.graphics genommen.
	// */
	// public static void initial(Color FontColor)
	// {
	// initial(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), FontColor);
	// }

	/**
	 * Initialisiert die Größen und Positionen der UI-Elemente der OpenGL Map, anhand der übergebenen Größe und des Eingestellten DPI
	 * Faktors.
	 * 
	 * @param width
	 * @param height
	 */
	public static void initial(float width, float height)
	{
		if (DPI != (float) Config.settings.MapViewDPIFaktor.getValue()
				|| FontFaktor != (float) Config.settings.MapViewFontFaktor.getValue())
		{
			DPI = (float) Config.settings.MapViewDPIFaktor.getValue();
			FontFaktor = (float) (0.666666666667 * DPI * Config.settings.MapViewFontFaktor.getValue());
			isInitial = false; // grössen müssen neu Berechnet werden
		}

		if (SurfaceSize == null)
		{
			SurfaceSize = new CB_RectF(0, 0, width, height);
			GL_UISizes tmp = new GL_UISizes();
			SurfaceSize.Add(tmp);

		}
		else
		{
			if (SurfaceSize.setSize(width, height))
			{
				// Surface grösse hat sich geändert, die Positionen der UI-Elemente müssen neu Berechnet werden.
				calcSizes();
				calcPos();
			}
		}

		if (Info == null) Info = new CB_RectF();
		if (Toggle == null) Toggle = new CB_RectF();
		if (ZoomBtn == null) ZoomBtn = new CB_RectF();
		if (ZoomScale == null) ZoomScale = new CB_RectF();
		if (Compass == null) Compass = new CB_RectF();
		if (InfoLine1 == null) InfoLine1 = new Vector2();
		if (InfoLine2 == null) InfoLine2 = new Vector2();
		if (Bubble == null) Bubble = new SizeF();
		if (bubbleCorrect == null) bubbleCorrect = new SizeF();
		if (!isInitial)
		{
			calcSizes();

			Fonts.LoadCalcFonts();
			SpriteCache.LoadSprites();
			calcPos();

			isInitial = true;
		}
	}

	/**
	 * das Rechteck, welches die Größe und Position aller GL_View´s auf der linken Seite darstellt. Dieses Rechteck ist immer Gültig! Das
	 * Rechteck UI_Reight hat die Gleiche Größe und Position wie UI_Left, wenn es sich nicht um ein Tablet Layout handelt.
	 */
	public static CB_RectF UI_Left;

	/**
	 * Das Rechteck, welches die Größe und Position aller GL_View´s auf der rechten Seite darstellt, wenn es sich um ein Tablet Layout
	 * handelt. Wenn es sich nicht um ein Tablet Layout handelt, hat dieses Rechteck die selbe Größe und Position wie UI_Left.
	 */
	public static CB_RectF UI_Right;

	/**
	 * Ist false solange die Größen nicht berechnet sind. Diese müssen nur einmal berechnet Werden, oder wenn ein Faktor (DPI oder
	 * FontFaktor) in den Settings geändert Wurde.
	 */
	private static boolean isInitial = false;

	/**
	 * Die Höhe des Schattens des Info Panels. Diese muss Berechnet werden, da sie für die Berechnung der Inhalt Positionen gebraucht wird.
	 */
	public static float infoShadowHeight;

	public static Vector2 InfoLine1;

	public static Vector2 InfoLine2;

	/**
	 * Dpi Faktor, welcher über die Settings eingestellt werden kann und mit dem HandyDisplay Wert vorbelegt ist. (HD2= 1.5)
	 */
	public static float DPI;

	/**
	 * Die Font Größe wird über den DPI Faktor berechnet und kann über den FontFaktor zusätzlich beeinflusst werden.
	 */
	public static float FontFaktor;

	/**
	 * Das Rechteck in dem das Info Panel dargestellt wird.
	 */
	public static CB_RectF Info;

	/**
	 * Das Rechteck in dem der ToggleButton dargestellt wird.
	 */
	public static CB_RectF Toggle;

	/**
	 * Das Rechteck in dem die Zoom Buttons dargestellt wird.
	 */
	public static CB_RectF ZoomBtn;

	/**
	 * Das Rechteck in dem die Zoom Scala dargestellt wird.
	 */
	public static CB_RectF ZoomScale;

	/**
	 * Die Größe des Compass Icons. Welche Abhängig von der Höhe des Info Panels ist.
	 */
	public static CB_RectF Compass;

	/**
	 * Halbe Compass grösse welche den Mittelpunkt darstellt.
	 */
	public static float halfCompass;

	/**
	 * Die Größe des zur Verfügung stehenden Bereiches von Gdx.graphics
	 */
	public static CB_RectF SurfaceSize;

	/**
	 * Größe des position Markers
	 */
	public static float PosMarkerSize;

	/**
	 * Halbe Größe des Position Markers, welche den Mittelpunkt darstellt
	 */
	public static float halfPosMarkerSize;

	/**
	 * Array der drei möglichen Grössen eines WP Icons
	 */
	public static SizeF[] WPSizes;

	/**
	 * Array der drei möglichen Grössen eines WP Underlay
	 */
	public static SizeF[] UnderlaySizes;

	/**
	 * Größe der Cache Info Bubble
	 */
	public static SizeF Bubble;

	/**
	 * halbe breite der Info Bubble, welche den Mitttelpunkt darstellt
	 */
	public static float halfBubble;

	/**
	 * Korektur Wert zwichen Bubble und deren Content
	 */
	public static SizeF bubbleCorrect;

	/**
	 * Größe des Target Arrows
	 */
	public static SizeF TargetArrow;

	/**
	 * Berechnet die Positionen der UI-Elemente
	 */
	private static void calcPos()
	{
		Float margin = (float) (6.6666667 * DPI);
		Info.setPos(new Vector2(margin, (float) (SurfaceSize.getHeight() - 100 - 66.666667 * DPI)));

		Float CompassMargin = (Info.getHeight() - Compass.getWidth()) / 2;

		Compass.setPos(new Vector2(Info.getX() + CompassMargin, Info.getY() + infoShadowHeight + CompassMargin));

		Toggle.setPos(new Vector2((float) (SurfaceSize.getWidth() - margin - Toggle.getWidth()),
				(float) (SurfaceSize.getHeight() - margin - Toggle.getHeight())));

		ZoomBtn.setPos(new Vector2((float) (SurfaceSize.getWidth() - margin - ZoomBtn.getWidth()), margin));
		ZoomScale
				.setPos(new Vector2(margin, (float) (SurfaceSize.getHeight() - (margin * 4) - Toggle.getHeight() - ZoomScale.getHeight())));
		InfoLine1.x = Compass.getCrossPos().x + margin;
		TextBounds bounds = Fonts.get18().getBounds("52° 34,806N ");
		InfoLine2.x = Info.getX() + Info.getWidth() - bounds.width - (margin * 2);

		Float T1 = Info.getHeight() / 4;

		InfoLine1.y = Info.getCrossPos().y - T1;
		InfoLine2.y = Info.getY() + T1 + bounds.height;

		// Aufräumen
		CompassMargin = null;
		margin = null;
		System.gc();

	}

	/**
	 * Berechnet die Größen der UI-Elemente
	 */
	private static void calcSizes()
	{
		// größe der Frames berechnen
		int frameLeftwidth = UiSizes.RefWidth;

		int WindowWidth = UiSizes.getWindowWidth();
		int frameRightWidth = WindowWidth - frameLeftwidth;

		// Window height- CacheNameViewHeight(35dip)-ImageButtonHeight(65dip)
		int frameHeight = UiSizes.getWindowHeight() - convertDip2Pix(35) - convertDip2Pix(65);

		UI_Left = new CB_RectF(0, convertDip2Pix(65), frameLeftwidth, frameHeight);
		UI_Right = UI_Left.copy();
		if (GlobalCore.isTab)
		{
			UI_Right.setX(frameLeftwidth + 1);
			UI_Right.setWidth(frameRightWidth);
		}

		infoShadowHeight = (float) (3.333333 * DPI);
		Info.setSize(244 * DPI, 58 * DPI);
		Compass.setSize((float) (44.6666667 * DPI), (float) (44.6666667 * DPI));
		halfCompass = Compass.getHeight() / 2;
		Toggle.setSize(58 * DPI, 58 * DPI);
		ZoomBtn.setSize((float) (158 * DPI), 48 * DPI);
		ZoomScale.setSize((float) (58 * DPI), 170 * DPI); // 280
		PosMarkerSize = (float) (46.666667 * DPI);
		halfPosMarkerSize = PosMarkerSize / 2;

		TargetArrow = new SizeF((float) (14 * DPI), (float) (42.666667 * DPI));

		UnderlaySizes = new SizeF[]
			{ new SizeF(13 * DPI, 13 * DPI), new SizeF(14 * DPI, 14 * DPI), new SizeF(21 * DPI, 21 * DPI) };
		WPSizes = new SizeF[]
			{ new SizeF(13 * DPI, 13 * DPI), new SizeF(20 * DPI, 20 * DPI), new SizeF(32 * DPI, 32 * DPI) };

		Bubble.setSize((float) 253.3333334 * DPI, (float) 105.333334 * DPI);
		halfBubble = Bubble.width / 2;
		bubbleCorrect.setSize((float) (6.6666667 * DPI), (float) 26.66667 * DPI);
	}

	static float scale = 0;

	private static int convertDip2Pix(float dips)
	{
		// Converting dips to pixels
		if (scale == 0) scale = UiSizes.getScale();
		return Math.round(dips * scale);
	}

	@Override
	public void sizeChanged()
	{
		calcPos();
	}

}
