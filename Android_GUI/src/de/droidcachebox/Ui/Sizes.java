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

package de.droidcachebox.Ui;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.Display;
import android.view.WindowManager;
import de.droidcachebox.R;

/**
 * Enthält die Größen einzelner Controls
 * 
 * @author Longri
 */
public class Sizes
{
	private static Size Button;
	private static Size QuickButtonList;
	private static int CacheInfoHeight;
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
	private static int ScaledFontSize_small;
	private static int ScaledFontSize_supersmall;
	private static int StrengthHeightMultipler;
	private static int IconContextMenuHeight;

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

		float scale = res.getDisplayMetrics().density;

		// Button = new Size(96,88);
		// QuickButtonList = new Size(460,90);

		Button = new Size((int) ((320 * scale) / 5), (int) (((320 * scale) / 5) - 5.3333f * scale));

		QuickButtonList = new Size((int) (320 * scale - (13.3333f * scale)), (int) (((320 * scale) / 5) - 4 * scale));

		if (windowWidth == 240 || windowHeight == 240)
		{
			scaledFontSize_normal = 12;
			scaledFontSize_big = 14;
			ScaledFontSize_small = 11;
			ScaledFontSize_supersmall = 10;
		}
		else
		{
			scaledFontSize_normal = res.getDimensionPixelSize(R.dimen.TextSize_normal);
			scaledFontSize_big = (int) (scaledFontSize_normal * 1.3);
			ScaledFontSize_small = (int) (scaledFontSize_normal * 0.8);
			ScaledFontSize_supersmall = (int) (ScaledFontSize_small * 0.7);
		}
		CornerSize = scaledFontSize_normal / 2;
		CacheInfoHeight = (int) (scaledFontSize_normal * 4.9);
		infoSliderHeight = (int) (scaledFontSize_normal * 2.2);
		iconSize = (int) (scaledFontSize_normal * 3.5);
		spaceWidth = (int) (scaledFontSize_normal * 0.7);
		tabWidth = (int) (scaledFontSize_normal * 0.6);
		halfCornerSize = (int) CornerSize / 2;

		CacheListItemSize = new Size(windowWidth, (int) (scaledFontSize_normal * 5));
		CacheListDrawRec = CacheListItemSize.getBounds(5, 2, -5, -2);
		StrengthHeightMultipler = windowHeight / 600;
		IconContextMenuHeight = (int) (windowHeight / 11.1);
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

	public static int getScaledFontSize_normal()
	{
		return scaledFontSize_normal;
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

}
