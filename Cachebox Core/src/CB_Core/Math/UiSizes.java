/* 
 * Copyright (C) 2011 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package CB_Core.Math;

import CB_Core.Config;
import CB_Core.GlobalCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Vector2;

//import de.cachebox_test.R;

/**
 * Enthält die Größen einzelner Controls
 * 
 * @author Longri
 */
public class UiSizes
{
	private static Size QuickButton;
	private static Size Button;
	private static Size QuickButtonList;
	private static int CacheInfoHeight;
	private static int scaledRefSize_normal;
	private static int scaledIconSize;
	private static int scaledFontSize_normal;
	private static int CornerSize;
	private static int infoSliderHeight;
	private static int iconSize;
	private static int spaceWidth;
	private static int tabWidth;
	private static int halfCornerSize;
	private static int windowWidth;
	private static int windowHeight;
	private static Size CacheListItemSize;
	// private static Rect CacheListDrawRec;
	private static CB_Rect CacheListDrawRec;
	private static int scaledFontSize_big;
	private static int scaledFontSize_btn;
	private static int ScaledFontSize_small;
	private static int ScaledFontSize_supersmall;
	private static int StrengthHeightMultipler;
	private static int IconContextMenuHeight;
	private static float scale;
	private static int margin;

	private static int arrowScaleList;
	private static int arrowScaleMap;
	private static int TB_icon_Size;

	private static double calcBase;

	public static Size initial(devicesSizes ini)
	{
		// Resources res = context.getResources();

		// WindowManager w = context.getWindowManager();
		// Display d = w.getDefaultDisplay();
		windowWidth = ini.Window.width;// d.getWidth();
		windowHeight = ini.Window.height;// d.getHeight();

		// if width>height switch the values (landscape start bug)
		// if (windowWidth > windowHeight)
		// {
		// int temp = windowWidth;
		// windowWidth = windowHeight;
		// windowHeight = temp;
		// }

		scale = ini.Density;// res.getDisplayMetrics().density;

		calcBase = 533.333 * scale;

		// Button = new Size(96,88);
		// QuickButtonList = new Size(460,90);

		int QuickButtonRef;
		int RefWidth;
		if (GlobalCore.isTab)
		{
			QuickButtonRef = 350;
			RefWidth = 340;
		}
		else
		{
			QuickButtonRef = 320;
			RefWidth = windowWidth;
		}

		QuickButton = new Size((int) ((QuickButtonRef * scale) / 5), (int) (((QuickButtonRef * scale) / 5) - 5.3333f * scale));

		// Button = new Size(res.getDimensionPixelSize(R.dimen.BtnSize),
		// (int) ((res.getDimensionPixelSize(R.dimen.BtnSize) - 5.3333f * scale)));

		Button = ini.ButtonSize.Copy();

		QuickButtonList = new Size((int) (QuickButtonRef * scale - (13.3333f * scale)), (int) (((QuickButtonRef * scale) / 5) - 4 * scale));

		scaledRefSize_normal = (int) ((calcBase / (ini.RefSize)) * scale);
		scaledFontSize_normal = (int) ((calcBase / (ini.TextSize_Normal)) * scale);
		scaledFontSize_big = (int) (scaledFontSize_normal * 1.1);
		ScaledFontSize_small = (int) (scaledFontSize_normal * 0.9);
		ScaledFontSize_supersmall = (int) (ScaledFontSize_small * 0.8);
		scaledFontSize_btn = (int) ((calcBase / ini.ButtonTextSize) * scale);

		scaledIconSize = (int) ((calcBase / ini.IconSize) * scale);

		margin = ini.Margin;

		CornerSize = scaledRefSize_normal;
		CacheInfoHeight = (int) (scaledRefSize_normal * 8);
		infoSliderHeight = (int) (scaledRefSize_normal * 2.4);
		iconSize = (int) (int) ((calcBase / ini.IconSize) * scale);
		spaceWidth = (int) (scaledFontSize_normal * 0.9);
		tabWidth = (int) (scaledFontSize_normal * 0.6);
		halfCornerSize = (int) CornerSize / 2;

		CacheListItemSize = new Size(RefWidth, (int) (scaledRefSize_normal * 8.6));
		CacheListDrawRec = CacheListItemSize.getBounds(5, 2, -5, -2);
		StrengthHeightMultipler = (int) (calcBase / 600);
		IconContextMenuHeight = (int) (calcBase / 11.1);

		arrowScaleList = ini.ArrowSizeList;
		arrowScaleMap = ini.ArrowSizeMap;
		TB_icon_Size = ini.TB_IconSize;

		return new Size(windowWidth, windowHeight);

	}

