package CB_UI_Base.Math;

import CB_UI_Base.Global;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Skin.CB_Skin;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Log.Logger;
import CB_Utils.Util.iChanged;

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

		Logger.DEBUG("Initial UISizes => " + width + "/" + height);
		Logger.DEBUG("DPI = " + DPI);

		if (DPI != CB_UI_Base_Settings.MapViewDPIFaktor.getValue() || FontFaktor != CB_UI_Base_Settings.MapViewFontFaktor.getValue())
		{

			DPI = CB_UI_Base_Settings.MapViewDPIFaktor.getValue();

			Logger.DEBUG("DPI != MapViewDPIFaktor " + DPI);

			FontFaktor = (float) (0.666666666667 * DPI * CB_UI_Base_Settings.MapViewFontFaktor.getValue());
			isInitial = false; // grössen müssen neu Berechnet werden
		}

		Logger.DEBUG("Initial UISizes => isInitial" + isInitial);

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
		if (Compass == null) Compass = new CB_RectF();
		if (InfoLine1 == null) InfoLine1 = new Vector2();
		if (InfoLine2 == null) InfoLine2 = new Vector2();
		if (Bubble == null) Bubble = new SizeF();
		if (bubbleCorrect == null) bubbleCorrect = new SizeF();
		if (!isInitial)
		{
			calcSizes();

			CB_UI_Base_Settings.nightMode.addChangedEventListner(new iChanged()
			{

				@Override
				public void isChanged()
				{
					Fonts.setNightMode(CB_UI_Base_Settings.nightMode.getValue());
				}
			});

			try
			{
				Fonts.loadFonts(CB_Skin.INSTANCE);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

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
	 * DPI Wert des Displays, kann nicht über die Settings verändert werden
	 */
	public static float defaultDPI = 1;

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

	// /**
	// * Die Größe der D/T Wertungs Stars
	// */
	// public static SizeF DT_Size;

	public static float margin;

	/**
	 * Berechnet die Positionen der UI-Elemente
	 */
	private static void calcPos()
	{
		Logger.DEBUG("GL_UISizes.calcPos()");

		float w = Global.isTab ? UI_Right.getWidth() : UI_Left.getWidth();
		float h = Global.isTab ? UI_Right.getHeight() : UI_Left.getHeight();

		Info.setPos(new Vector2(margin, (h - margin - Info.getHeight())));

		Float CompassMargin = (Info.getHeight() - Compass.getWidth()) / 2;

		Compass.setPos(new Vector2(Info.getX() + CompassMargin, Info.getY() + infoShadowHeight + CompassMargin));

		Toggle.setPos(new Vector2((w - margin - Toggle.getWidth()), h - margin - Toggle.getHeight()));

		ZoomBtn.setPos(new Vector2((w - margin - ZoomBtn.getWidth()), margin));

		InfoLine1.x = Compass.getMaxX() + margin;
		TextBounds bounds;
		if (Fonts.getNormal() != null)
		{
			bounds = Fonts.getNormal().getBounds("52° 34,806N ");
		}
		else
		{
			bounds = new TextBounds();
			bounds.height = 20;
			bounds.width = 100;
		}

		InfoLine2.x = Info.getX() + Info.getWidth() - bounds.width - (margin * 2);

		Float T1 = Info.getHeight() / 4;

		InfoLine1.y = Info.getMaxY() - T1;
		InfoLine2.y = Info.getY() + T1 + bounds.height;

		// Aufräumen
		CompassMargin = null;

	}

	public static float BottomButtonHeight = convertDip2Pix(65);
	public static float TopButtonHeight = convertDip2Pix(35);

	// public static boolean set_Top_Buttom_Height(float Top, float Bottom)
	// {
	// if (BottomButtonHeight == Bottom && TopButtonHeight == Top) return false;
	//
	// BottomButtonHeight = Bottom;
	// TopButtonHeight = Top;
	// return true;
	// }

	/**
	 * Berechnet die Größen der UI-Elemente
	 */
	private static void calcSizes()
	{
		Logger.DEBUG("GL_UISizes.calcSizes()");
		// größe der Frames berechnen
		int frameLeftwidth = UI_Size_Base.that.RefWidth;

		int WindowWidth = UI_Size_Base.that.getWindowWidth();
		int frameRightWidth = WindowWidth - frameLeftwidth;

		// max 65dp
		if (frameLeftwidth < 400)
		{
			BottomButtonHeight = Math.min(frameLeftwidth / 5.8f, convertDip2Pix(69));
		}
		else
		{
			BottomButtonHeight = Math.min(frameLeftwidth / 5.18f, convertDip2Pix(69));
		}

		margin = (float) (6.6666667 * DPI);

		frameHeight = UI_Size_Base.that.getWindowHeight() - convertDip2Pix(35) - BottomButtonHeight;

		UI_Left = new CB_RectF(0, convertDip2Pix(65), frameLeftwidth, frameHeight);
		UI_Right = UI_Left.copy();
		if (Global.isTab)
		{
			UI_Right.setX(frameLeftwidth + 1);
			UI_Right.setWidth(frameRightWidth);
		}

		infoShadowHeight = (float) (3.333333 * defaultDPI);
		Info.setSize((UI_Size_Base.that.RefWidth - (UI_Size_Base.that.getButtonWidth() * 1.1f) - (margin * 3)), UI_Size_Base.that.getButtonHeight() * 1.1f);
		Compass.setSize((float) (44.6666667 * DPI), (float) (44.6666667 * DPI));
		halfCompass = Compass.getHeight() / 2;
		Toggle.setSize(UI_Size_Base.that.getButtonWidth() * 1.1f, UI_Size_Base.that.getButtonHeight() * 1.1f);
		ZoomBtn.setSize((158 * defaultDPI), 48 * defaultDPI);
		PosMarkerSize = (float) (46.666667 * DPI);
		halfPosMarkerSize = PosMarkerSize / 2;

		TargetArrow = new SizeF((float) (12.6 * DPI), (float) (38.4 * DPI));

		UnderlaySizes = new SizeF[]
			{ new SizeF(13 * DPI, 13 * DPI), new SizeF(14 * DPI, 14 * DPI), new SizeF(21 * DPI, 21 * DPI) };
		WPSizes = new SizeF[]
			{ new SizeF(13 * DPI, 13 * DPI), new SizeF(20 * DPI, 20 * DPI), new SizeF(32 * DPI, 32 * DPI) };

		Bubble.setSize((float) 273.3333334 * defaultDPI, (float) 113.333334 * defaultDPI);
		halfBubble = Bubble.width / 2;
		bubbleCorrect.setSize((float) (6.6666667 * DPI), (float) 26.66667 * DPI);

		// DT_Size = new SizeF(37 * DPI, (37 * DPI * 0.2f));

	}

	static float frameHeight = -1;

	public static void writeDebug(String name, CB_RectF rec)
	{
		Logger.LogCat(name + "   ------ x/y/W/H =  " + rec.getX() + "/" + rec.getY() + "/" + rec.getWidth() + "/" + rec.getHeight());
	}

	public static void writeDebug(String name, float size)
	{
		Logger.LogCat(name + "   ------ size =  " + size);
	}

	public static void writeDebug(String name, SizeF sizeF)
	{
		Logger.LogCat(name + "   ------ W/H =  " + sizeF.width + "/" + sizeF.height);
	}

	public static void writeDebug(String name, SizeF[] SizeArray)
	{
		for (int i = 0; i < SizeArray.length; i++)
		{
			writeDebug(name + "[" + i + "]", SizeArray[i]);
		}
	}

	static float scale = 0;

	public static int convertDip2Pix(float dips)
	{
		// Converting dips to pixels
		if (scale == 0) scale = UI_Size_Base.that.getScale();
		return Math.round(dips * scale);
	}

	@Override
	public void sizeChanged()
	{
		Logger.DEBUG("GL_UISizes.sizeChanged()");
		calcPos();

	}

}
