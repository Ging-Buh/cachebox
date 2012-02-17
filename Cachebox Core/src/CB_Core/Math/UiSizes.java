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

import CB_Core.GlobalCore;

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

}