	public static int getTbIconSize()
	{
		return TB_icon_Size;
	}

	public static int getArrowScaleList()
	{
		return arrowScaleList;
	}

	public static int getArrowScaleMap()
	{
		return arrowScaleMap;
	}

	public static int getMargin()
	{
		return margin;
	}

	public static int getWindowHeight()
	{
		return windowHeight;
	}

	public static int getWindowWidth()
	{
		return windowWidth;
	}

	public static int getButtonHeight()
	{
		return Button.height;
	}

	public static int getButtonWidth()
	{
		return Button.width;
	}

	public static int getButtonWidthWide()
	{
		return (int) (Button.width * 1.8);
	}

	public static int getQuickButtonHeight()
	{
		return QuickButton.height;
	}

	public static int getQuickButtonWidth()
	{
		return QuickButton.width;
	}

	public static int getQuickButtonListHeight()
	{
		return QuickButtonList.height;
	}

	public static int getQuickButtonListWidth()
	{
		return QuickButtonList.width;
	}

	public static int getCacheInfoHeight()
	{
		return CacheInfoHeight;
	}

	public static int getCornerSize()
	{
		return CornerSize;
	}

	public static int getScaledFontSize()
	{
		return scaledFontSize_normal;
	}

	public static int getScaledIconSize()
	{
		return scaledIconSize;
	}

	public static int getScaledFontSize_btn()
	{
		return scaledFontSize_btn;
	}

	public static int getScaledRefSize_normal()
	{
		return scaledRefSize_normal;
	}

	public static int getScaledFontSize_big()
	{
		return scaledFontSize_big;
	}

	public static int getScaledFontSize_small()
	{
		return ScaledFontSize_small;
	}

	public static int getScaledFontSize_supersmall()
	{
		return ScaledFontSize_supersmall;
	}

	public static int getInfoSliderHeight()
	{
		return infoSliderHeight;
	}

	public static int getIconSize()
	{
		return iconSize;
	}

	public static int getSpaceWidth()
	{
		return spaceWidth;
	}

	public static int getTabWidth()
	{
		return tabWidth;
	}

	public static int getHalfCornerSize()
	{
		return halfCornerSize;
	}

	public static Size getCacheListItemSize()
	{
		return CacheListItemSize;
	}

	public static CB_Rect getCacheListItemRec()
	{
		return CacheListDrawRec;
	}

	public static int getIconAddCorner()
	{
		return iconSize + CornerSize;
	}

	public static int getStrengthHeight()
	{
		return StrengthHeightMultipler;
	}

	public static int getIconContextMenuHeight()
	{
		return IconContextMenuHeight;
	}

	public static float getScale()
	{
		return scale;
	}

