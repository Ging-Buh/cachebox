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

package de.cachebox_test.Ui;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.Display;
import android.view.WindowManager;
import de.cachebox_test.R;

/**
 * Enthält die Größen einzelner Controls
 * 
 * @author Longri
 */
public class Sizes
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
	private static Rect CacheListDrawRec;
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

	public static void initial(boolean land, Activity context)
	{
		// TODO berechne die Werte anhand der Auflösung.
		// jetzt eingesetzte Werte beziehen sich auf eine Auflösung von
		// 460x800(HD2) Longri

		Resources res = context.getResources();

		WindowManager w = context.getWindowManager();
		Display d = w.getDefaultDisplay();
		windowWidth = d.getWidth();
		windowHeight = d.getHeight();

		// if width>height switch the values (landscape start bug)
		if (windowWidth > windowHeight)
		{
			int temp = windowWidth;
			windowWidth = windowHeight;
			windowHeight = temp;
		}

		scale = res.getDisplayMetrics().density;

		// Button = new Size(96,88);
		// QuickButtonList = new Size(460,90);

		QuickButton = new Size((int) ((320 * scale) / 5), (int) (((320 * scale) / 5) - 5.3333f * scale));

		Button = new Size(res.getDimensionPixelSize(R.dimen.BtnSize),
				(int) ((res.getDimensionPixelSize(R.dimen.BtnSize) - 5.3333f * scale)));

		QuickButtonList = new Size((int) (320 * scale - (13.3333f * scale)), (int) (((320 * scale) / 5) - 4 * scale));

		scaledRefSize_normal = (int) ((windowHeight / (res.getDimensionPixelSize(R.dimen.RefSize))) * scale);
		scaledFontSize_normal = (int) ((windowHeight / (res.getDimensionPixelSize(R.dimen.TextSize_normal))) * scale);
		scaledFontSize_big = (int) (scaledFontSize_normal * 1.1);
		ScaledFontSize_small = (int) (scaledFontSize_normal * 0.9);
		ScaledFontSize_supersmall = (int) (ScaledFontSize_small * 0.8);
		scaledFontSize_btn = (int) ((windowHeight / (res.getDimensionPixelSize(R.dimen.BtnTextSize))) * scale);

		scaledIconSize = (int) ((windowHeight / (res.getDimensionPixelSize(R.dimen.IconSize))) * scale);

		margin = res.getDimensionPixelSize(R.dimen.Margin);

		CornerSize = scaledRefSize_normal;
		CacheInfoHeight = (int) (scaledRefSize_normal * 8);
		infoSliderHeight = (int) (scaledRefSize_normal * 2.4);
		iconSize = (int) (int) ((windowHeight / (res.getDimensionPixelSize(R.dimen.IconSize))) * scale);
		spaceWidth = (int) (scaledFontSize_normal * 0.9);
		tabWidth = (int) (scaledFontSize_normal * 0.6);
		halfCornerSize = (int) CornerSize / 2;

		CacheListItemSize = new Size(windowWidth, (int) (scaledRefSize_normal * 8.6));
		CacheListDrawRec = CacheListItemSize.getBounds(5, 2, -5, -2);
		StrengthHeightMultipler = windowHeight / 600;
		IconContextMenuHeight = (int) (windowHeight / 11.1);

		arrowScaleList = res.getDimensionPixelSize(R.dimen.ArrowSize_List);
		arrowScaleMap = res.getDimensionPixelSize(R.dimen.ArrowSize_Map);
		TB_icon_Size = res.getDimensionPixelSize(R.dimen.TB_icon_Size);
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

	public static Rect getCacheListItemRec()
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