	/**
	 * Diese Klasse Kapselt die Werte, welche in der OpenGL Map benötigt werden. Auch die Benutzen Fonts werden hier gespeichert, da die
	 * Grösse hier berechnet wird.
	 * 
	 * @author Longri
	 */
	public static class GL implements SizeChangedEvent
	{
		/**
		 * Initialisiert die Größen und Positionen der UI-Elemente der OpenGL Map, anhand der zur Verfügung stehenden Größe und des
		 * Eingestellten DPI Faktors. Für die Berechnung wird die Größe von Gdx.graphics genommen.
		 */
		public static void initial()
		{
			initial(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}

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
				GL tmp = new GL();
				SurfaceSize.Add(tmp);

			}
			else
			{
				if (SurfaceSize.setSize(width, height))
				{
					// Surface grösse hat sich geändert, die Positionen der UI-Elemente müssen neu Berechnet werden.
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
				LoadCalcFonts();
				calcPos();

				isInitial = true;
			}
		}

		/**
		 * Ist false solange die Größen nicht berechnet sind. Diese müssen nur einmal berechnet Werden, oder wenn ein Faktor (DPI oder
		 * FontFaktor) in den Settings geändert Wurde.
		 */
		private static boolean isInitial = false;

		/**
		 * Die Höhe des Schattens des Info Panels. Diese muss Berechnet werden, da sie für die Berechnung der Inhalt Positionen gebraucht
		 * wird.
		 */
		public static float infoShadowHeight;

		public static Vector2 InfoLine1;

		public static Vector2 InfoLine2;

		/**
		 * Dpi Faktor, welcher über die Settings eingestellt werden kann und mit dem HandyDisplay Wert vorbelegt ist. (HD2= 1.5)
		 */
		public static float DPI;

		/**
		 * Der bitmap Font der mit ArialBold18.fnt geladen wurde
		 */
		public static BitmapFont fontAB18;

		/**
		 * Der bitmap Font der mit ArialBold16outline.fnt geladen wurde.
		 */
		public static BitmapFont fontAB16out;

		/**
		 * Der bitmap Font der mit ArialBold22.fnt geladen wurde
		 */
		public static BitmapFont fontAB22;

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
			Info.setPos(new Vector2(margin, (float) (SurfaceSize.getHeight() - 66.666667 * DPI)));

			Float CompassMargin = (Info.getHeight() - Compass.getWidth()) / 2;

			Compass.setPos(new Vector2(Info.getX() + CompassMargin, Info.getY() + infoShadowHeight + CompassMargin));

			Toggle.setPos(new Vector2((float) (SurfaceSize.getWidth() - margin - Toggle.getWidth()), (float) (SurfaceSize.getHeight()
					- margin - Toggle.getHeight())));

			ZoomBtn.setPos(new Vector2((float) (SurfaceSize.getWidth() - margin - ZoomBtn.getWidth()), margin));
			ZoomScale.setPos(new Vector2(margin, (float) (SurfaceSize.getHeight() - (margin * 4) - Toggle.getHeight() - ZoomScale
					.getHeight())));
			InfoLine1.x = Compass.getCrossPos().x + margin;
			TextBounds bounds = fontAB18.getBounds("52° 34,806N ");
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

		/**
		 * Lädt die verwendeten Bitmap Fonts und berechnet die entsprechenden Größen
		 */
		private static void LoadCalcFonts()
		{
			fontAB18 = new BitmapFont(Gdx.files.internal("data/ArialBold18.fnt"), Gdx.files.internal("data/ArialBold18.png"), false);
			// fontAB18.setColor(0.0f, 0.2f, 0.0f, 1.0f);
			fontAB18.setScale(FontFaktor);

			fontAB16out = new BitmapFont(Gdx.files.internal("data/ArialBold16outline.fnt"),
					Gdx.files.internal("data/ArialBold16outline.png"), false);
			// fontAB16out.setColor(1.0f, 0.2f, 0.0f, 1.0f);
			fontAB16out.setScale(FontFaktor);

			fontAB22 = new BitmapFont(Gdx.files.internal("data/ArialBold22.fnt"), Gdx.files.internal("data/ArialBold22.png"), false);
			// fontAB22.setColor(0.0f, 0.0f, 0.0f, 1.0f);
			fontAB22.setScale(FontFaktor);

		}

		@Override
		public void sizeChanged()
		{
			calcPos();
		}

	}

}
